package com.turfbooking.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TurfRequest {

    @NotBlank(message = "Turf name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.1", message = "Price must be greater than 0")
    private BigDecimal pricePerHour;

    private String description;

    private String imageUrl;
}
