package com.turfbooking.app.service;

import com.turfbooking.app.dto.TurfRequest;
import com.turfbooking.app.dto.TurfResponse;

import java.util.List;

public interface TurfService {

    TurfResponse createTurf(TurfRequest request, Long ownerId);

    TurfResponse getTurfById(Long id);

    List<TurfResponse> getAllTurfs();

    List<TurfResponse> getTurfsByOwner(Long ownerId);
}
