package com.smarttourism.backend.admin.mapper;

import com.smarttourism.backend.admin.dto.UserResponse;
import com.smarttourism.backend.users.entity.User;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for {@link User} ↔ {@link UserResponse} conversions.
 *
 * <p>Validates: Requirement 9.7
 */
@Mapper
public interface UserMapper {

    /**
     * Maps a {@link User} entity to a {@link UserResponse} DTO.
     *
     * @param user the entity to map
     * @return the response DTO
     */
    UserResponse toResponse(User user);
}
