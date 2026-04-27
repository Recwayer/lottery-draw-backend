# Lottery Draw Backend

Backend MVP для сервиса лотерейных тиражей на Java 21 и Servlet API.
Проект использует Jetty, PostgreSQL, HikariCP, Liquibase, Hibernate,
Jackson, JWT, bcrypt, SLF4J/Logback, JUnit, Mockito, Testcontainers,
JaCoCo и Spotless.

## Переменные окружения

Обязательные:

- `DB_URL` - JDBC URL для PostgreSQL.
- `DB_USER` - пользователь PostgreSQL.
- `DB_PASSWORD` - пароль PostgreSQL.
- `JWT_SECRET` - HMAC-секрет для JWT, минимум 32 байта.

Необязательные:

- `JWT_EXPIRES_MINUTES` - время жизни access token в минутах, по умолчанию `60`.
- `PORT` - HTTP-порт, по умолчанию `8080`.
- `DB_POOL_SIZE` - максимальный размер пула Hikari, по умолчанию `10`.
- `DB_POOL_MIN_IDLE` - минимальное число idle-соединений Hikari, по умолчанию `2`.
- `DB_POOL_IDLE_TIMEOUT` - idle timeout Hikari в миллисекундах, по умолчанию `30000`.
- `DB_CONNECTION_TIMEOUT` - connection timeout Hikari в миллисекундах, по умолчанию `10000`.
- `LIQUIBASE_TARGET_SCHEMA` - целевая схема БД, по умолчанию `public`.
- `LIQUIBASE_MIGRATION_SCHEMA` - схема для служебных таблиц Liquibase, по умолчанию `migration`.
- `HIBERNATE_SHOW_SQL` - выводить SQL, по умолчанию `true`.
- `HIBERNATE_FORMAT_SQL` - форматировать SQL, по умолчанию `true`.
- `HIBERNATE_DDL_AUTO_STRATEGY` - стратегия проверки схемы Hibernate, по умолчанию `validate`.
- `HIBERNATE_DEFAULT_SCHEMA` - схема Hibernate по умолчанию, по умолчанию `public`.

## Запуск через Docker Compose

```bash
./gradlew clean shadowJar
docker compose up --build
```

API будет доступно по адресу `http://localhost:8080`.

## Локальный запуск через Gradle

Сначала запустите PostgreSQL, затем выполните:

```bash
DB_URL=jdbc:postgresql://localhost:5432/lottery \
DB_USER=admin \
DB_PASSWORD=password \
JWT_SECRET=local-development-jwt-secret-please-change \
JWT_EXPIRES_MINUTES=60 \
./gradlew run
```

Если shell не поддерживает inline-переменные окружения, задайте их заранее перед запуском Gradle.

## Примеры API

Регистрация:

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123!"}'
```

Логин:

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123!"}'
```

Получить текущего пользователя:

```bash
curl -i http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <accessToken>"
```

Получить билеты текущего пользователя:

```bash
curl -i http://localhost:8080/api/v1/users/me/tickets \
  -H "Authorization: Bearer <accessToken>"
```

## Проверка

```bash
./gradlew clean test
./gradlew spotlessCheck
./gradlew jacocoTestReport
./gradlew check
docker compose up --build
```

OpenAPI-спецификация: `docs/openapi.yaml`.
