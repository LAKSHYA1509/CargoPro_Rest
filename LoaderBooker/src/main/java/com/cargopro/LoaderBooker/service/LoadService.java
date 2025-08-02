package com.cargopro.LoaderBooker.service;

import org.springframework.stereotype.Service;

import com.cargopro.LoaderBooker.exception.ResourceNotFoundException;
import com.cargopro.LoaderBooker.model.dto.LoadRequestDTO;
import com.cargopro.LoaderBooker.model.dto.LoadResponseDTO;
import com.cargopro.LoaderBooker.model.entity.Load;
import com.cargopro.LoaderBooker.model.enums.LoadStatus;
import com.cargopro.LoaderBooker.repository.LoadRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoadService {

    private final LoadRepository loadRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public LoadService(LoadRepository loadRepository, ModelMapper modelMapper) {
        this.loadRepository = loadRepository;
        this.modelMapper = modelMapper;
    }

    public LoadResponseDTO createLoad(LoadRequestDTO loadRequestDTO) {
        Load load = modelMapper.map(loadRequestDTO, Load.class);

        Load savedLoad = loadRepository.save(load);
        return modelMapper.map(savedLoad, LoadResponseDTO.class);
    }

    public LoadResponseDTO getLoadById(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));
        return modelMapper.map(load, LoadResponseDTO.class);
    }

    public LoadResponseDTO updateLoad(UUID loadId, LoadRequestDTO loadRequestDTO) {
        Load existingLoad = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with ID: " + loadId));

        modelMapper.map(loadRequestDTO, existingLoad);

        existingLoad.setId(loadId);

        existingLoad.setStatus(existingLoad.getStatus());

        Load updatedLoad = loadRepository.save(existingLoad);
        return modelMapper.map(updatedLoad, LoadResponseDTO.class);
    }

    public void deleteLoad(UUID loadId) {
        if (!loadRepository.existsById(loadId)) {
            throw new ResourceNotFoundException("Load not found with ID: " + loadId);
        }
        loadRepository.deleteById(loadId);
    }

    public Page<LoadResponseDTO> getAllLoads(String shipperId, String truckType, LoadStatus status, Pageable pageable) {

        Page<Load> loadsPage = loadRepository.findAll(pageable);
        return loadsPage.map(load -> modelMapper.map(load, LoadResponseDTO.class));
    }
}
