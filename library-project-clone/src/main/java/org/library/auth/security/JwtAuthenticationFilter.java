package org.library.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.library.user.model.User;
import org.library.user.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter
{
  private final JwtTokenProvider tokenProvider;
  private final UserRepository   userRepository;

  public JwtAuthenticationFilter(
    JwtTokenProvider tokenProvider, UserRepository userRepository )
  {
    this.tokenProvider = tokenProvider;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request, HttpServletResponse response,
    FilterChain filterChain ) throws ServletException, IOException
  {
    String token = null;

    // Retrieve token from Authorization header
    String bearerToken = request.getHeader( "Authorization" );
    if ( bearerToken != null && bearerToken.startsWith( "Bearer " ) ) {
      token = bearerToken.substring( 7 );
    }

    // Alternatively, retrieve token from HttpOnly cookie
    if ( token == null && request.getCookies() != null ) {
      token = Arrays.stream( request.getCookies() )
                    .filter( cookie -> "refreshToken".equals( cookie.getName() ) )
                    .findFirst()
                    .map( Cookie::getValue )
                    .orElse( null );
    }

    if ( token != null && tokenProvider.validateToken( token ) ) {
      String userId = tokenProvider.getUserIdFromToken( token );
      User user = userRepository.findById( userId ).orElse( null );
      if ( user != null ) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          user,
          null,
          user.getAuthorities()
        );
        authentication.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );
        SecurityContextHolder.getContext().setAuthentication( authentication );
      }
    }

    filterChain.doFilter(
      request,
      response
    );
  }
}
