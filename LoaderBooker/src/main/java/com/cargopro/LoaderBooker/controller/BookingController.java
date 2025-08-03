// In src/main/java/com/cargopro/LoaderBooker/controller/BookingController.java
package com.cargopro.LoaderBooker.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; // Import for @Operation
import org.springframework.http.HttpStatus; // Import for @Parameter
import org.springframework.http.ResponseEntity; // Import for @ApiResponse
import org.springframework.web.bind.annotation.DeleteMapping; // Import for 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cargopro.LoaderBooker.model.dto.BookingRequestDTO;
import com.cargopro.LoaderBooker.model.dto.BookingResponseDTO;
import com.cargopro.LoaderBooker.model.enums.BookingStatus;
import com.cargopro.LoaderBooker.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Booking Management", description = "Operations related to load bookings by transporters") // Tag for the controller
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Create a new booking", description = "Allows a transporter to create a booking for an existing load.")
    @ApiResponse(responseCode = "201", description = "Booking created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or load is cancelled")
    @ApiResponse(responseCode = "404", description = "Load not found")
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO bookingRequestDTO) {
        BookingResponseDTO createdBooking = bookingService.createBooking(bookingRequestDTO);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    @Operation(summary = "Get booking details by ID", description = "Retrieves a single booking by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Booking found")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBookingById(
            @Parameter(description = "ID of the booking to retrieve", required = true) @PathVariable UUID bookingId) {
        BookingResponseDTO booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "Update booking details", description = "Updates an existing booking. Can be used to change proposed rate, comment, or status (e.g., ACCEPTED, REJECTED).")
    @ApiResponse(responseCode = "200", description = "Booking updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or status transition")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> updateBooking(
            @Parameter(description = "ID of the booking to update", required = true) @PathVariable UUID bookingId,
            @Valid @RequestBody BookingRequestDTO bookingRequestDTO) {
        BookingResponseDTO updatedBooking = bookingService.updateBooking(bookingId, bookingRequestDTO);
        return ResponseEntity.ok(updatedBooking);
    }

    @Operation(summary = "Delete a booking", description = "Deletes a booking by its ID. May revert the associated load's status if it's the last active booking.")
    @ApiResponse(responseCode = "204", description = "Booking deleted successfully (No Content)")
    @ApiResponse(responseCode = "404", description = "Booking not found")
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(
            @Parameter(description = "ID of the booking to delete", required = true) @PathVariable UUID bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all bookings with filtering and pagination",
               description = "Retrieves a paginated list of bookings, with optional filters by load ID, transporter ID, and booking status.")
    @ApiResponse(responseCode = "200", description = "List of bookings retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid filter parameter format (e.g., invalid Load ID UUID)")
    @GetMapping
    public ResponseEntity<Page<BookingResponseDTO>> getAllBookings(
            @Parameter(description = "Optional: Filter bookings by Load ID (UUID string)") @RequestParam(required = false) String loadId,
            @Parameter(description = "Optional: Filter bookings by Transporter ID") @RequestParam(required = false) String transporterId,
            @Parameter(description = "Optional: Filter bookings by Status (PENDING, ACCEPTED, REJECTED)") @RequestParam(required = false) BookingStatus status,
            @Parameter(description = "Page number (0-indexed, default 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (default 10)") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponseDTO> bookings = bookingService.getAllBookings(loadId, transporterId, status, pageable);
        return ResponseEntity.ok(bookings);
    }
}