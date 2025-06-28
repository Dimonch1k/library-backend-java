package org.library.config;

import lombok.RequiredArgsConstructor;
import org.library.auth.resolver.CurrentUserArgumentResolver;
import org.library.logging.RequestLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
  private final CurrentUserArgumentResolver currentUserArgumentResolver;

  @Override
  public void addInterceptors ( InterceptorRegistry registry ) {
    registry.addInterceptor( new RequestLoggingInterceptor() );
  }

  @Override
  public void addArgumentResolvers ( List<HandlerMethodArgumentResolver> resolvers ) {
    resolvers.add( currentUserArgumentResolver );
  }
}
