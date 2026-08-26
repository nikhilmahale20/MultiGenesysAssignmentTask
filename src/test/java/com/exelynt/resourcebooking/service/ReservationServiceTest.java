package com.exelynt.resourcebooking.service;

import com.exelynt.resourcebooking.entity.Reservation;
import com.exelynt.resourcebooking.entity.Resource;
import com.exelynt.resourcebooking.entity.Role;
import com.exelynt.resourcebooking.entity.User;
import com.exelynt.resourcebooking.exception.UnauthorizedAccessException;
import com.exelynt.resourcebooking.repository.ReservationRepository;
import com.exelynt.resourcebooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "password")
        );
    }

    @Test
    void testGetReservationById_UnauthorizedAccess() {
        // Setup current user
        User currentUser = User.builder().id(1L).username("user").role(Role.USER).build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(currentUser));
        
        // Setup another user's reservation
        User otherUser = User.builder().id(2L).username("other").role(Role.USER).build();
        Reservation reservation = Reservation.builder().id(100L).user(otherUser).resource(new Resource()).build();
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        
        // Assert throws UnauthorizedAccessException
        assertThrows(UnauthorizedAccessException.class, () -> reservationService.getReservationById(100L));
    }
}
