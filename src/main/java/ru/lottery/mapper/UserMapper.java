package ru.lottery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.lottery.model.User;
import ru.lottery.model.dto.RegisterRequest;

@Mapper(
    injectionStrategy = org.mapstruct.InjectionStrategy.CONSTRUCTOR,
    nullValueCheckStrategy = org.mapstruct.NullValueCheckStrategy.ALWAYS)
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "role", ignore = true)
  User toEntity(RegisterRequest request);
}
