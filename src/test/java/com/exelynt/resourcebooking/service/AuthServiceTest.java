package com.exelynt.resourcebooking.service;

import com.exelynt.resourcebooking.dto.AuthRequest;
import com.exelynt.resourcebooking.dto.AuthResponse;
import com.exelynt.resourcebooking.entity.Role;
import com.exelynt.resourcebooking.entity.User;
import com.exelynt.resourcebooking.repository.UserRepository;
import com.exelynt.resourcebooking.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void testLogin_Success() {
        AuthRequest request = new AuthRequest("user", "password");
        User user = User.builder().username("user").password("encoded").role(Role.USER).build();
        
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("mocked-jwt-token");
        
        AuthResponse response = authService.login(request);
        
        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        verify(authenticationManager, times(1)).authenticate(any());
    }
}
