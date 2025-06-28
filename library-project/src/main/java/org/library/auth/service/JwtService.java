package org.library.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

  @Value( "${jwt.secret}" )
  private String secret;

  @Value( "${jwt.access-token-expiration}" )
  private long accessTokenExpiration;

  @Value( "${jwt.refresh-token-expiration}" )
  private long refreshTokenExpiration;

  private SecretKey getSigningKey () {
    return Keys.hmacShaKeyFor( secret.getBytes() );
  }

  public String generateAccessToken ( UUID userId, String role ) {
    return generateToken(
      userId,
      role,
      accessTokenExpiration
    );
  }

  public String generateRefreshToken ( UUID userId, String role ) {
    return generateToken(
      userId,
      role,
      refreshTokenExpiration
    );
  }

  private String generateToken ( UUID userId, String role, long expiration ) {
    return Jwts
      .builder()
      .setSubject( userId.toString() )
      .claim(
        "role",
        role
      )
      .setIssuedAt( new Date() )
      .setExpiration( new Date( System.currentTimeMillis() + expiration ) )
      .signWith(
        getSigningKey(),
        SignatureAlgorithm.HS256
      )
      .compact();
  }

  public UUID extractUserId ( String token ) {
    return UUID.fromString( extractClaim(
      token,
      Claims::getSubject
    ) );
  }

  public String extractRole ( String token ) {
    return extractClaim(
      token,
      claims -> claims.get(
        "role",
        String.class
      )
    );
  }

  public Date extractExpiration ( String token ) {
    return extractClaim(
      token,
      Claims::getExpiration
    );
  }

  public <T> T extractClaim ( String token, Function<Claims, T> claimsResolver ) {
    final Claims claims = extractAllClaims( token );
    return claimsResolver.apply( claims );
  }

  // Fixed method using the new API
  private Claims extractAllClaims ( String token ) {
    return Jwts.parser().setSigningKey( getSigningKey() ).build().parseClaimsJws( token ).getBody();
  }

  public Boolean isTokenExpired ( String token ) {
    return extractExpiration( token ).before( new Date() );
  }

  public Boolean validateToken ( String token, UUID userId ) {
    final UUID tokenUserId = extractUserId( token );
    return ( tokenUserId.equals( userId ) && !isTokenExpired( token ) );
  }
}