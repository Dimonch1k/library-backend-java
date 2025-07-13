package org.library.config;

import lombok.RequiredArgsConstructor;
import org.library.auth.enums.Role;
import org.library.auth.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity( prePostEnabled = true )
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain (
    HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter
    // inject here instead of via field
  ) throws Exception {
    http
      .csrf( AbstractHttpConfigurer::disable )
      .cors( cors -> cors.configurationSource( corsConfigurationSource() ) )
      .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
      .authorizeHttpRequests( authz -> authz
        .requestMatchers( "/api/v1/auth/**" )
        .permitAll()
        .requestMatchers(
          HttpMethod.GET,
          "/api/v1/book/**"
        )
        .permitAll()
        .requestMatchers(
          HttpMethod.GET,
          "/api/v1/author/**"
        )
        .permitAll()
        .requestMatchers(
          HttpMethod.POST,
          "/api/v1/book/**"
        )
        .hasRole( Role.ADMIN.name() )
        .requestMatchers(
          HttpMethod.PUT,
          "/api/v1/book/**"
        )
        .hasRole( Role.ADMIN.name() )
        .requestMatchers(
          HttpMethod.PATCH,
          "/api/v1/book/**"
        )
        .hasRole( Role.ADMIN.name() )
        .requestMatchers(
          HttpMethod.DELETE,
          "/api/v1/book/**"
        )
        .hasRole( Role.ADMIN.name() )
        .requestMatchers(
          HttpMethod.POST,
          "/api/v1/author/**"
        )
        .hasRole( Role.ADMIN.name() )
        .requestMatchers(
          HttpMethod.PUT,
          "/api/v1/author/**"
        )
        .hasRole( Role.ADMIN.name() )
        .requestMatchers(
          HttpMethod.PATCH,
          "/api/v1/author/**"
        )
        .hasRole( Role.ADMIN.name() )
        .requestMatchers(
          HttpMethod.DELETE,
          "/api/v1/author/**"
        )
        .hasRole( Role.ADMIN.name() )
        // Admin-only order endpoints
        .requestMatchers(
          HttpMethod.GET,
          "/api/v1/order"
        )
        .hasRole( Role.ADMIN.name() )
        .requestMatchers(
          HttpMethod.DELETE,
          "/api/v1/order/**"
        )
        .hasRole( Role.ADMIN.name() )
        // Authenticated user order endpoints
        .requestMatchers(
          HttpMethod.POST,
          "/api/v1/order/borrow/**"
        )
        .authenticated()
        .requestMatchers(
          HttpMethod.PATCH,
          "/api/v1/order/return/**"
        )
        .authenticated()
        .requestMatchers(
          HttpMethod.PATCH,
          "/api/v1/order/cancel/**"
        )
        .authenticated()
        .requestMatchers(
          HttpMethod.GET,
          "/api/v1/order/my-orders/**"
        )
        .authenticated()
        .requestMatchers( "/api/v1/user/**" )
        .authenticated()
        .anyRequest()
        .authenticated() )
      .addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
      );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder () {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource () {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns( Arrays.asList( "*" ) );
    configuration.setAllowedMethods( Arrays.asList(
      "GET",
      "POST",
      "PUT",
      "PATCH",
      "DELETE",
      "OPTIONS"
    ) );
    configuration.setAllowedHeaders( Arrays.asList( "*" ) );
    configuration.setAllowCredentials( true );

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration(
      "/**",
      configuration
    );
    return source;
  }
}