package com.cargopro.LoaderBooker.service;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cargopro.LoaderBooker.exception.BadRequestException;
import com.cargopro.LoaderBooker.exception.ResourceNotFoundException;
import com.cargopro.LoaderBooker.model.dto.BookingRequestDTO;
import com.cargopro.LoaderBooker.model.dto.BookingResponseDTO;
import com.cargopro.LoaderBooker.model.entity.Booking;
import com.cargopro.LoaderBooker.model.entity.Load;
import com.cargopro.LoaderBooker.model.enums.BookingStatus;
import com.cargopro.LoaderBooker.model.enums.LoadStatus;
import com.cargopro.LoaderBooker.repository.BookingRepository;
import com.cargopro.LoaderBooker.repository.LoadRepository;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final LoadRepository loadRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public BookingService(BookingRepository bookingRepository, LoadRepository loadRepository, ModelMapper modelMapper) {
        this.bookingRepository = bookingRepository;
        this.loadRepository = loadRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO bookingRequestDTO) {
        Load load = loadRepository.findById(bookingRequestDTO.getLoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + bookingRequestDTO.getLoadId()));

        if (load.getStatus() != LoadStatus.POSTED) {
            throw new BadRequestException("Load is not available for booking. Current status: " + load.getStatus());
        }

        Booking booking = modelMapper.map(bookingRequestDTO, Booking.class);
        booking.setLoad(load);

        Booking savedBooking = bookingRepository.save(booking);
        
        // Only change load status to BOOKED if the booking is accepted
        if (savedBooking.getStatus() == BookingStatus.ACCEPTED && load.getStatus() == LoadStatus.POSTED) {
            load.setStatus(LoadStatus.BOOKED);
            loadRepository.save(load);
        }
        
        BookingResponseDTO responseDTO = modelMapper.map(savedBooking, BookingResponseDTO.class);
        responseDTO.setLoadId(load.getId());
        return responseDTO;
    }
    public BookingResponseDTO getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        BookingResponseDTO responseDTO = modelMapper.map(booking, BookingResponseDTO.class);
        responseDTO.setLoadId(booking.getLoad().getId());
        return responseDTO;
    }

    @Transactional
    public BookingResponseDTO updateBooking(UUID bookingId, BookingRequestDTO bookingRequestDTO) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!existingBooking.getLoad().getId().equals(bookingRequestDTO.getLoadId())) {
            throw new BadRequestException("Cannot change the associated load for an existing booking.");
        }

        BookingStatus newStatus = bookingRequestDTO.getStatus();
        if (newStatus != null && newStatus == BookingStatus.ACCEPTED && existingBooking.getStatus() != BookingStatus.ACCEPTED) {
            existingBooking.setStatus(BookingStatus.ACCEPTED);
        } else if (newStatus != null && newStatus == BookingStatus.REJECTED && existingBooking.getStatus() != BookingStatus.REJECTED) {
            existingBooking.setStatus(BookingStatus.REJECTED);

        } else if (newStatus != null && existingBooking.getStatus() == BookingStatus.ACCEPTED && newStatus != BookingStatus.ACCEPTED) {

            throw new BadRequestException("Cannot change booking status from ACCEPTED.");
        } else {

            modelMapper.map(bookingRequestDTO, existingBooking);
            existingBooking.setId(bookingId); 
            existingBooking.setLoad(existingBooking.getLoad()); 

            if (bookingRequestDTO.getStatus() == null) {
                existingBooking.setStatus(existingBooking.getStatus());
            }
        }

        Booking updatedBooking = bookingRepository.save(existingBooking);

        BookingResponseDTO responseDTO = modelMapper.map(updatedBooking, BookingResponseDTO.class);
        responseDTO.setLoadId(updatedBooking.getLoad().getId());
        return responseDTO;
    }

    @Transactional
    public void deleteBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        bookingRepository.delete(booking);
    }

    public Page<BookingResponseDTO> getAllBookings(String loadId, String transporterId, BookingStatus status, Pageable pageable) {

        Page<Booking> bookingsPage = bookingRepository.findAll(pageable);
        return bookingsPage.map(booking -> {
            BookingResponseDTO dto = modelMapper.map(booking, BookingResponseDTO.class);
            dto.setLoadId(booking.getLoad().getId());
            return dto;
        });
    }
}
