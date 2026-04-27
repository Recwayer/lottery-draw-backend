package ru.lottery.repository;

import java.util.Optional;

import org.hibernate.SessionFactory;

import ru.lottery.model.User;
import ru.lottery.util.HibernateExecutor;

public class UserRepository extends AbstractRepository<User> {

  public UserRepository(SessionFactory sessionFactory) {
    super(sessionFactory, User.class);
  }

  public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(
        HibernateExecutor.executeWithoutTransaction(
            sessionFactory,
            session ->
                session
                    .createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult()));
  }
}
