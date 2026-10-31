package com.prokey.baccaratio.controller;

import com.prokey.baccaratio.controller.dto.AuthRequest;
import com.prokey.baccaratio.controller.dto.AuthResponse;
import com.prokey.baccaratio.controller.dto.RegisterRequest;
import com.prokey.baccaratio.controller.dto.UserDto;
import com.prokey.baccaratio.mapper.UserMapper;
import com.prokey.baccaratio.security.JwtUtil;
import com.prokey.baccaratio.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserMapper userMapper;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UserService userService, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public UserDto registerUser(@Valid @RequestBody RegisterRequest request) {
        return userMapper.userToUserDto(userService.register(request.username(), request.password()));
    }

    @PostMapping("/login")
    public AuthResponse createAuthenticationToken(@Valid @RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return new AuthResponse(jwtUtil.generateToken(userDetails));
    }
}
