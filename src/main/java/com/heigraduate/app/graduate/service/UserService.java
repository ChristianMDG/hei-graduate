package com.heigraduate.app.graduate.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import com.heigraduate.app.graduate.repository.UserRepository;
import com.heigraduate.app.graduate.validator.UserValidator;
import com.heigraduate.app.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User createUser(String email, String rawPassword, UserRole role) {
        userValidator.validateEmailIsUnique(email);
        User user =
                User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .active(true)
                        .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(UUID id, String email, UserRole role, boolean active) {
        User user = getUserById(id);
        userValidator.validateEmailIsUniqueForUpdate(email, id);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(active);
        return userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email " + email));
    }

    /** Verifies credentials, refreshes {@code lastLogin} and issues a bearer token. */
    public LoginResult login(String email, String rawPassword) {
        User user = loadUserByUsername(email);

        if (!user.isEnabled()) {
            throw new BadCredentialsException("This account has been deactivated");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String token = jwtService.generate(user);
        return new LoginResult(token, user);
    }

    public record LoginResult(String token, User user) {}
}