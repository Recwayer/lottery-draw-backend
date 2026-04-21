package ru.lottery.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HibernateExecutor {

  public static <T> T execute(SessionFactory factory, HibernateFunction<T> fn) {

    validate(factory, fn);

    long start = System.currentTimeMillis();
    Transaction transaction = null;

    try (Session session = factory.openSession()) {

      log.debug("OPEN transaction session");

      transaction = session.beginTransaction();

      T result = fn.apply(session);

      transaction.commit();

      log.debug("transaction SUCCESS in {} ms", System.currentTimeMillis() - start);

      return result;

    } catch (Exception e) {

      rollback(transaction);

      log.error("transaction FAILED in {} ms", System.currentTimeMillis() - start, e);

      throw new RuntimeException("Database transaction failed", e);
    }
  }

  public static <T> T executeWithoutTransaction(SessionFactory factory, HibernateFunction<T> fn) {

    validate(factory, fn);

    long start = System.currentTimeMillis();

    try (Session session = factory.openSession()) {

      log.debug("OPEN READ session");

      T result = fn.apply(session);

      log.debug("READ SUCCESS in {} ms", System.currentTimeMillis() - start);

      return result;

    } catch (Exception e) {
      log.error("READ FAILED in {} ms", System.currentTimeMillis() - start, e);
      throw new RuntimeException("Database read failed", e);
    }
  }

  private static void rollback(Transaction transaction) {
    if (transaction != null) {
      try {
        transaction.rollback();
        log.warn("Transaction rolled back");
      } catch (Exception ex) {
        log.error("Rollback failed", ex);
      }
    }
  }

  private static void validate(SessionFactory factory, HibernateFunction<?> fn) {
    if (factory == null) {
      throw new IllegalArgumentException("SessionFactory is null");
    }

    if (fn == null) {
      throw new IllegalArgumentException("Hibernate function is null");
    }
  }

  @FunctionalInterface
  public interface HibernateFunction<T> {
    T apply(Session session);
  }
}
