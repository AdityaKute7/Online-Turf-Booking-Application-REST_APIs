package com.turfbooking.app.service.impl;


import com.turfbooking.app.dto.TurfRequest;
import com.turfbooking.app.dto.TurfResponse;
import com.turfbooking.app.entity.Turf;
import com.turfbooking.app.entity.User;
import com.turfbooking.app.exception.ResourceNotFoundException;
import com.turfbooking.app.repository.TurfRepository;
import com.turfbooking.app.repository.UserRepository;
import com.turfbooking.app.service.TurfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurfServiceImpl implements TurfService {

    private final TurfRepository turfRepository;
    private final UserRepository userRepository;

    @Override
    public TurfResponse createTurf(TurfRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));

        Turf turf = Turf.builder()
                .name(request.getName())
                .location(request.getLocation())
                .pricePerHour(request.getPricePerHour())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .owner(owner)
                .build();

        return mapToResponse(turfRepository.save(turf));
    }

    @Override
    public TurfResponse getTurfById(Long id) {
        Turf turf = turfRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turf not found with id: " + id));
        return mapToResponse(turf);
    }

    @Override
    public List<TurfResponse> getAllTurfs() {
        return turfRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TurfResponse> getTurfsByOwner(Long ownerId) {
        return turfRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TurfResponse mapToResponse(Turf turf) {
        return TurfResponse.builder()
                .id(turf.getId())
                .name(turf.getName())
                .location(turf.getLocation())
                .pricePerHour(turf.getPricePerHour())
                .description(turf.getDescription())
                .imageUrl(turf.getImageUrl())
                .ownerId(turf.getOwner().getId())
                .ownerName(turf.getOwner().getName())
                .build();
    }
}
