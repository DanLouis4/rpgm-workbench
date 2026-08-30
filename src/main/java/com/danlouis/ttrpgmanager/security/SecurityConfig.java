package com.danlouis.ttrpgmanager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Configures application security and access rules for the TTRPG Manager.
 *
 * <p>Public-facing pages and static resources are available without
 * authentication. Protected application resources require an authenticated
 * user and are further restricted according to configured role and
 * authorization rules.</p>
 *
 * @author Daniel Louis
 */
@Configuration
public class SecurityConfig {

    /**
     * Defines the HTTP security rules used by the application.
     *
     * <p>The home page, public record-list pages, error page, and static
     * resources are accessible to unauthenticated users. Other requests
     * require authentication.</p>
     *
     * @param http the Spring Security HTTP configuration
     * @return the configured security filter chain
     * @throws Exception if the security configuration cannot be created
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
        .authorizeHttpRequests(auth -> auth

                // ---------------------------------------------------------
                // Static Resources
                // ---------------------------------------------------------

                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/videos/**")
                .permitAll()


                // ---------------------------------------------------------
                // Public Application Routes
                // ---------------------------------------------------------

                .requestMatchers(
                        "/",
                        "/register",
                        "/login",
                        "/error")
                .permitAll()


                // ---------------------------------------------------------
                // Game System Management
                // ---------------------------------------------------------

                .requestMatchers(
                        "/systems/create",
                        "/systems/*/edit",
                        "/systems/*/delete")
                .hasRole("GAME_DESIGNER")


                // ---------------------------------------------------------
                // Public Game System Browsing
                // ---------------------------------------------------------

                .requestMatchers(
                        HttpMethod.GET,
                        "/systems",
                        "/systems/*")
                .permitAll()


                // ---------------------------------------------------------
                // Other Public Module Browsing
                // ---------------------------------------------------------

                .requestMatchers(
                        HttpMethod.GET,
                        "/campaigns",
                        "/characters")
                .permitAll()


                // ---------------------------------------------------------
                // All Remaining Requests
                // ---------------------------------------------------------

                .anyRequest()
                .authenticated()
        )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?status=error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?status=logout")
                .permitAll()
            );

        return http.build();
    }

    /**
     * Provides a password encoder bean for hashing user passwords.
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}