# telegram-messenger-backend

Бэкенд MVP под [Telegram Bot API](https://core.telegram.org/bots/api) на **Spring Boot 3.5** и **Java 21**: REST, JPA, валидация, Actuator, in-memory **H2** для локальной разработки.

Бот в Telegram: [@vokals_bot](https://t.me/vokals_bot).

## Требования

- **JDK 21** или новее
- **GNU Make** (опционально; удобные цели в корневом `Makefile`)
- Установленный Maven **не обязателен** — в репозитории есть `./mvnw`

## Зависимости Spring Boot

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `h2` (runtime)

## Быстрый старт

Полная проверка (очистка, тесты, сборка):

```bash
make verify
# эквивалент: ./mvnw clean verify
```

Запуск приложения (порт по умолчанию **9090**, чтобы реже пересекаться с типичным **8080** у других приложений; профиль **`default`** — токен бота не обязателен):

```bash
make run
# эквивалент: ./mvnw spring-boot:run
```

Свой порт при необходимости:

```bash
SERVER_PORT=8080 make run
```

Справка по целям Make:

```bash
make help
```

## Makefile

| Цель | Описание |
|------|----------|
| `make help` | Краткая справка |
| `make verify` | `clean` + тесты + упаковка (как типичный CI) |
| `make test` | Только тесты |
| `make package` | Сборка JAR без тестов |
| `make clean` | Очистка `target/` |
| `make run` | Запуск с профилем `default` |
| `make run-local` | Запуск с `SPRING_PROFILES_ACTIVE=local` (см. ниже) |

## Токен бота (не коммитить)

Токен выдаёт [@BotFather](https://t.me/BotFather) после `/newbot`. Не кладите его в git.

### Вариант 1: переменная окружения

```bash
export TELEGRAM_BOT_TOKEN='ваш_токен'
make run
```

### Вариант 2: профиль `local` и `application-local.yml`

Файл `src/main/resources/application-local.yml` указан в `.gitignore`.

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# вставьте токен в application-local.yml

make run-local
```

Альтернатива без Make:

```bash
export SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run
```

или

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Если заданы и `TELEGRAM_BOT_TOKEN`, и `telegram.bot.token` в YAML, обычно выигрывает переменная окружения. Для варианта только с файлом не экспортируйте `TELEGRAM_BOT_TOKEN`.

## HTTP API

| Метод | Путь | Описание |
|--------|------|----------|
| `POST` | `/api/telegram/webhook` | Заготовка под `setWebhook`: тело запроса от Telegram (разбор JSON — по мере развития MVP). |

В проде webhook доступен только по **HTTPS**; URL регистрируется методом [`setWebhook`](https://core.telegram.org/bots/api#setwebhook).

## Actuator

Базовый путь: `http://localhost:9090/actuator` (если не меняли `SERVER_PORT`).

Подключены endpoints: `health`, `info`, `metrics` (настройка в `application.yml`).

```bash
curl -s http://localhost:9090/actuator/health
```

## H2 Console

Для разработки: [http://localhost:9090/h2-console](http://localhost:9090/h2-console).

- JDBC URL: `jdbc:h2:mem:telegram`
- Пользователь: `sa`
- Пароль: пустой

## Устранение неполадок

### `BindException` / «Адрес уже используется» при `spring-boot:run`

Указанный порт (по умолчанию **9090**) уже занят — например, второй экземпляр этого приложения или другой сервис.

1. Укажите свободный порт: `SERVER_PORT=9100 make run`.
2. Либо найдите и остановите процесс: `ss -tlnp | grep ':9090 '` или `lsof -i :9090` (подставьте свой порт).

### Maven пишет только `Process terminated with exit code: 1`

Перезапустите с логом приложения: `./mvnw spring-boot:run -e` — внизу будет цепочка причин (часто та же занятость порта).

## Передача заказчику

Передайте репозиторий, этот README и шаблон `application-local.yml.example`. Реальный токен и прод-настройки заказчик задаёт у себя. Бота в Telegram лучше создавать на аккаунте заказчика в BotFather, чтобы токен и владение ботом оставались у них.
