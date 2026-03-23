# telegram-messenger-backend

Бэкенд MVP под Telegram Bot API на **Spring Boot 3.5** и **Java 21**: REST, JPA, валидация, Actuator, in-memory **H2** для локальной разработки.

## Требования

- JDK 21+ (для сборки подойдёт и более новый LTS, если Maven настроен корректно)
- Maven (либо встроенный wrapper: `./mvnw`)

## Зависимости приложения

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `h2` (runtime)

## Сборка и тесты

```bash
./mvnw clean verify
```

## Запуск

```bash
./mvnw spring-boot:run
```

По умолчанию активен профиль **`default`** (без обязательного токена бота — удобно для CI и тестов).

## Токен бота (не коммитить)

Токен выдаёт [@BotFather](https://t.me/BotFather) после создания бота (`/newbot`). Хранить его только вне репозитория.

### Вариант 1: переменная окружения

```bash
export TELEGRAM_BOT_TOKEN='ваш_токен'
./mvnw spring-boot:run
```

### Вариант 2: профиль `local` и `application-local.yml`

Файл `src/main/resources/application-local.yml` **в .gitignore** и не попадает в git.

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# вставьте токен в application-local.yml

export SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run
```

Либо одной командой:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Если заданы и `TELEGRAM_BOT_TOKEN`, и значение в YAML, приоритет может отдаться переменной окружения — для чистого варианта с файлом не задавайте `TELEGRAM_BOT_TOKEN`.

## HTTP API

| Метод | Путь | Описание |
|--------|------|-----------|
| `POST` | `/api/telegram/webhook` | Заготовка под `setWebhook`: приём POST с телом от Telegram (пока без разбора JSON). |

Для работы webhook в проде нужен публичный **HTTPS** URL; URL нужно зарегистрировать через [Bot API `setWebhook`](https://core.telegram.org/bots/api#setwebhook).

## Actuator

Базовый префикс: `/actuator`.

Открыты endpoints: `health`, `info`, `metrics` (см. `application.yml`).

Пример:

```bash
curl -s http://localhost:8080/actuator/health
```

## H2 Console

Включена для разработки: в браузере откройте `http://localhost:8080/h2-console`. JDBC URL из конфигурации: `jdbc:h2:mem:telegram`, пользователь `sa`, пароль пустой.

## Передача заказчику

Передайте исходный код, инструкцию по запуску и **шаблон** `application-local.yml.example`. Токен и прод-конфигурацию заказчик вносит сам; бот в Telegram лучше создавать на аккаунте заказчика в BotFather, чтобы владение токеном было у них.
