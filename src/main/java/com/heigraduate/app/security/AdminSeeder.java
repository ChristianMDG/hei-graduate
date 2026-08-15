package com.heigraduate.app.security;


import com.heigraduate.app.graduate.model.UserRole;
import com.heigraduate.app.graduate.repository.UserRepository;
import com.heigraduate.app.graduate.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${admin.seed.email:}")
    private String seedEmail;

    @Value("${admin.seed.password:}")
    private String seedPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (seedEmail.isBlank() || seedPassword.isBlank()) {
            log.debug("No ADMIN_SEED_EMAIL/ADMIN_SEED_PASSWORD configured, skipping admin seeding");
            return;
        }
        if (userRepository.findByEmail(seedEmail).isPresent()) {
            log.debug("Admin account {} already exists, skipping seeding", seedEmail);
            return;
        }

        userService.createUser(seedEmail, seedPassword, UserRole.ADMIN);
        log.info("Seeded initial ADMIN account: {}", seedEmail);
    }
}