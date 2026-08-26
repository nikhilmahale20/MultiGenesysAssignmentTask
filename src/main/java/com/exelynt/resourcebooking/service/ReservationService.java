package com.exelynt.resourcebooking.service;

import com.exelynt.resourcebooking.dto.ReservationRequest;
import com.exelynt.resourcebooking.dto.ReservationResponse;
import com.exelynt.resourcebooking.entity.Reservation;
import com.exelynt.resourcebooking.entity.ReservationStatus;
import com.exelynt.resourcebooking.entity.Resource;
import com.exelynt.resourcebooking.entity.Role;
import com.exelynt.resourcebooking.entity.User;
import com.exelynt.resourcebooking.exception.ResourceNotFoundException;
import com.exelynt.resourcebooking.exception.UnauthorizedAccessException;
import com.exelynt.resourcebooking.repository.ReservationRepository;
import com.exelynt.resourcebooking.repository.ResourceRepository;
import com.exelynt.resourcebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public Page<ReservationResponse> getReservations(ReservationStatus status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        User currentUser = getCurrentUser();
        Long filterUserId = currentUser.getRole() == Role.ADMIN ? null : currentUser.getId();
        
        return reservationRepository.findWithFilters(filterUserId, status, minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = findReservationAndCheckAccess(id);
        return mapToResponse(reservation);
    }

    public ReservationResponse createReservation(ReservationRequest request) {
        User currentUser = getCurrentUser();
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));
        
        Reservation reservation = Reservation.builder()
                .resource(resource)
                .user(currentUser)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ReservationStatus.PENDING) // Always starts as PENDING for users
                .price(request.getPrice())
                .build();
                
        // Admin can override status during creation
        if (currentUser.getRole() == Role.ADMIN && request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }

        return mapToResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse updateReservationStatus(Long id, ReservationStatus newStatus) {
        // Based on prompt, only ADMIN handles full CRUD. But let's check roles.
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Only administrators can update reservations directly.");
        }
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        
        reservation.setStatus(newStatus);
        return mapToResponse(reservationRepository.save(reservation));
    }

    public void deleteReservation(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Only administrators can delete reservations.");
        }
        
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation not found with id: " + id);
        }
        
        reservationRepository.deleteById(id);
    }

    private Reservation findReservationAndCheckAccess(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
                
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You do not have permission to access this reservation.");
        }
        
        return reservation;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedAccessException("User not authenticated"));
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .resourceId(reservation.getResource().getId())
                .userId(reservation.getUser().getId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .price(reservation.getPrice())
                .build();
    }
}
