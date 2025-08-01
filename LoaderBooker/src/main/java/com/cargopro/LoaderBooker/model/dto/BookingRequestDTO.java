package com.cargopro.LoaderBooker.model.dto;

import com.cargopro.LoaderBooker.model.enums.BookingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingRequestDTO {

    private UUID id;
    @NotNull(message = "Load ID is required")
    private UUID loadId;
    @NotBlank(message = "Transporter ID is required")
    private String transporterId;
    @NotNull(message = "Proposed rate is required")
    private Double proposedRate;
    private String comment;
    private BookingStatus status;
}
