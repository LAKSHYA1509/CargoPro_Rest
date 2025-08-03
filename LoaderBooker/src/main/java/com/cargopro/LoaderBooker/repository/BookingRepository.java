package com.cargopro.LoaderBooker.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.cargopro.LoaderBooker.model.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    // Custom query methods can be defined here if needed

    // Example: Find bookings by customer name
    // List<Booking> findByCustomerName(String customerName);
    
    // Additional methods can be added as per requirements
    List<Booking> findByLoadId(UUID loadId);

}
