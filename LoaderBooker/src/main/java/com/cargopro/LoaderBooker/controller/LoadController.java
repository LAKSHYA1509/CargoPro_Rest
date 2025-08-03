package com.cargopro.LoaderBooker.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cargopro.LoaderBooker.model.dto.LoadRequestDTO;
import com.cargopro.LoaderBooker.model.dto.LoadResponseDTO;
import com.cargopro.LoaderBooker.model.enums.LoadStatus;
import com.cargopro.LoaderBooker.service.LoadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/load")
@Tag(name = "Load Management", description = "Operations related to Loads")
public class LoadController {

    private final LoadService loadService;

    @Autowired
    public LoadController(LoadService loadService) {
        this.loadService = loadService;
    }

    @Operation(summary = "Create a new load", description = "Adds a new load (cargo request) to the system.")
    @ApiResponse(responseCode = "201", description = "Load created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @PostMapping
    public ResponseEntity<LoadResponseDTO> createLoad(@Valid @RequestBody LoadRequestDTO loadRequestDTO) {
        LoadResponseDTO createdLoad = loadService.createLoad(loadRequestDTO);
        return new ResponseEntity<>(createdLoad, HttpStatus.CREATED);
    }

    @Operation(summary = "Get load details by ID", description = "Retrieves a single load by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Load found")
    @ApiResponse(responseCode = "404", description = "Load not found")
    @GetMapping("/{loadId}")
    public ResponseEntity<LoadResponseDTO> getLoadById(@PathVariable UUID loadId) {
        LoadResponseDTO load = loadService.getLoadById(loadId);
        return ResponseEntity.ok(load);
    }

    @PutMapping("/{loadId}")
    public ResponseEntity<LoadResponseDTO> updateLoad(@PathVariable UUID loadId, @Valid @RequestBody LoadRequestDTO loadRequestDTO) {
        LoadResponseDTO updatedLoad = loadService.updateLoad(loadId, loadRequestDTO);
        return ResponseEntity.ok(updatedLoad);
    }

    @DeleteMapping("/{loadId}")
    public ResponseEntity<Void> deleteLoad(@PathVariable UUID loadId) {
        loadService.deleteLoad(loadId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update load status", description = "Changes the status of a specific load (e.g., to IN_TRANSIT, DELIVERED, CANCELLED).")
    @ApiResponse(responseCode = "200", description = "Load status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    @ApiResponse(responseCode = "404", description = "Load not found")
    @PutMapping("/{loadId}/status")
   public ResponseEntity<LoadResponseDTO> updateLoadStatus(
            @Parameter(description = "ID of the load to update", required = true) @PathVariable UUID loadId,
            @Parameter(description = "New status for the load", required = true) @RequestParam LoadStatus newStatus) {
        LoadResponseDTO updatedLoad = loadService.updateLoadStatus(loadId, newStatus);
        return ResponseEntity.ok(updatedLoad);
    }

    @GetMapping
    public ResponseEntity<Page<LoadResponseDTO>> getAllLoads(
            @RequestParam(required = false) String shipperId,
            @RequestParam(required = false) String truckType,
            @RequestParam(required = false) LoadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoadResponseDTO> loads = loadService.getAllLoads(pageable);
        return ResponseEntity.ok(loads);
    }
}