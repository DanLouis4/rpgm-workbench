package com.danlouis.ttrpgmanager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;


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
                        "/videos/**",
                        "/favicon.ico")
                .permitAll()


                // ---------------------------------------------------------
                // Public Application Routes
                // ---------------------------------------------------------

                .requestMatchers(
                        "/",
                        "/about",
                        "/privacy-policy",
                        "/terms-of-use",
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
                        "/systems/*/delete",
                        "/systems/*/terms/**")
                .hasAnyRole("GAME_DESIGNER", "ADMINISTRATOR")


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
                // ---------------------------------------------------------
                // Access Denied Handling
                // ---------------------------------------------------------
                .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, deniedException) -> {

                        String contextPath = request.getContextPath();
                        String requestPath = request.getRequestURI();

                        // Game System creation attempted without required role
                        if ("GET".equals(request.getMethod())
                                && requestPath.equals(
                                contextPath + "/systems/create")) {

                        response.sendRedirect(
                                contextPath
                                + "/systems?access=game-designer-required");

                        return;
                        }

                        // Preserve normal 403 behavior for other denied requests
                        response.sendError(
                                HttpServletResponse.SC_FORBIDDEN);
                })
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