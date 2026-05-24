package com.smarttourism.backend.admin.controller;

import com.smarttourism.backend.admin.dto.UpdateUserStatusRequest;
import com.smarttourism.backend.admin.dto.UserResponse;
import com.smarttourism.backend.admin.mapper.UserMapper;
import com.smarttourism.backend.admin.service.AdminUserService;
import com.smarttourism.backend.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for admin-level user management.
 *
 * <p>All endpoints require ADMIN role.
 *
 * <p>Validates: Requirements 9.7, 9.8
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserMapper userMapper;

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable pagination parameters (default: page 0, size 20, sort by createdAt desc)
     * @return page of users
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserResponse> users = adminUserService.getAllUsers(pageable)
                .map(userMapper::toResponse);
        return ResponseEntity.ok(users);
    }

    /**
     * Updates the active status of a user.
     *
     * <p>When a user is deactivated, they will not be able to authenticate.
     *
     * @param userId  the user ID
     * @param request the status update request
     * @return the updated user
     */
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        User updatedUser = adminUserService.updateUserStatus(userId, request.getActive());
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }
}
