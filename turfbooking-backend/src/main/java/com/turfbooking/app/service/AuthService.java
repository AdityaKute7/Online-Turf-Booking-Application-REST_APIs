package com.turfbooking.app.service;

import com.turfbooking.app.dto.AuthResponse;
import com.turfbooking.app.dto.LoginRequest;
import com.turfbooking.app.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
