package ru.lottery.security;

import java.util.Arrays;

import ru.lottery.exception.ForbiddenException;
import ru.lottery.model.enums.Role;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RoleGuard {

  public void requireAny(AuthenticatedUser user, Role... allowedRoles) {
    if (user == null) {
      throw new ForbiddenException("Access is forbidden");
    }

    boolean allowed = Arrays.stream(allowedRoles).anyMatch(role -> role == user.role());
    if (!allowed) {
      throw new ForbiddenException("Access is forbidden");
    }
  }
}
