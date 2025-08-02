package com.cargopro.LoaderBooker.service;

import com.cargopro.LoaderBooker.exception.ResourceNotFoundException;
import com.cargopro.LoaderBooker.model.dto.LoadRequestDTO;
import com.cargopro.LoaderBooker.model.dto.LoadResponseDTO;
import com.cargopro.LoaderBooker.model.entity.Load;
import com.cargopro.LoaderBooker.model.enums.LoadStatus;
import com.cargopro.LoaderBooker.repository.LoadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoadServiceTest {

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private LoadService loadService;

    private Load sampleLoad;
    private LoadRequestDTO sampleLoadRequestDTO;
    private LoadResponseDTO sampleLoadResponseDTO;
    private UUID loadId;

    @BeforeEach
    void setUp() {
        loadId = UUID.randomUUID();
        sampleLoad = new Load();
        sampleLoad.setId(loadId);
        sampleLoad.setShipperId("shipper123");
        sampleLoad.setLoadingPoint("Delhi");
        sampleLoad.setUnloadingPoint("Mumbai");
        sampleLoad.setLoadingDate(LocalDateTime.now().plusDays(1));
        sampleLoad.setUnloadingDate(LocalDateTime.now().plusDays(3));
        sampleLoad.setProductType("Electronics");
        sampleLoad.setTruckType("Open");
        sampleLoad.setNoOfTrucks(1);
        sampleLoad.setWeight(100.0);
        sampleLoad.setComment("Fragile items");
        sampleLoad.setDatePosted(LocalDateTime.now());
        sampleLoad.setStatus(LoadStatus.POSTED);

        sampleLoadRequestDTO = new LoadRequestDTO();
        sampleLoadRequestDTO.setShipperId("shipper123");
        sampleLoadRequestDTO.setLoadingPoint("Delhi");
        sampleLoadRequestDTO.setUnloadingPoint("Mumbai");
        sampleLoadRequestDTO.setLoadingDate(LocalDateTime.now().plusDays(1));
        sampleLoadRequestDTO.setUnloadingDate(LocalDateTime.now().plusDays(3));
        sampleLoadRequestDTO.setProductType("Electronics");
        sampleLoadRequestDTO.setTruckType("Open");
        sampleLoadRequestDTO.setNoOfTrucks(1);
        sampleLoadRequestDTO.setWeight(100.0);
        sampleLoadRequestDTO.setComment("Fragile items");

        sampleLoadResponseDTO = new LoadResponseDTO();
        sampleLoadResponseDTO.setId(loadId);
        sampleLoadResponseDTO.setShipperId("shipper123");
        sampleLoadResponseDTO.setLoadingPoint("Delhi");
        sampleLoadResponseDTO.setUnloadingPoint("Mumbai");
        sampleLoadResponseDTO.setLoadingDate(sampleLoad.getLoadingDate());
        sampleLoadResponseDTO.setUnloadingDate(sampleLoad.getUnloadingDate());
        sampleLoadResponseDTO.setProductType("Electronics");
        sampleLoadResponseDTO.setTruckType("Open");
        sampleLoadResponseDTO.setNoOfTrucks(1);
        sampleLoadResponseDTO.setWeight(100.0);
        sampleLoadResponseDTO.setComment("Fragile items");
        sampleLoadResponseDTO.setDatePosted(sampleLoad.getDatePosted());
        sampleLoadResponseDTO.setStatus(LoadStatus.POSTED);
    }

    @Test
    void testCreateLoadSuccess() {
        when(modelMapper.map(sampleLoadRequestDTO, Load.class)).thenReturn(sampleLoad);
        when(loadRepository.save(any(Load.class))).thenReturn(sampleLoad);
        when(modelMapper.map(sampleLoad, LoadResponseDTO.class)).thenReturn(sampleLoadResponseDTO);

        LoadResponseDTO result = loadService.createLoad(sampleLoadRequestDTO);

        assertNotNull(result);
        assertEquals(loadId, result.getId());
        assertEquals(LoadStatus.POSTED, result.getStatus());
        verify(loadRepository, times(1)).save(any(Load.class));
    }

    @Test
    void testGetLoadByIdSuccess() {
        when(loadRepository.findById(loadId)).thenReturn(Optional.of(sampleLoad));
        when(modelMapper.map(sampleLoad, LoadResponseDTO.class)).thenReturn(sampleLoadResponseDTO);

        LoadResponseDTO result = loadService.getLoadById(loadId);

        assertNotNull(result);
        assertEquals(loadId, result.getId());
    }

    @Test
    void testGetLoadByIdNotFound() {
        when(loadRepository.findById(loadId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loadService.getLoadById(loadId));
    }

    @Test
    void testUpdateLoadSuccess() {
        Load existingLoadFromDb = new Load(); // Simulate load fetched from DB
        existingLoadFromDb.setId(loadId);
        existingLoadFromDb.setShipperId("shipper123");
        existingLoadFromDb.setLoadingPoint("Delhi");
        existingLoadFromDb.setUnloadingPoint("Mumbai");
        existingLoadFromDb.setLoadingDate(sampleLoad.getLoadingDate());
        existingLoadFromDb.setUnloadingDate(sampleLoad.getUnloadingDate());
        existingLoadFromDb.setProductType("Electronics");
        existingLoadFromDb.setTruckType("Open");
        existingLoadFromDb.setNoOfTrucks(1);
        existingLoadFromDb.setWeight(100.0);
        existingLoadFromDb.setComment("Fragile items");
        existingLoadFromDb.setDatePosted(sampleLoad.getDatePosted());
        existingLoadFromDb.setStatus(LoadStatus.POSTED); // Initial status

        Load updatedLoad = new Load(); // Simulate the load object after ModelMapper updates it
        updatedLoad.setId(loadId);
        updatedLoad.setShipperId("newShipper"); // Changed field
        updatedLoad.setLoadingPoint("Pune");
        updatedLoad.setUnloadingPoint("Bangalore");
        updatedLoad.setLoadingDate(existingLoadFromDb.getLoadingDate());
        updatedLoad.setUnloadingDate(existingLoadFromDb.getUnloadingDate());
        updatedLoad.setProductType("Textiles");
        updatedLoad.setTruckType("Closed");
        updatedLoad.setNoOfTrucks(2);
        updatedLoad.setWeight(200.0);
        updatedLoad.setComment("Urgent delivery");
        updatedLoad.setDatePosted(existingLoadFromDb.getDatePosted());
        updatedLoad.setStatus(existingLoadFromDb.getStatus()); // Status should remain the same from business logic

        LoadRequestDTO updateRequestDTO = new LoadRequestDTO();
        updateRequestDTO.setShipperId("newShipper");
        updateRequestDTO.setLoadingPoint("Pune");
        updateRequestDTO.setUnloadingPoint("Bangalore");
        updateRequestDTO.setLoadingDate(sampleLoad.getLoadingDate());
        updateRequestDTO.setUnloadingDate(sampleLoad.getUnloadingDate());
        updateRequestDTO.setProductType("Textiles");
        updateRequestDTO.setTruckType("Closed");
        updateRequestDTO.setNoOfTrucks(2);
        updateRequestDTO.setWeight(200.0);
        updateRequestDTO.setComment("Urgent delivery");


        LoadResponseDTO updatedResponseDTO = new LoadResponseDTO();
        updatedResponseDTO.setId(loadId);
        updatedResponseDTO.setShipperId("newShipper");
        updatedResponseDTO.setLoadingPoint("Pune");
        updatedResponseDTO.setUnloadingPoint("Bangalore");
        updatedResponseDTO.setLoadingDate(sampleLoad.getLoadingDate());
        updatedResponseDTO.setUnloadingDate(sampleLoad.getUnloadingDate());
        updatedResponseDTO.setProductType("Textiles");
        updatedResponseDTO.setTruckType("Closed");
        updatedResponseDTO.setNoOfTrucks(2);
        updatedResponseDTO.setWeight(200.0);
        updatedResponseDTO.setComment("Urgent delivery");
        updatedResponseDTO.setDatePosted(sampleLoad.getDatePosted());
        updatedResponseDTO.setStatus(LoadStatus.POSTED); // Expected status after update (should preserve original)

        when(loadRepository.findById(loadId)).thenReturn(Optional.of(existingLoadFromDb));

        // CORRECTED LINE: Cast argument 0 to LoadRequestDTO
        doAnswer(invocation -> {
            LoadRequestDTO sourceDTO = invocation.getArgument(0, LoadRequestDTO.class);
            Load destinationEntity = invocation.getArgument(1, Load.class);

            destinationEntity.setShipperId(sourceDTO.getShipperId());
            destinationEntity.setLoadingPoint(sourceDTO.getLoadingPoint());
            destinationEntity.setUnloadingPoint(sourceDTO.getUnloadingPoint());
            destinationEntity.setLoadingDate(sourceDTO.getLoadingDate());
            destinationEntity.setUnloadingDate(sourceDTO.getUnloadingDate());
            destinationEntity.setProductType(sourceDTO.getProductType());
            destinationEntity.setTruckType(sourceDTO.getTruckType());
            destinationEntity.setNoOfTrucks(sourceDTO.getNoOfTrucks());
            destinationEntity.setWeight(sourceDTO.getWeight());
            destinationEntity.setComment(sourceDTO.getComment());
            return null;
        }).when(modelMapper).map(any(LoadRequestDTO.class), any(Load.class));

        when(loadRepository.save(any(Load.class))).thenReturn(updatedLoad);
        when(modelMapper.map(any(Load.class), eq(LoadResponseDTO.class))).thenReturn(updatedResponseDTO);


        LoadResponseDTO result = loadService.updateLoad(loadId, updateRequestDTO);

        assertNotNull(result);
        assertEquals("newShipper", result.getShipperId());
        assertEquals("Pune", result.getLoadingPoint());
        assertEquals(loadId, result.getId());
        assertEquals(LoadStatus.POSTED, result.getStatus());
        verify(loadRepository, times(1)).save(existingLoadFromDb); // Verify save was called on the modified existingLoad
    }

    @Test
    void testUpdateLoadNotFound() {
        when(loadRepository.findById(loadId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loadService.updateLoad(loadId, sampleLoadRequestDTO));
        verify(loadRepository, never()).save(any(Load.class));
    }

    @Test
    void testDeleteLoadSuccess() {
        when(loadRepository.existsById(loadId)).thenReturn(true);
        doNothing().when(loadRepository).deleteById(loadId);

        loadService.deleteLoad(loadId);

        verify(loadRepository, times(1)).deleteById(loadId);
    }

    @Test
    void testDeleteLoadNotFound() {
        when(loadRepository.existsById(loadId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> loadService.deleteLoad(loadId));
        verify(loadRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void testGetAllLoads() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Load> loadsPage = new PageImpl<>(Arrays.asList(sampleLoad), pageable, 1);
        when(loadRepository.findAll(pageable)).thenReturn(loadsPage);
        when(modelMapper.map(any(Load.class), eq(LoadResponseDTO.class))).thenReturn(sampleLoadResponseDTO);


        Page<LoadResponseDTO> result = loadService.getAllLoads(null, null, null, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(loadId, result.getContent().get(0).getId());
        verify(loadRepository, times(1)).findAll(pageable);
    }
}