package ru.lottery.repository;

import java.util.Optional;
import java.util.UUID;

import org.hibernate.SessionFactory;

import ru.lottery.util.HibernateExecutor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractRepository<T> {

  protected final SessionFactory sessionFactory;
  private final Class<T> entityClass;

  public void save(T entity) {
    HibernateExecutor.execute(
        sessionFactory,
        session -> {
          session.persist(entity);
          return null;
        });
  }

  public void update(T entity) {
    HibernateExecutor.execute(
        sessionFactory,
        session -> {
          session.merge(entity);
          return null;
        });
  }

  public Optional<T> findById(UUID id) {
    return Optional.ofNullable(
        HibernateExecutor.executeWithoutTransaction(
            sessionFactory, session -> session.find(entityClass, id)));
  }

  public void delete(T entity) {
    HibernateExecutor.execute(
        sessionFactory,
        session -> {
          session.remove(entity);
          return null;
        });
  }
}
