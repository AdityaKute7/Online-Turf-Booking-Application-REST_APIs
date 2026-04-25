package com.turfbooking.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurfResponse {

    private Long id;
    private String name;
    private String location;
    private BigDecimal pricePerHour;
    private String description;
    private String imageUrl;
    private Long ownerId;
    private String ownerName;
}
