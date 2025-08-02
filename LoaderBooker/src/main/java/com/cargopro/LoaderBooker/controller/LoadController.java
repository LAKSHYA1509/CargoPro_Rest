package com.cargopro.LoaderBooker.controller;

import com.cargopro.LoaderBooker.model.dto.LoadRequestDTO;
import com.cargopro.LoaderBooker.model.dto.LoadResponseDTO;
import com.cargopro.LoaderBooker.model.enums.LoadStatus;
import com.cargopro.LoaderBooker.service.LoadService;
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
@RequestMapping("/load")
public class LoadController {

    private final LoadService loadService;

    @Autowired
    public LoadController(LoadService loadService) {
        this.loadService = loadService;
    }

    @PostMapping
    public ResponseEntity<LoadResponseDTO> createLoad(@Valid @RequestBody LoadRequestDTO loadRequestDTO) { // [cite: 51]
        LoadResponseDTO createdLoad = loadService.createLoad(loadRequestDTO);
        return new ResponseEntity<>(createdLoad, HttpStatus.CREATED);
    }

    @GetMapping("/{loadId}")
    public ResponseEntity<LoadResponseDTO> getLoadById(@PathVariable UUID loadId) { // [cite: 53]
        LoadResponseDTO load = loadService.getLoadById(loadId);
        return ResponseEntity.ok(load);
    }

    @PutMapping("/{loadId}")
    public ResponseEntity<LoadResponseDTO> updateLoad(@PathVariable UUID loadId, @Valid @RequestBody LoadRequestDTO loadRequestDTO) { // [cite: 54]
        LoadResponseDTO updatedLoad = loadService.updateLoad(loadId, loadRequestDTO);
        return ResponseEntity.ok(updatedLoad);
    }

    @DeleteMapping("/{loadId}")
    public ResponseEntity<Void> deleteLoad(@PathVariable UUID loadId) { // [cite: 55]
        loadService.deleteLoad(loadId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping // [cite: 52]
    public ResponseEntity<Page<LoadResponseDTO>> getAllLoads(
            @RequestParam(required = false) String shipperId,
            @RequestParam(required = false) String truckType,
            @RequestParam(required = false) LoadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoadResponseDTO> loads = loadService.getAllLoads(shipperId, truckType, status, pageable);
        return ResponseEntity.ok(loads);
    }
}