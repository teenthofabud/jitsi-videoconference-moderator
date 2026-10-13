package com.teenthofabud.videoconference.mdr.configuration;

import com.auth0.spring.boot.Auth0AuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {

    @Bean
    CorsConfigurationSource outlookCorsConfigurationSource() {
        CorsConfiguration outlookCors = new CorsConfiguration();
        outlookCors.setAllowedOriginPatterns(List.of("*"));
        outlookCors.setAllowedMethods(List.of("GET", "POST", "DELETE", "OPTIONS"));
        outlookCors.setAllowedHeaders(List.of("Content-Type"));
        outlookCors.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/outlook/conferences/**", outlookCors);
        return source;
    }

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http, Auth0AuthenticationFilter authFilter,
                                    CorsConfigurationSource outlookCorsConfigurationSource) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(outlookCorsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs*/**"
                        ).permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/users/preview").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servers/default").permitAll()
                        .requestMatchers("/api/outlook/conferences/**").permitAll()
                        .anyRequest().authenticated()

                )
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
