package com.cargopro.LoaderBooker.controller;

import com.cargopro.LoaderBooker.model.dto.BookingRequestDTO;
import com.cargopro.LoaderBooker.model.dto.BookingResponseDTO;
import com.cargopro.LoaderBooker.model.enums.BookingStatus;
import com.cargopro.LoaderBooker.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO bookingRequestDTO) {
        BookingResponseDTO createdBooking = bookingService.createBooking(bookingRequestDTO);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable UUID bookingId) {
        BookingResponseDTO booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> updateBooking(@PathVariable UUID bookingId, @Valid @RequestBody BookingRequestDTO bookingRequestDTO) {
        BookingResponseDTO updatedBooking = bookingService.updateBooking(bookingId, bookingRequestDTO);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponseDTO>> getAllBookings(
            @RequestParam(required = false) String loadId,
            @RequestParam(required = false) String transporterId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponseDTO> bookings = bookingService.getAllBookings(loadId, transporterId, status, pageable);
        return ResponseEntity.ok(bookings);
    }
}