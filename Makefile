.PHONY: help clean test verify package run run-local

MVN := ./mvnw

help:
	@echo "Targets:"
	@echo "  make verify     — clean + tests + package (как CI)"
	@echo "  make test       — только тесты"
	@echo "  make package    — собрать JAR без тестов"
	@echo "  make clean      — mvn clean"
	@echo "  make run        — spring-boot:run (default: БД из SPRING_DATASOURCE_URL*, порт см. SERVER_PORT)"
	@echo "  make run-local  — spring-boot:run с профилем local (H2 in-memory, h2-console, ddl-auto: update)"
	@echo "  другой порт: SERVER_PORT=8080 make run"

clean:
	$(MVN) -q clean

test:
	$(MVN) -q test

verify:
	$(MVN) -q clean verify

package:
	$(MVN) -q package -DskipTests

run:
	$(MVN) spring-boot:run

run-local:
	SPRING_PROFILES_ACTIVE=local $(MVN) spring-boot:run
