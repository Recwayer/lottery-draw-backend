package ru.lottery.config;

import org.junit.jupiter.api.Test;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;

class LiquibaseChangelogTest {

  @Test
  void changelogIsValid() throws Exception {
    ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
    Database database =
        DatabaseFactory.getInstance()
            .openDatabase("offline:postgresql", null, null, null, resourceAccessor);

    try (Liquibase liquibase =
        new Liquibase("db/changelog-master.yaml", resourceAccessor, database)) {
      liquibase.validate();
    }
  }
}
