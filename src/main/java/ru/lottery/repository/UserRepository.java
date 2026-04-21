package ru.lottery.repository;

import org.hibernate.SessionFactory;

import ru.lottery.model.User;

public class UserRepository extends AbstractRepository<User> {

  public UserRepository(SessionFactory sessionFactory) {
    super(sessionFactory, User.class);
  }
}
