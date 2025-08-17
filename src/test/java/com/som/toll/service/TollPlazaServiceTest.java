package com.som.toll.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.som.toll.dto.RouteDto;
import com.som.toll.dto.TollPlazaResponse;
import com.som.toll.entity.TollPlaza;
import com.som.toll.entity.TollRouteCache;
import com.som.toll.repository.TollPlazaRepository;
import com.som.toll.repository.TollRouteCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class TollPlazaServiceTest {

    private TollPlazaRepository plazaRepository;
    private TollRouteCacheRepository cacheRepository;
    private TollPlazaService service;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        plazaRepository = Mockito.mock(TollPlazaRepository.class);
        cacheRepository = Mockito.mock(TollRouteCacheRepository.class);
        service = new TollPlazaService(plazaRepository, cacheRepository);
    }

    @Test
    void shouldReturnFromCacheIfAvailable() throws Exception {
        // given
        var route = new RouteDto("560064", "411045", 855.8);
        var cachedResponse = new TollPlazaResponse(route, List.of());
        String json = mapper.writeValueAsString(cachedResponse);

        Mockito.when(cacheRepository.findBySourcePincodeAndDestinationPincode("560064", "411045"))
                .thenReturn(Optional.of(TollRouteCache.builder()
                        .sourcePincode("560064")
                        .destinationPincode("411045")
                        .responseJson(json)
                        .createdAt(LocalDateTime.now())
                        .build()));

        // when
        var result = service.findTollsOnRouteCached(route, new double[]{12.9, 77.6}, new double[]{18.5, 73.9});

        // then
        assertThat(result.getRoute().getDistanceInKm()).isEqualTo(855.8);
        Mockito.verify(plazaRepository, Mockito.never()).findAll();
    }

    @Test
    void shouldComputeFreshAndDeduplicateTolls() {
        // given
        var route = new RouteDto("560064", "411045", 855.8);

        var plaza1 = new TollPlaza(1L, "Devanahalli Toll Plaza", 13.1936004, 77.6472356, "Karnataka");
        var plaza2 = new TollPlaza(2L, "Devanahalli Toll Plaza", 13.1936004, 77.6472356, "Karnataka"); // duplicate

        Mockito.when(plazaRepository.findAll()).thenReturn(List.of(plaza1, plaza2));
        Mockito.when(cacheRepository.findBySourcePincodeAndDestinationPincode("560064", "411045"))
                .thenReturn(Optional.empty());
        Mockito.when(cacheRepository.save(any(TollRouteCache.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = service.findTollsOnRouteCached(route,
                new double[]{12.9, 77.6}, new double[]{18.5, 73.9});

        // then
        assertThat(result.getTollPlazas()).hasSize(1); // deduplicated
        assertThat(result.getTollPlazas().get(0).getName()).isEqualTo("Devanahalli Toll Plaza");
    }
}

