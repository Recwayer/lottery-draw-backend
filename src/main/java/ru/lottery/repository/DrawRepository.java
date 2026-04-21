package ru.lottery.repository;

import org.hibernate.SessionFactory;

import ru.lottery.model.Draw;

public class DrawRepository extends AbstractRepository<Draw> {

  public DrawRepository(SessionFactory sessionFactory) {
    super(sessionFactory, Draw.class);
  }
}
