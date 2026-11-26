package com.healthdekho.service;

import com.healthdekho.dto.LoginRequest;
import com.healthdekho.dto.LoginResponse;
import com.healthdekho.dto.RegisterRequest;
import com.healthdekho.dto.RegisterResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);
}
