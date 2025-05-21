package com.example.usermanagement.controller;

import com.example.usermanagement.dto.AuthResponseDto;
import com.example.usermanagement.dto.LoginRequestDto;
import com.example.usermanagement.dto.RegisterRequestDto;
import com.example.usermanagement.security.jwt.JwtTokenProvider;
import com.example.usermanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        try {
            userService.registerUser(registerRequestDto);
            return new ResponseEntity<>(new AuthResponseDto("User registered successfully!", true), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Log the exception details if necessary
            return new ResponseEntity<>(new AuthResponseDto(e.getMessage(), false), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new AuthResponseDto("Login successful", true, jwt));
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(new AuthResponseDto("Invalid username or password", false), HttpStatus.UNAUTHORIZED);
        }
    }
}
