package com.cargopro.LoaderBooker.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.cargopro.LoaderBooker.model.enums.BookingStatus;

import lombok.Data;

@Data
public class BookingResponseDTO {
private UUID id;
    private UUID loadId;
    private String transporterId;
    private Double proposedRate;
    private String comment;
    private BookingStatus status;
    private LocalDateTime requestedAt;
}
