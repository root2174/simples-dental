.PHONY: build up

build:
	@echo "Building application and Docker images..."
	./mvnw clean package -DskipTests
	docker-compose build

up:
	@echo "Starting all services..."
	docker-compose up -d

test:
	@echo "Running tests..."
	./mvnw test

test-coverage:
	@echo "Running tests with coverage report..."
	./mvnw test jacoco:report
