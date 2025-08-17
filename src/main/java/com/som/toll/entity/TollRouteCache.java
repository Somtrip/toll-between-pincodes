package com.som.toll.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "toll_route_cache")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TollRouteCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourcePincode;
    private String destinationPincode;

    @Lob
    @Column(columnDefinition = "LONGTEXT")  // <-- important
    private String responseJson;

    private LocalDateTime createdAt;
}
