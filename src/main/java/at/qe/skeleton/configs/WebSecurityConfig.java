package at.qe.skeleton.configs;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring configuration for web security.
 * <p>
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    @Value("${cors.allowed-methods}")
    private String allowedMethods;
    @Value("${cors.allowed-headers}")
    private String allowedHeaders;
    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;
    @Value("${cors.max-age}")
    private long maxAge;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           TokenAuthenticationFilter tokenAuthenticationFilter)
            throws Exception {

        try {

            http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable).headers(headers -> headers.frameOptions(
                        FrameOptionsConfig::sameOrigin)) // needed for H2 console
                .authorizeHttpRequests(
                        authorize -> authorize.requestMatchers("/h2-console/**").permitAll()
                                              .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                              .requestMatchers("/error/**").permitAll()
                                              .requestMatchers("/api/admin/**")
                                              .hasAnyAuthority("ADMIN").requestMatchers("/api/**")
                                              .authenticated()
                                              // Allow access to the login endpoint
                                              .requestMatchers("/authentication/**").permitAll()
                                              .anyRequest().authenticated())
                // Add the token authentication filter before the UsernamePasswordAuthenticationFilter
                .addFilterBefore(tokenAuthenticationFilter,
                                 org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)

                .sessionManagement(
                        // no session creation, we use JWT
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception -> exception.accessDeniedHandler(
                        (request, response, accessDeniedException) -> response.setStatus(
                                HttpStatus.FORBIDDEN.value())).authenticationEntryPoint(
                        (request, response, authException) -> response.setStatus(
                                HttpStatus.UNAUTHORIZED.value())));

            return http.build();
        } catch (Exception ex) {
            throw new BeanCreationException("Wrong spring security configuration", ex);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> originsList = Arrays.stream(allowedOrigins.split(",")).map(String::trim)
                                         .toList();
        List<String> methodsList = Arrays.stream(allowedMethods.split(",")).map(String::trim)
                                         .toList();
        List<String> headersList = Arrays.stream(allowedHeaders.split(",")).map(String::trim)
                                         .toList();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(originsList);
        config.setAllowedMethods(methodsList);
        config.setAllowedHeaders(headersList);
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
