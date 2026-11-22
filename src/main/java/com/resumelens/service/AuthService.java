package com.resumelens.service;

import com.resumelens.dto.AuthResponse;
import com.resumelens.dto.LoginRequest;
import com.resumelens.dto.RegisterRequest;
import com.resumelens.entity.User;
import com.resumelens.exception.ApiException;
import com.resumelens.repository.UserRepository;
import com.resumelens.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        try {
            User user = users.save(new User(email, encoder.encode(request.password())));
            return new AuthResponse(jwt.generate(user), user.getEmail());
        } catch (DataIntegrityViolationException e) {   // two sign-ups at the same instant
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = users.findByEmail(email)
                .filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        return new AuthResponse(jwt.generate(user), user.getEmail());
    }
}