package org.library.config;

import org.library.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig
{
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig( JwtAuthenticationFilter jwtAuthenticationFilter ) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain( HttpSecurity http ) throws Exception {
    http.csrf()
        .disable()
        .authorizeHttpRequests( authorize -> authorize.requestMatchers( "/api/auth/**" )
                                                      .permitAll()
                                                      .requestMatchers( "/api/user/**" )
                                                      .authenticated()
                                                      .anyRequest()
                                                      .authenticated() )
        .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
        .addFilterBefore(
          jwtAuthenticationFilter,
          UsernamePasswordAuthenticationFilter.class
        );

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration authConfig ) throws Exception
  {
    return authConfig.getAuthenticationManager();
  }
}
