package ru.lottery.security;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ru.lottery.exception.ApiException;
import ru.lottery.exception.UnauthorizedException;
import ru.lottery.web.ErrorResponseWriter;

public class AuthFilter implements Filter {
  public static final String AUTHENTICATED_USER_ATTRIBUTE =
      AuthenticatedUser.class.getName() + ".requestAttribute";

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;

  public AuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest httpRequest)
        || !(response instanceof HttpServletResponse httpResponse)) {
      chain.doFilter(request, response);
      return;
    }

    try {
      AuthenticatedUser authenticatedUser = authenticate(httpRequest);
      httpRequest.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser);
      chain.doFilter(request, response);
    } catch (ApiException e) {
      ErrorResponseWriter.write(httpResponse, e.getHttpStatus(), e.getErrorCode(), e.getMessage());
    }
  }

  public static AuthenticatedUser getAuthenticatedUser(HttpServletRequest request) {
    Object attribute = request.getAttribute(AUTHENTICATED_USER_ATTRIBUTE);
    if (attribute instanceof AuthenticatedUser authenticatedUser) {
      return authenticatedUser;
    }
    throw new UnauthorizedException("Authentication is required");
  }

  private AuthenticatedUser authenticate(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization == null || authorization.isBlank()) {
      throw new UnauthorizedException("Authorization header is required");
    }
    if (!authorization.startsWith(BEARER_PREFIX)) {
      throw new UnauthorizedException("Authorization header must use Bearer token");
    }

    String token = authorization.substring(BEARER_PREFIX.length()).trim();
    if (token.isBlank()) {
      throw new UnauthorizedException("Bearer token is required");
    }
    return jwtService.parseToken(token);
  }
}
