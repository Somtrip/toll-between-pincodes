package com.som.toll.loader;

import com.som.toll.entity.TollPlaza;
import com.som.toll.repository.TollPlazaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class TollPlazaCsvLoader {

    private final TollPlazaRepository plazaRepository;

    @Value("${toll.csv.path:classpath:toll_plaza_india.csv}")
    private Resource tollCsv;

    @PostConstruct
    public void loadCsv() {
        try {
            if (plazaRepository.count() > 0) {
                log.info("Toll plazas already loaded in DB. Skipping CSV load.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    tollCsv.getInputStream(), StandardCharsets.UTF_8))) {

                // skip header
                reader.lines()
                        .skip(1)
                        .map(line -> line.split(",", -1))
                        .map(parts -> TollPlaza.builder()
                                .longitude(Double.parseDouble(parts[0].trim()))
                                .latitude(Double.parseDouble(parts[1].trim()))
                                .name(parts[2].trim())
                                .geoState(parts[3].trim())
                                .build())
                        .forEach(plazaRepository::save);

                log.info(" Toll plazas loaded successfully from CSV: {}", tollCsv.getFilename());
            }
        } catch (Exception e) {
            log.error(" Failed to load toll CSV from path [{}]", tollCsv, e);
        }
    }
}
