package com.turfbooking.app.controller;

import com.turfbooking.app.dto.ApiResponse;
import com.turfbooking.app.dto.TurfRequest;
import com.turfbooking.app.dto.TurfResponse;
import com.turfbooking.app.entity.User;
import com.turfbooking.app.service.TurfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turfs")
@RequiredArgsConstructor
@Tag(name = "Turfs", description = "Turf management endpoints")
public class TurfController {

    private final TurfService turfService;

    @GetMapping
    @Operation(summary = "Get all turfs (public)")
    public ResponseEntity<ApiResponse<List<TurfResponse>>> getAllTurfs() {
        List<TurfResponse> turfs = turfService.getAllTurfs();
        return ResponseEntity.ok(ApiResponse.success("Turfs fetched successfully", turfs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get turf by ID (public)")
    public ResponseEntity<ApiResponse<TurfResponse>> getTurfById(@PathVariable Long id) {
        TurfResponse turf = turfService.getTurfById(id);
        return ResponseEntity.ok(ApiResponse.success("Turf fetched successfully", turf));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Create a new turf", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TurfResponse>> createTurf(
            @Valid @RequestBody TurfRequest request,
            @AuthenticationPrincipal User currentUser) {

        TurfResponse turf = turfService.createTurf(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Turf created successfully", turf));
    }

    @GetMapping("/my-turfs")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get turfs by current owner", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<TurfResponse>>> getMyTurfs(
            @AuthenticationPrincipal User currentUser) {

        List<TurfResponse> turfs = turfService.getTurfsByOwner(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Owner turfs fetched", turfs));
    }
}
