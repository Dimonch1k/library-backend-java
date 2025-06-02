package org.library.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.library.auth.dto.LoginDto;
import org.library.auth.dto.RegisterDto;
import org.library.auth.security.JwtTokenProvider;
import org.library.user.model.User;
import org.library.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping( "/api/auth" )
public class AuthController
{
  private final UserService userService;
  private final JwtTokenProvider tokenProvider;

  public AuthController(
    UserService userService, JwtTokenProvider tokenProvider )
  {
    this.userService = userService;
    this.tokenProvider = tokenProvider;
  }

  @PostMapping( "/register" )
  public ResponseEntity<?> register(
    @RequestBody
    RegisterDto registerDto, HttpServletResponse response )
  {
    User user = userService.create( registerDto );
    String accessToken = tokenProvider.generateAccessToken(
      user.getId(),
      user.getRole().name()
    );
    String refreshToken = tokenProvider.generateRefreshToken(
      user.getId(),
      user.getRole().name()
    );

    // Set refresh token in HttpOnly cookie
    Cookie refreshTokenCookie = new Cookie(
      "refreshToken",
      refreshToken
    );
    refreshTokenCookie.setHttpOnly( true );
    refreshTokenCookie.setSecure( true );
    refreshTokenCookie.setPath( "/" );
    refreshTokenCookie.setMaxAge( 7 * 24 * 60 * 60 ); // 7 days
    response.addCookie( refreshTokenCookie );

    return ResponseEntity.ok().body( Map.of(
      "accessToken",
      accessToken
    ) );
  }

  @PostMapping( "/login" )
  public ResponseEntity<?> login(
    @RequestBody
    LoginDto loginDto, HttpServletResponse response )
  {
    User user = userService.authenticate( loginDto );
    String accessToken = tokenProvider.generateAccessToken(
      user.getId(),
      user.getRole().name()
    );
    String refreshToken = tokenProvider.generateRefreshToken(
      user.getId(),
      user.getRole().name()
    );

    // Set refresh token in HttpOnly cookie
    Cookie refreshTokenCookie = new Cookie(
      "refreshToken",
      refreshToken
    );
    refreshTokenCookie.setHttpOnly( true );
    refreshTokenCookie.setSecure( true );
    refreshTokenCookie.setPath( "/" );
    refreshTokenCookie.setMaxAge( 7 * 24 * 60 * 60 ); // 7 days
    response.addCookie( refreshTokenCookie );

    return ResponseEntity.ok().body( Map.of(
      "accessToken",
      accessToken
    ) );
  }

  @PostMapping( "/refresh-token" )
  public ResponseEntity<?> refreshToken(
    @CookieValue( value = "refreshToken", required = false )
    String refreshToken )
  {
    if ( refreshToken != null && tokenProvider.validateToken( refreshToken ) ) {
      String userId = tokenProvider.getUserIdFromToken( refreshToken );
      String role = tokenProvider.getRoleFromToken( refreshToken );
      String newAccessToken = tokenProvider.generateAccessToken(
        userId,
        role
      );
      return ResponseEntity.ok().body( Map.of(
        "accessToken",
        newAccessToken
      ) );
    }
    return ResponseEntity.status( HttpStatus.UNAUTHORIZED )
                         .body( "Invalid refresh token" );
  }

  @PostMapping( "/logout" )
  public ResponseEntity<?> logout( HttpServletResponse response ) {
    // Invalidate refresh token cookie
    Cookie refreshTokenCookie = new Cookie(
      "refreshToken",
      null
    );
    refreshTokenCookie.setHttpOnly( true );
    refreshTokenCookie.setSecure( true );
    refreshTokenCookie.setPath( "/" );
    refreshTokenCookie.setMaxAge( 0 );
    response.addCookie( refreshTokenCookie );

    return ResponseEntity.ok().body( "Logged out successfully" );
  }
}