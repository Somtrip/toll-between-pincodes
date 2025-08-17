package com.som.toll.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "toll_plaza")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollPlaza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) 
    private String name;

    @Column(nullable = false) 
    private Double latitude;

    @Column(nullable = false) 
    private Double longitude;

    @Column(nullable = false) 
    private String geoState;   // NEW field
}
