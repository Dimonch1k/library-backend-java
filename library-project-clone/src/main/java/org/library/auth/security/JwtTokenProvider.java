package org.library.auth.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider
{
  @Value( "${jwt.secret}" )
  private String jwtSecret;
  @Value( "${jwt.accessTokenExpirationMs}" )
  private int    jwtAccessTokenExpirationMs;
  @Value( "${jwt.refreshTokenExpirationMs}" )
  private int    jwtRefreshTokenExpirationMs;

  public String generateAccessToken( String userId, String role ) {
    return Jwts.builder().setSubject( userId ).claim(
      "role",
      role
    ).setIssuedAt( new Date() ).setExpiration( new Date(
      System.currentTimeMillis() + jwtAccessTokenExpirationMs ) ).signWith(
      SignatureAlgorithm.HS512,
      jwtSecret
    ).compact();
  }

  public String generateRefreshToken( String userId, String role ) {
    return Jwts.builder().setSubject( userId ).claim(
      "role",
      role
    ).setIssuedAt( new Date() ).setExpiration( new Date(
      System.currentTimeMillis() + jwtRefreshTokenExpirationMs ) ).signWith(
      SignatureAlgorithm.HS512,
      jwtSecret
    ).compact();
  }

  public boolean validateToken( String token ) {
    try {
      Jwts.parser().setSigningKey( jwtSecret ).parseClaimsJws( token );
      return true;
    }
    catch ( JwtException | IllegalArgumentException e ) {
      // Log token validation error
    }
    return false;
  }

  public String getUserIdFromToken( String token ) {
    return Jwts.parser()
               .setSigningKey( jwtSecret )
               .parseClaimsJws( token )
               .getBody()
               .getSubject();
  }

  public String getRoleFromToken( String token ) {
    return (String) Jwts.parser()
                        .setSigningKey( jwtSecret )
                        .parseClaimsJws( token )
                        .getBody()
                        .get( "role" );
  }
}