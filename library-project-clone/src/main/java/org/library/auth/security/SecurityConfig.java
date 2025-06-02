package org.library.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig
{
  private final JwtAuthenticationFilter jwtFilter;

  @Bean
  public SecurityFilterChain filterChain( HttpSecurity http ) throws Exception {
    return http.csrf( csrf -> csrf.disable() )
               .authorizeHttpRequests( auth -> auth.requestMatchers( "/api/auth/**" )
                                                   .permitAll()
                                                   .anyRequest()
                                                   .authenticated() )
               .addFilterBefore(
                 jwtFilter,
                 UsernamePasswordAuthenticationFilter.class
               )
               .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration configuration ) throws Exception
  {
    return configuration.getAuthenticationManager();
  }
}
