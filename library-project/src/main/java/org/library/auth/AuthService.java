package org.library.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.library.auth.dto.AuthResponseDto;
import org.library.auth.dto.LoginDto;
import org.library.auth.dto.RegisterDto;
import org.library.auth.dto.TokenResponseDto;
import org.library.auth.service.JwtService;
import org.library.user.UserService;
import org.library.user.dto.UserResponseDto;
import org.library.user.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

  private final UserService userService;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Value( "${jwt.refresh-token-name}" )
  private String refreshTokenName;

  @Value( "${jwt.refresh-token-expiration}" )
  private long refreshTokenExpiration;

  public AuthResponseDto register ( RegisterDto dto ) {
    userService.checkUserDuplicateByEmail( dto.getEmail() );

    UserResponseDto user = userService.create( dto );

    String accessToken = jwtService.generateAccessToken(
      user.getId(),
      user.getRole()
    );

    return new AuthResponseDto(
      user,
      accessToken
    );
  }

  public AuthResponseDto login ( LoginDto dto ) {
    UserResponseDto user = validateUser( dto );

    String accessToken = jwtService.generateAccessToken(
      user.getId(),
      user.getRole()
    );

    return new AuthResponseDto(
      user,
      accessToken
    );
  }

  public TokenResponseDto refreshToken ( String refreshToken ) {
    try {
      Long userId = jwtService.extractUserId( refreshToken );
      String role = jwtService.extractRole( refreshToken );

      if ( jwtService.isTokenExpired( refreshToken ) ) {
        throw new ResponseStatusException(
          UNAUTHORIZED,
          "Refresh token expired"
        );
      }

      UserResponseDto user = userService.getById( userId );

      String newAccessToken = jwtService.generateAccessToken(
        userId,
        role
      );
      return new TokenResponseDto( newAccessToken );
    } catch ( Exception e ) {
      throw new ResponseStatusException(
        UNAUTHORIZED,
        "Invalid refresh token"
      );
    }
  }

  public void addRefreshTokenToResponse (
    HttpServletResponse response, Long userId, String role
  ) {
    String refreshToken = jwtService.generateRefreshToken(
      userId,
      role
    );

    Cookie cookie = new Cookie(
      refreshTokenName,
      refreshToken
    );
    cookie.setHttpOnly( true );
    cookie.setSecure( true );
    cookie.setPath( "/" );
    cookie.setMaxAge( (int) ( refreshTokenExpiration / 1000 ) );
    cookie.setAttribute(
      "SameSite",
      "None"
    );

    response.addCookie( cookie );
  }

  public void removeRefreshTokenFromResponse ( HttpServletResponse response ) {
    Cookie cookie = new Cookie(
      refreshTokenName,
      ""
    );
    cookie.setHttpOnly( true );
    cookie.setSecure( true );
    cookie.setPath( "/" );
    cookie.setMaxAge( 0 );
    cookie.setAttribute(
      "SameSite",
      "None"
    );

    response.addCookie( cookie );
  }

  public boolean isValidToken ( String token ) {
    try {
      Long userId = jwtService.extractUserId( token );
      return !jwtService.isTokenExpired( token ) && userId != null;
    } catch ( Exception e ) {
      return false;
    }
  }

  private UserResponseDto validateUser ( LoginDto dto ) {
    Optional<User> fullUserOptional = userService.getByEmailWithoutException( dto.getEmail() );

    if ( !fullUserOptional.isPresent() ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Invalid credentials"
      );
    }

    User fullUser = fullUserOptional.get();

    if ( !passwordEncoder.matches(
      dto.getPassword(),
      fullUser.getPassword()
    ) ) {
      throw new ResponseStatusException(
        BAD_REQUEST,
        "Invalid credentials"
      );
    }

    return userService.toResponseDto( fullUser );
  }
}