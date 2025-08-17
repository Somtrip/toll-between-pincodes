package com.som.toll.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.som.toll.dto.RouteDto;
import com.som.toll.dto.TollPlazaDto;
import com.som.toll.dto.TollPlazaResponse;
import com.som.toll.entity.TollPlaza;
import com.som.toll.entity.TollRouteCache;
import com.som.toll.repository.TollPlazaRepository;
import com.som.toll.repository.TollRouteCacheRepository;
import com.som.toll.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TollPlazaService {

    private static final Logger log = LoggerFactory.getLogger(TollPlazaService.class);

    private final TollPlazaRepository plazaRepository;
    private final TollRouteCacheRepository cacheRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public TollPlazaService(TollPlazaRepository plazaRepository,
                            TollRouteCacheRepository cacheRepository) {
        this.plazaRepository = plazaRepository;
        this.cacheRepository = cacheRepository;
    }

    /**
     * Returns toll plazas for the given route. Uses cache unless refresh == true.
     */
    public TollPlazaResponse findTollsOnRouteCached(RouteDto route, double[] src, double[] dst) {
        return findTollsOnRoute(route, src, dst, /*refresh*/ false);
    }

    /**
     * Overload with explicit refresh control.
     */
    public TollPlazaResponse findTollsOnRoute(RouteDto route, double[] src, double[] dst, boolean refresh) {
        if (!refresh) {
            var cached = cacheRepository.findBySourcePincodeAndDestinationPincode(
                    route.getSourcePincode(), route.getDestinationPincode());
            if (cached.isPresent()) {
                try {
                    log.debug("Serving from cache for {} -> {}", route.getSourcePincode(), route.getDestinationPincode());
                    return mapper.readValue(cached.get().getResponseJson(), TollPlazaResponse.class);
                } catch (Exception e) {
                    log.warn("Cache read failed, recomputing: {}", e.getMessage());
                }
            }
        } else {
            log.debug("Bypassing cache for {} -> {}", route.getSourcePincode(), route.getDestinationPincode());
        }

        // Compute fresh (with de-dup)
        var tolls = computeTollsOnRoute(route, src, dst);
        var response = new TollPlazaResponse(route, tolls);

        // Save to cache
        try {
            var json = mapper.writeValueAsString(response);
            cacheRepository.save(TollRouteCache.builder()
                    .sourcePincode(route.getSourcePincode())
                    .destinationPincode(route.getDestinationPincode())
                    .responseJson(json)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to cache response: {}", e.getMessage());
        }

        return response;
    }

    /**
     * Computes toll plazas on the route corridor and de-duplicates aggressively.
     */
    private List<TollPlazaDto> computeTollsOnRoute(RouteDto route, double[] src, double[] dst) {
        double latA = src[0], lonA = src[1];
        double latB = dst[0], lonB = dst[1];

        var all = plazaRepository.findAll();

        // Map to candidates & corridor filter
        var candidates = all.stream()
                .map(p -> toCandidate(p, latA, lonA, latB, lonB))
                .filter(Candidate::onCorridor)
                .collect(Collectors.toList());

        // First-stage dedup: name(norm) + rounded coords (5 decimals ~ 1.1m)
        Map<String, Candidate> uniqueByKey = new LinkedHashMap<>();
        for (var c : candidates) {
            String key = normalizeKey(c.name(), c.lat(), c.lng());
            // keep the one closer to source, in case of dup
            uniqueByKey.merge(key, c, (a, b) -> a.distanceFromSource() <= b.distanceFromSource() ? a : b);
        }

        // Second-stage (safety): collapse same-name items within 100m
        // This handles minor coord jitter across datasets.
        List<Candidate> uniques = new ArrayList<>();
        for (var c : uniqueByKey.values()) {
            boolean merged = false;
            for (int i = 0; i < uniques.size(); i++) {
                var u = uniques.get(i);
                if (sameName(c.name(), u.name())) {
                    double dMeters = GeoUtils.haversineKm(c.lat(), c.lng(), u.lat(), u.lng()) * 1000.0;
                    if (dMeters <= 100.0) { // treat as same physical plaza
                        // keep the closer-to-source one
                        if (c.distanceFromSource() < u.distanceFromSource()) {
                            uniques.set(i, c);
                        }
                        merged = true;
                        break;
                    }
                }
            }
            if (!merged) uniques.add(c);
        }

        // Debug stats
        log.debug("Plazas: total={}, corridor={}, uniqueKey={}, uniqueName100m={}",
                all.size(), candidates.size(), uniqueByKey.size(), uniques.size());

        // Sort and map to DTOs
        return uniques.stream()
                .sorted(Comparator.comparingDouble(Candidate::distanceFromSource))
                .map(c -> new TollPlazaDto(c.name(), c.lat(), c.lng(), c.distanceFromSource(), c.geoState()))
                .toList();
    }

    private static boolean sameName(String a, String b) {
        return normalizeName(a).equals(normalizeName(b));
    }

    private static String normalizeKey(String name, double lat, double lng) {
        String nm = normalizeName(name);
        double la = round(lat, 5);
        double ln = round(lng, 5);
        return nm + "|" + la + "|" + ln;
    }

    private static String normalizeName(String s) {
        // lower, trim, collapse whitespace, remove periods/extra dashes
        return s == null ? "" :
                s.toLowerCase(Locale.ROOT)
                 .replace('.', ' ')
                 .replace('-', ' ')
                 .replace('_', ' ')
                 .replaceAll("\\s+", " ")
                 .trim();
    }

    private static double round(double v, int scale) {
        return new BigDecimal(v).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Converts a TollPlaza entity to a candidate with route geometry checks.
     */
    private Candidate toCandidate(TollPlaza p, double latA, double lonA, double latB, double lonB) {
        double latP = p.getLatitude();
        double lonP = p.getLongitude();

        double crossKm = GeoUtils.crossTrackDistanceKm(latA, lonA, latB, lonB, latP, lonP);
        double alongKm = GeoUtils.alongTrackDistanceKm(latA, lonA, latB, lonB, latP, lonP);
        double abKm = GeoUtils.haversineKm(latA, lonA, latB, lonB);

        // relaxed segment tolerance
        boolean withinSegment = alongKm >= -20.0 && alongKm <= abKm + 20.0;
        boolean onCorridor = crossKm <= 25.0 && withinSegment;

        double distFromSource = GeoUtils.haversineKm(latA, lonA, latP, lonP);

        return new Candidate(p.getName(), latP, lonP, p.getGeoState(), onCorridor, distFromSource);
    }

    private record Candidate(String name,
                             double lat,
                             double lng,
                             String geoState,
                             boolean onCorridor,
                             double distanceFromSource) {}
}
