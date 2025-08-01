package com.cargopro.LoaderBooker.model.dto;

import com.cargopro.LoaderBooker.model.enums.LoadStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LoadResponseDTO {
    private UUID id;
    private String shipperId;
    private String facility;
    private String loadingPoint;
    private String unloadingPoint;
    private LocalDateTime loadingDate;
    private LocalDateTime unloadingDate;
    private String productType;
    private String truckType;
    private Integer noOfTrucks;
    private Double weight;
    private String comment;
    private LocalDateTime datePosted;
    private LoadStatus status;
}