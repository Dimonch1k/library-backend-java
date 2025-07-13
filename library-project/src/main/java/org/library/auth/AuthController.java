package org.library.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.auth.dto.AuthResponseDto;
import org.library.auth.dto.LoginDto;
import org.library.auth.dto.RegisterDto;
import org.library.auth.dto.TokenResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@RestController
@RequestMapping( "/api/v1/auth" )
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Value( "${jwt.refresh-token-name}" )
  private String refreshTokenName;

  @PostMapping( "/register" )
  @ResponseStatus( HttpStatus.OK )
  public AuthResponseDto register (
    @Valid @RequestBody RegisterDto dto, HttpServletResponse response
  ) {
    AuthResponseDto authResponse = authService.register( dto );
    authService.addRefreshTokenToResponse(
      response,
      authResponse.getUser().getId(),
      authResponse.getUser().getRole()
    );
    return authResponse;
  }

  @PostMapping( "/login" )
  @ResponseStatus( HttpStatus.OK )
  public AuthResponseDto login (
    @Valid @RequestBody LoginDto dto, HttpServletResponse response
  ) {
    AuthResponseDto authResponse = authService.login( dto );
    authService.addRefreshTokenToResponse(
      response,
      authResponse.getUser().getId(),
      authResponse.getUser().getRole()
    );
    return authResponse;
  }

  @PostMapping( "/refresh" )
  @ResponseStatus( HttpStatus.OK )
  public TokenResponseDto refreshToken (
    HttpServletRequest request, HttpServletResponse response
  ) {
    Cookie[] cookies = request.getCookies();
    if ( cookies == null ) {
      authService.removeRefreshTokenFromResponse( response );
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Refresh token not found"
      );
    }

    String refreshToken = Arrays
      .stream( cookies )
      .filter( cookie -> refreshTokenName.equals( cookie.getName() ) )
      .map( Cookie::getValue )
      .findFirst()
      .orElse( null );

    if ( refreshToken == null ) {
      authService.removeRefreshTokenFromResponse( response );
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Refresh token not found"
      );
    }

    return authService.refreshToken( refreshToken );
  }

  @PostMapping( "/logout" )
  @ResponseStatus( HttpStatus.OK )
  public ResponseEntity<String> logout ( HttpServletResponse response ) {
    authService.removeRefreshTokenFromResponse( response );
    return ResponseEntity.ok( "Logged out successfully" );
  }

  @GetMapping( "/validate" )
  @ResponseStatus( HttpStatus.OK )
  public ResponseEntity<String> validateToken (
    @RequestHeader( "Authorization" ) String authorizationHeader
  ) {
    if ( authorizationHeader == null || !authorizationHeader.startsWith( "Bearer " ) ) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Invalid authorization header"
      );
    }

    String token = authorizationHeader.substring( 7 );

    try {
      if ( authService.isValidToken( token ) ) {
        return ResponseEntity.ok( "Token is valid" );
      } else {
        throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED,
          "Invalid token"
        );
      }
    } catch ( Exception e ) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Invalid token"
      );
    }
  }
}