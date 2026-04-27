package ru.lottery.mapper;

import ru.lottery.dto.auth.AuthUserResponse;
import ru.lottery.dto.user.UserResponse;
import ru.lottery.model.User;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {

  public UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt(), user.getUpdatedAt());
  }

  public AuthUserResponse toAuthResponse(User user) {
    return new AuthUserResponse(user.getId(), user.getEmail(), user.getRole());
  }
}
