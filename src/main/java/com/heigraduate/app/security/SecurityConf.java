package com.heigraduate.app.security;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.heigraduate.app.security.exception.RestAccessDeniedHandler;
import com.heigraduate.app.security.exception.RestAuthenticationEntryPoint;
import com.heigraduate.app.security.filter.BearerAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConf {

    private final RestAuthenticationEntryPoint entryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, BearerAuthFilter bearerAuthFilter) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
                .exceptionHandling(
                        e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/login")
                        .permitAll()
                        .requestMatchers("/ping", "/health/**")
                        .permitAll()
                        .requestMatchers("/api/users", "/api/users/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(bearerAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}