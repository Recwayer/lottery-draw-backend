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
    return HibernateExecutor.executeWithoutTransaction(
        sessionFactory,
        session ->
            session
                .createQuery("from User u where lower(u.email) = :email", User.class)
                .setParameter("email", email.toLowerCase())
                .setMaxResults(1)
                .uniqueResultOptional());
  }

  public boolean existsByEmail(String email) {
    return HibernateExecutor.executeWithoutTransaction(
        sessionFactory,
        session ->
            session
                    .createQuery(
                        "select count(u.id) from User u where lower(u.email) = :email", Long.class)
                    .setParameter("email", email.toLowerCase())
                    .uniqueResult()
                > 0);
  }
}
