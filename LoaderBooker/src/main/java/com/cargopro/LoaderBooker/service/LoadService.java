// In src/main/java/com/cargopro/LoaderBooker/service/LoadService.java

package com.cargopro.LoaderBooker.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cargopro.LoaderBooker.exception.BadRequestException;
import com.cargopro.LoaderBooker.exception.ResourceNotFoundException;
import com.cargopro.LoaderBooker.model.dto.LoadRequestDTO;
import com.cargopro.LoaderBooker.model.dto.LoadResponseDTO;
import com.cargopro.LoaderBooker.model.entity.Load;
import com.cargopro.LoaderBooker.model.enums.LoadStatus;
import com.cargopro.LoaderBooker.repository.LoadRepository;

@Service
public class LoadService {

    private final LoadRepository loadRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public LoadService(LoadRepository loadRepository, ModelMapper modelMapper) {
        this.loadRepository = loadRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public LoadResponseDTO createLoad(LoadRequestDTO loadRequestDTO) {
        Load load = modelMapper.map(loadRequestDTO, Load.class);
        load.setDatePosted(LocalDateTime.now());
        load.setStatus(LoadStatus.POSTED); // Default status for new loads
        Load savedLoad = loadRepository.save(load);
        return modelMapper.map(savedLoad, LoadResponseDTO.class);
    }

    public LoadResponseDTO getLoadById(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));
        return modelMapper.map(load, LoadResponseDTO.class);
    }

    @Transactional
    public LoadResponseDTO updateLoad(UUID loadId, LoadRequestDTO loadRequestDTO) {
        Load existingLoad = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));

        // Prevent status changes via this general update; use specific status endpoint
        LoadStatus originalStatus = existingLoad.getStatus();
        modelMapper.map(loadRequestDTO, existingLoad);
        existingLoad.setId(loadId); // Ensure ID is preserved
        existingLoad.setStatus(originalStatus); // Keep original status unless changed by specific status update

        Load updatedLoad = loadRepository.save(existingLoad);
        return modelMapper.map(updatedLoad, LoadResponseDTO.class);
    }

    @Transactional
    public void deleteLoad(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));
        // Consider cascading delete for associated bookings here if not handled by JPA cascade types
        // For now, assume JPA cascade handles it or bookings must be deleted first.
        // If you need explicit deletion of bookings before load, add:
        // bookingRepository.deleteByLoadId(loadId); // Requires a new method in BookingRepository
        loadRepository.delete(load);
    }

    public Page<LoadResponseDTO> getAllLoads(Pageable pageable) {
        Page<Load> loadsPage = loadRepository.findAll(pageable);
        return loadsPage.map(load -> modelMapper.map(load, LoadResponseDTO.class));
    }

    // New method for Step 3: Update Load Status
    @Transactional
    public LoadResponseDTO updateLoadStatus(UUID loadId, LoadStatus newStatus) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));

        // Implement transition rules:
        // Example: Cannot go from DELIVERED to POSTED, BOOKED, or IN_TRANSIT
        if (load.getStatus() == LoadStatus.DELIVERED &&
            (newStatus == LoadStatus.POSTED || newStatus == LoadStatus.BOOKED || newStatus == LoadStatus.IN_TRANSIT)) {
            throw new BadRequestException("Cannot change status from DELIVERED to " + newStatus);
        }
        // Example: Cannot cancel a delivered load
        if (load.getStatus() == LoadStatus.DELIVERED && newStatus == LoadStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a delivered load.");
        }
        // Example: Only a BOOKED load can go IN_TRANSIT
        if (newStatus == LoadStatus.IN_TRANSIT && load.getStatus() != LoadStatus.BOOKED) {
             throw new BadRequestException("Load must be BOOKED to go IN_TRANSIT.");
        }
        // Example: Only IN_TRANSIT load can be DELIVERED
        if (newStatus == LoadStatus.DELIVERED && load.getStatus() != LoadStatus.IN_TRANSIT) {
            throw new BadRequestException("Load must be IN_TRANSIT to be DELIVERED.");
        }
        // Example: Any status can be CANCELLED, as long as it's not DELIVERED (already covered)
        // No specific rules for POSTED -> BOOKED, as this is handled by booking creation.
        // No specific rules for BOOKED -> POSTED, as this is handled by booking deletion/rejection.


        load.setStatus(newStatus);
        Load updatedLoad = loadRepository.save(load);
        return modelMapper.map(updatedLoad, LoadResponseDTO.class);
    }
}