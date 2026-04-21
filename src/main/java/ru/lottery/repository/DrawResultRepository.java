package ru.lottery.repository;

import org.hibernate.SessionFactory;

import ru.lottery.model.DrawResult;

public class DrawResultRepository extends AbstractRepository<DrawResult> {

  public DrawResultRepository(SessionFactory sessionFactory) {
    super(sessionFactory, DrawResult.class);
  }
}
