// In src/main/java/com/cargopro/LoaderBooker/service/BookingService.java
package com.cargopro.LoaderBooker.service;

import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.cargopro.LoaderBooker.specification.BookingSpecification;

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

        if (load.getStatus() == LoadStatus.CANCELLED) {
            throw new BadRequestException("Cannot create a booking for a cancelled load.");
        }

        Booking booking = modelMapper.map(bookingRequestDTO, Booking.class);
        booking.setLoad(load);

        Booking savedBooking = bookingRepository.save(booking);

        if (load.getStatus() == LoadStatus.POSTED) {
            load.setStatus(LoadStatus.BOOKED);
            loadRepository.save(load);
        }

        BookingResponseDTO responseDTO = modelMapper.map(savedBooking, BookingResponseDTO.class);
        responseDTO.setLoadId(savedBooking.getLoad().getId());
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

        BookingStatus oldStatus = existingBooking.getStatus();
        BookingStatus newStatus = bookingRequestDTO.getStatus();

        if (newStatus != null) {
            if (newStatus == BookingStatus.ACCEPTED && oldStatus != BookingStatus.ACCEPTED) {
                existingBooking.setStatus(BookingStatus.ACCEPTED);
                Load load = existingBooking.getLoad();
                if (load.getStatus() == LoadStatus.POSTED) {
                    load.setStatus(LoadStatus.BOOKED);
                    loadRepository.save(load);
                }
            } else if (newStatus == BookingStatus.REJECTED && oldStatus != BookingStatus.REJECTED) {
                existingBooking.setStatus(BookingStatus.REJECTED);
                checkAndRevertLoadStatus(existingBooking.getLoad().getId());
            } else if (oldStatus == BookingStatus.ACCEPTED && newStatus != BookingStatus.ACCEPTED) {
                throw new BadRequestException("Cannot change booking status from ACCEPTED.");
            } else {
                modelMapper.map(bookingRequestDTO, existingBooking);
                existingBooking.setId(bookingId);
                existingBooking.setLoad(existingBooking.getLoad());
                if (bookingRequestDTO.getStatus() == null) {
                    existingBooking.setStatus(oldStatus);
                }
            }
        } else {
            modelMapper.map(bookingRequestDTO, existingBooking);
            existingBooking.setId(bookingId);
            existingBooking.setLoad(existingBooking.getLoad());
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

        UUID loadId = booking.getLoad().getId();
        bookingRepository.delete(booking);

        checkAndRevertLoadStatus(loadId);
    }

    public Page<BookingResponseDTO> getAllBookings(String loadIdStr, String transporterId, BookingStatus status, Pageable pageable) {
        UUID loadId = null;
        if (loadIdStr != null && !loadIdStr.isEmpty()) {
            try {
                loadId = UUID.fromString(loadIdStr);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid Load ID format: " + loadIdStr);
            }
        }

        Specification<Booking> spec = BookingSpecification.withFilters(loadId, transporterId, status);
        Page<Booking> bookingsPage = bookingRepository.findAll(spec, pageable);

        return bookingsPage.map(booking -> {
            BookingResponseDTO dto = modelMapper.map(booking, BookingResponseDTO.class);
            if (booking.getLoad() != null) {
                dto.setLoadId(booking.getLoad().getId());
            }
            return dto;
        });
    }

    private void checkAndRevertLoadStatus(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found for booking status check: " + loadId));

        List<Booking> associatedBookings = bookingRepository.findByLoadId(loadId);

        boolean allRejectedOrNoOtherActiveBookings = associatedBookings.isEmpty()
                || associatedBookings.stream()
                        .allMatch(b -> b.getStatus() == BookingStatus.REJECTED);

        if (load.getStatus() == LoadStatus.BOOKED && allRejectedOrNoOtherActiveBookings) {
            load.setStatus(LoadStatus.POSTED);
            loadRepository.save(load);
        }
    }
}
