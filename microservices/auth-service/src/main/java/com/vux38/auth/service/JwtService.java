package com.vux38.auth.service;

import org.springframework.stereotype.Service;

import com.vux38.auth.security.JwtTokenService;

@Service
public class JwtService {

    private final JwtTokenService jwtTokenService;

    public JwtService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public String generateAccessToken(String username) {
        return jwtTokenService.generateToken(username);
    }
}
