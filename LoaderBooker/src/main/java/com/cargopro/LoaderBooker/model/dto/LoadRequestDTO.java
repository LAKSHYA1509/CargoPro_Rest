package com.cargopro.LoaderBooker.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LoadRequestDTO {

    private UUID id;
    @NotBlank(message = "Shipper ID is required")
    private String shipperId;

    @NotBlank(message = "Facility is required")
    private String facility;
    @NotBlank(message = "Loading point is required")
    private String loadingPoint;
    @NotBlank(message = "Unloading point is required")
    private String unloadingPoint;
    @NotNull(message = "Loading date is required")
    private LocalDateTime loadingDate;
    @NotNull(message = "Unloading date is required")
    private LocalDateTime unloadingDate;
    @NotBlank(message = "Product type is required")
    private String productType;
    @NotBlank(message = "Truck type is required")
    private String truckType;
    @NotNull(message = "Number of trucks is required")
    private Integer noOfTrucks;
    @NotNull(message = "Weight is required")
    private Double weight;
    private String comment;

}
