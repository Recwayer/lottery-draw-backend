package ru.lottery.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import ru.lottery.util.constant.Env;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
  private static final SecretKey KEY =
      Keys.hmacShaKeyFor(EnvUtil.get(Env.JWT_SECRET.name()).getBytes(StandardCharsets.UTF_8));
  private static final long EXPIRATION_MS = 3_600_000; // 1 час

  public static String generateToken(String email, String role) {
    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
        .signWith(KEY, Jwts.SIG.HS256)
        .compact();
  }

  public static Claims parseToken(String token) {
    return Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
  }
}
