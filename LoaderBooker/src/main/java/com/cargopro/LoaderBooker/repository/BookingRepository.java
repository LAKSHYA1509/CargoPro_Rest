package com.cargopro.LoaderBooker.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cargopro.LoaderBooker.model.entity.Booking;


@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Custom query methods can be defined here if needed

    // Example: Find bookings by customer name
    // List<Booking> findByCustomerName(String customerName);
    
    // Additional methods can be added as per requirements

}
