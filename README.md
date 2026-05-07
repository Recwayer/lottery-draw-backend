# Lottery Draw Backend

Backend сервис для лотерейных розыгрышей.

## Стек

- **Java 21**
- **Micronaut 4.10.x** (Netty)
- **Micronaut Data JDBC** + **HikariCP**
- **Micronaut Security** (JWT access + refresh, refresh persisted in DB)
- **Micronaut Validation**
- **Liquibase** (через `micronaut-liquibase`)
- **PostgreSQL 17**
- **BCrypt** (jBCrypt) для паролей
- **Lombok**
- **Logback**
- Сборка: **Gradle** + Micronaut Application Plugin (Shadow JAR)

## Сборка и запуск

### Локально

```bash
./gradlew run
```

### Fat JAR

```bash
./gradlew shadowJar
java -jar build/libs/lottery_draw_backend-1.0.0-all.jar
```

### Docker Compose

```bash
./gradlew shadowJar
docker compose up --build
```

## Health endpoint

Поднимается автоматически через `io.micronaut:micronaut-management`:

```
GET http://localhost:8080/health
```

Включает в себя проверку доступности базы данных (`jdbc` health indicator).

## Конфигурация

Вся конфигурация — в [src/main/resources/application.yml](src/main/resources/application.yml).
Значения подставляются из переменных окружения через `${ENV:default}`.

### Обязательные переменные

| Переменная   | Описание                              |
|--------------|---------------------------------------|
| `DB_URL`     | JDBC URL PostgreSQL                   |
| `DB_USER`    | Имя пользователя БД                   |
| `DB_PASSWORD`| Пароль                                |

### Опциональные переменные

| Переменная                     | По умолчанию  | Описание                                         |
|--------------------------------|---------------|--------------------------------------------------|
| `PORT`                         | `8080`        | HTTP-порт Micronaut                              |
| `DB_POOL_SIZE`                 | `10`          | HikariCP `maximum-pool-size`                     |
| `DB_POOL_MIN_IDLE`             | `2`           | HikariCP `minimum-idle`                          |
| `DB_POOL_IDLE_TIMEOUT`         | `30000`       | HikariCP `idle-timeout` (ms)                     |
| `DB_CONNECTION_TIMEOUT`        | `10000`       | HikariCP `connection-timeout` (ms)               |
| `DB_DEFAULT_SCHEMA`            | `public`      | Схема приложения (используется и Liquibase как `default-schema`) |
| `DB_MIGRATION_SCHEMA`          | `migration`   | Схема для метаданных Liquibase (`liquibase-schema`) |
| `JWT_SECRET`                   | dev-stub      | Секрет для подписи access JWT (HS256), **минимум 32 символа** |
| `JWT_REFRESH_SECRET`           | dev-stub      | Секрет для генерации refresh-токена (signed JWT), **минимум 32 символа** |
| `JWT_ACCESS_TTL_SECONDS`       | `900`         | TTL access-токена в секундах (по умолчанию 15 минут) |
| `JWT_REFRESH_TTL_SECONDS`      | `604800`      | TTL refresh-токена в секундах (по умолчанию 7 дней) |

## API

### Аутентификация

```bash
# Регистрация (анонимно)
curl -X POST http://localhost:8080/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"secret12"}'

# Логин — встроенный контроллер Micronaut Security
curl -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice@example.com","password":"secret12"}'
# => { "access_token": "...", "refresh_token": "...", "token_type": "Bearer", "expires_in": 900 }

# Обновление пары токенов
curl -X POST http://localhost:8080/oauth/access_token \
  -H 'Content-Type: application/json' \
  -d '{"grant_type":"refresh_token","refresh_token":"<refresh>"}'

# Логаут (ревокает refresh-token в БД)
curl -X POST http://localhost:8080/logout \
  -H 'Authorization: Bearer <access>' \
  -H 'Content-Type: application/json' \
  -d '{"refresh_token":"<refresh>"}'
```

### Тиражи (admin)

```bash
# Создать тираж (lotteryTypeId — UUID типа лотереи, см. /lottery-types)
curl -X POST http://localhost:8080/admin/draws \
  -H 'Authorization: Bearer <admin-access>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Weekly #42","drawDate":"2030-01-01T20:00:00","lotteryTypeId":"00000000-0000-0000-0000-00000000c649"}'

# Запустить продажи (CREATED -> ACTIVE)
curl -X POST http://localhost:8080/admin/draws/<id>/start \
  -H 'Authorization: Bearer <admin-access>'

# Провести розыгрыш (ACTIVE -> FINISHED + проставить WIN/LOSE билетам)
curl -X POST http://localhost:8080/admin/draws/<id>/run \
  -H 'Authorization: Bearer <admin-access>'

# Отменить тираж (CREATED|ACTIVE -> CANCELED, связанные билеты тоже CANCELED)
curl -X POST http://localhost:8080/admin/draws/<id>/cancel \
  -H 'Authorization: Bearer <admin-access>'
```

### Тиражи и билеты (user)

```bash
# Активные тиражи
curl http://localhost:8080/draws/active \
  -H 'Authorization: Bearer <access>'

# Купить билет (числа генерит сервер)
curl -X POST http://localhost:8080/draws/<id>/tickets \
  -H 'Authorization: Bearer <access>'

# Мои билеты
curl http://localhost:8080/tickets/me \
  -H 'Authorization: Bearer <access>'

# Один билет
curl http://localhost:8080/tickets/<ticketId> \
  -H 'Authorization: Bearer <access>'
```

## Дополнительные эндпоинты

### Типы лотерей

```bash
# Список типов (любой авторизованный)
curl http://localhost:8080/lottery-types \
  -H 'Authorization: Bearer <access>'

# Создать новый тип (admin)
curl -X POST http://localhost:8080/admin/lottery-types \
  -H 'Authorization: Bearer <admin-access>' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Mini 5/36","poolMin":1,"poolMax":36,"picks":5}'
```

В сиде уже создан `Classic 6/49` с фиксированным id `00000000-0000-0000-0000-00000000c649`,
все существующие тиражи привязаны к нему.

### Отчёты по завершённым тиражам

```bash
# JSON (по умолчанию)
curl 'http://localhost:8080/admin/reports/draws' \
  -H 'Authorization: Bearer <admin-access>'

# С фильтрами по периоду и типу лотереи
curl 'http://localhost:8080/admin/reports/draws?from=2026-01-01T00:00:00&to=2026-12-31T23:59:59&type=00000000-0000-0000-0000-00000000c649' \
  -H 'Authorization: Bearer <admin-access>'

# CSV
curl 'http://localhost:8080/admin/reports/draws?format=csv' \
  -H 'Authorization: Bearer <admin-access>' -o draws.csv
```

Колонки CSV: `drawId,lotteryType,name,drawDate,winningNumbers,totalTickets,winners`.

### История пользователя

```bash
# Все события (с пагинацией)
curl 'http://localhost:8080/me/history?page=0&size=20' \
  -H 'Authorization: Bearer <access>'

# С фильтром по типам (повторяемый параметр)
curl 'http://localhost:8080/me/history?type=BUY_TICKET&type=TICKET_WIN' \
  -H 'Authorization: Bearer <access>'
```

Возможные `type`: `REGISTER`, `LOGIN`, `LOGOUT`, `BUY_TICKET`, `TICKET_WIN`,
`TICKET_LOSE`, `TICKET_REFUND`, `DRAW_CANCELED`, `NOTIFICATION_SENT`.

### Уведомления (SSE)

```bash
# Long-lived поток уведомлений о результатах тиража и отменах
curl -N -H 'Authorization: Bearer <access>' http://localhost:8080/notifications/stream

# Последние NOTIFICATION_SENT события (для пропущенных при оффлайне)
curl 'http://localhost:8080/notifications/recent?limit=20' \
  -H 'Authorization: Bearer <access>'
```

Каждое сообщение SSE — JSON `NotificationPayload`:

```json
{
  "drawId": "...",
  "ticketId": "...",
  "drawName": "Weekly #42",
  "lotteryType": "Classic 6/49",
  "numbers": "3,7,12,18,24,40",
  "winningNumbers": "1,7,12,33,41,49",
  "status": "LOSE",
  "message": "К сожалению, ваш билет не выиграл."
}
```

### Создание ADMIN

Регистрация открывает только роль `USER`. Чтобы получить ADMIN-токен,
обновите роль в БД:

```sql
update "user" set role = 'ADMIN' where email = 'alice@example.com';
```

## База данных

- Миграции лежат в [src/main/resources/db/changelog/](src/main/resources/db/changelog/).
- Master changelog — [src/main/resources/db/changelog-master.yaml](src/main/resources/db/changelog-master.yaml).
- Стартовый init-скрипт PostgreSQL ([scripts/init-scripts/create-schema_migration.sql](scripts/init-scripts/create-schema_migration.sql)) создаёт схему `migration`, в которой Liquibase хранит свои таблицы (`DATABASECHANGELOG`, `DATABASECHANGELOGLOCK`).
- Миграции применяются автоматически при старте приложения благодаря `micronaut-liquibase`.
