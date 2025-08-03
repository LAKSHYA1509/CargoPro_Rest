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
        load.setStatus(LoadStatus.POSTED); 
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

        LoadStatus originalStatus = existingLoad.getStatus();
        modelMapper.map(loadRequestDTO, existingLoad);
        existingLoad.setId(loadId); 
        existingLoad.setStatus(originalStatus); 

        Load updatedLoad = loadRepository.save(existingLoad);
        return modelMapper.map(updatedLoad, LoadResponseDTO.class);
    }

    @Transactional
    public void deleteLoad(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));

        loadRepository.delete(load);
    }

    public Page<LoadResponseDTO> getAllLoads(Pageable pageable) {
        Page<Load> loadsPage = loadRepository.findAll(pageable);
        return loadsPage.map(load -> modelMapper.map(load, LoadResponseDTO.class));
    }

    @Transactional
    public LoadResponseDTO updateLoadStatus(UUID loadId, LoadStatus newStatus) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));

        if (load.getStatus() == LoadStatus.DELIVERED &&
            (newStatus == LoadStatus.POSTED || newStatus == LoadStatus.BOOKED || newStatus == LoadStatus.IN_TRANSIT)) {
            throw new BadRequestException("Cannot change status from DELIVERED to " + newStatus);
        }

        if (load.getStatus() == LoadStatus.DELIVERED && newStatus == LoadStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a delivered load.");
        }

        if (newStatus == LoadStatus.IN_TRANSIT && load.getStatus() != LoadStatus.BOOKED) {
             throw new BadRequestException("Load must be BOOKED to go IN_TRANSIT.");
        }

        if (newStatus == LoadStatus.DELIVERED && load.getStatus() != LoadStatus.IN_TRANSIT) {
            throw new BadRequestException("Load must be IN_TRANSIT to be DELIVERED.");
        }

        load.setStatus(newStatus);
        Load updatedLoad = loadRepository.save(load);
        return modelMapper.map(updatedLoad, LoadResponseDTO.class);
    }
}