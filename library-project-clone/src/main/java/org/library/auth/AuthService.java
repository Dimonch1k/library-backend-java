package org.library.auth;

import lombok.RequiredArgsConstructor;
import org.library.auth.dto.LoginDto;
import org.library.auth.dto.RegisterDto;
import org.library.auth.model.Role;
import org.library.auth.security.JwtTokenProvider;
import org.library.user.model.User;
import org.library.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService
{
  private final UserService           userService;
  private final PasswordEncoder       passwordEncoder;
  private final JwtTokenProvider      jwtProvider;
  private final AuthenticationManager authManager;

  public Map<String, String> register( RegisterDto request ) {
    if ( userService.getByEmail( request.getEmail() ) != null ) {
      throw new RuntimeException( "Email already in use" );
    }

    User user = User.builder()
                    .email( request.getEmail() )
                    .password( passwordEncoder.encode( request.getPassword() ) )
                    .role( Role.USER )
                    .build();

    userService.save( user );

    String token = jwtProvider.generateToken( user );

    return Map.of(
      "accessToken",
      token
    );
  }

  public Map<String, String> login( LoginDto request ) {
    authManager.authenticate( new UsernamePasswordAuthenticationToken(
      request.getEmail(),
      request.getPassword()
    ) );

    User user = userService.getByEmail( request.getEmail() )
                           .orElseThrow( () -> new RuntimeException( "Invalid credentials" ) );

    String token = jwtProvider.generateToken( user );

    return Map.of(
      "accessToken",
      token
    );
  }
}