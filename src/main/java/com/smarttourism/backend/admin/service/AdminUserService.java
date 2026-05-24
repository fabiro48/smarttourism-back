package com.smarttourism.backend.admin.service;

import com.smarttourism.backend.common.exception.ResourceNotFoundException;
import com.smarttourism.backend.users.entity.User;
import com.smarttourism.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for admin-level user management.
 *
 * <p>Provides user listing and status management capabilities.
 *
 * <p>Validates: Requirements 9.7, 9.8
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable pagination parameters
     * @return page of users
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Updates the active status of a user.
     *
     * <p>When a user is deactivated, they will not be able to authenticate
     * (verified in {@code UserDetailsServiceImpl}).
     *
     * @param userId the user ID
     * @param active the new active status
     * @return the updated user
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Transactional
    public User updateUserStatus(UUID userId, Boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setActive(active);
        return userRepository.save(user);
    }
}
