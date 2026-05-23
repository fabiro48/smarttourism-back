package com.smarttourism.backend.security;

import com.smarttourism.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation that loads users
 * from the database by their email address.
 *
 * <p>The {@link com.smarttourism.backend.users.entity.User} entity already
 * implements {@link UserDetails}, so it is returned directly without any
 * additional mapping.
 *
 * <p>Validates: Requirement 2.4
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their email address.
     *
     * @param email the email used as the username in this system
     * @return the matching {@link UserDetails} (never {@code null})
     * @throws UsernameNotFoundException if no user with the given email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No se encontró un usuario con el email: " + email));
    }
}
