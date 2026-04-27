package ru.lottery.service;

import ru.lottery.dto.user.UserResponse;
import ru.lottery.exception.NotFoundApiException;
import ru.lottery.mapper.UserMapper;
import ru.lottery.model.User;
import ru.lottery.repository.UserRepository;
import ru.lottery.security.AuthenticatedUser;

public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserResponse getCurrentUser(AuthenticatedUser authenticatedUser) {
    User user =
        userRepository
            .findById(authenticatedUser.userId())
            .orElseThrow(() -> new NotFoundApiException("User not found"));
    return UserMapper.toResponse(user);
  }
}
