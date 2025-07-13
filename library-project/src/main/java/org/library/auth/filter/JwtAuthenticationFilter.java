package org.library.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.library.auth.service.JwtService;
import org.library.user.UserService;
import org.library.user.dto.UserResponseDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserService userService;

  @Override
  protected void doFilterInternal (
    HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
  ) throws ServletException, IOException {

    final String authHeader = request.getHeader( "Authorization" );
    final String jwt;
    final Long userId;

    if ( authHeader == null || !authHeader.startsWith( "Bearer " ) ) {
      filterChain.doFilter(
        request,
        response
      );
      return;
    }

    jwt = authHeader.substring( 7 );

    try {
      userId = jwtService.extractUserId( jwt );

      if ( userId != null && SecurityContextHolder.getContext().getAuthentication() == null ) {
        UserResponseDto user;
        try {
          user = userService.getById( userId );
        } catch ( Exception e ) {
          filterChain.doFilter(
            request,
            response
          );
          return;
        }

        if ( jwtService.validateToken(
          jwt,
          userId
        ) ) {
          List<SimpleGrantedAuthority> authorities = List.of( new SimpleGrantedAuthority(
            "ROLE_" + user.getRole() ) );

          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            user,
            null,
            authorities
          );
          authToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );
          SecurityContextHolder.getContext().setAuthentication( authToken );
        }
      }
    } catch ( Exception e ) {
      logger.error(
        "Error during token validation",
        e
      );
    }

    filterChain.doFilter(
      request,
      response
    );
  }
}
