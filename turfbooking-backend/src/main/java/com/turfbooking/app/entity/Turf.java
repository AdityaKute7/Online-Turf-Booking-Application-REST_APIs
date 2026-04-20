package com.turfbooking.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "turfs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private BigDecimal pricePerHour;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    // Many turfs can belong to one owner (User with OWNER role)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
