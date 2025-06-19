package org.library.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class RequestLoggingInterceptor implements HandlerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger( RequestLoggingInterceptor.class );

  @Override
  public boolean preHandle (
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) {
    String method = request.getMethod();
    String path = request.getRequestURI();

    logger.info(
      "{} {}",
      method,
      path
    );
    return true;
  }
}
