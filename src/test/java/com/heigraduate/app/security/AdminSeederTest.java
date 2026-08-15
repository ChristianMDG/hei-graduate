package com.heigraduate.app.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import com.heigraduate.app.graduate.repository.UserRepository;
import com.heigraduate.app.graduate.service.UserService;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AdminSeederTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService userService = mock(UserService.class);
    private final AdminSeeder seeder = new AdminSeeder(userRepository, userService);

    @Test
    void does_nothing_when_no_seed_credentials_are_configured() throws Exception {
        setField(seeder, "seedEmail", "");
        setField(seeder, "seedPassword", "");

        seeder.run(null);

        verify(userService, never()).createUser(any(), any(), any());
    }

    @Test
    void does_nothing_when_the_admin_account_already_exists() throws Exception {
        setField(seeder, "seedEmail", "admin@hei.school");
        setField(seeder, "seedPassword", "Admin1234!");
        when(userRepository.findByEmail("admin@hei.school"))
                .thenReturn(Optional.of(mock(User.class)));

        seeder.run(null);

        verify(userService, never()).createUser(any(), any(), any());
    }

    @Test
    void creates_the_admin_account_when_configured_and_absent() throws Exception {
        setField(seeder, "seedEmail", "admin@hei.school");
        setField(seeder, "seedPassword", "Admin1234!");
        when(userRepository.findByEmail("admin@hei.school")).thenReturn(Optional.empty());

        seeder.run(null);

        verify(userService).createUser(eq("admin@hei.school"), eq("Admin1234!"), eq(UserRole.ADMIN));
    }

    private static void setField(Object target, String name, String value) throws Exception {
        Field field = AdminSeeder.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}