package ru.lottery.unit.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ru.lottery.util.JwtUtil;

import io.jsonwebtoken.Claims;

public class JwtAuthFilter extends HttpFilter {
  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    String authHeader = req.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      try {
        String token = authHeader.substring(7);
        Claims claims = JwtUtil.parseToken(token);
        req.setAttribute("userEmail", claims.getSubject());
        req.setAttribute("userRole", claims.get("role", String.class));
      } catch (Exception e) {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write("{\"error\":\"Invalid or expired token\"}");
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
