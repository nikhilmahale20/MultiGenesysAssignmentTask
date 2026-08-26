# Resource Booking API

A secure RESTful Resource Booking System built with Spring Boot, Java 17, Spring Security, JWT, and PostgreSQL.

## Features

- **JWT Authentication**: Stateless authentication using JSON Web Tokens.
- **Role-Based Access Control (RBAC)**: Supports `ADMIN` and `USER` roles.
- **Resource Management**: Admins have full CRUD access. Users have read-only access.
- **Reservation Management**: Users can create and view their own reservations. Admins have full CRUD access to all reservations.
- **Advanced Querying**: Filter reservations by status, min price, max price with support for pagination and sorting.
- **API Documentation**: Integrated Swagger/OpenAPI UI.

## Requirements

- Java 17+
- Maven 3.8+
- Docker and Docker Compose (Optional, for running PostgreSQL)

## Setup Instructions

### 1. Database Configuration

The application uses PostgreSQL. You can quickly spin up a local instance using Docker Compose:

```bash
docker-compose up -d
```

This will start a PostgreSQL instance on port `5432` with the database `resource_booking`, user `root`, and password `password`.

If you prefer using an existing database, update the properties in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/resource_booking
spring.datasource.username=root
spring.datasource.password=password
```

### 2. Environment Variables

The JWT secret and expiration are configured in `application.properties`. For production environments, it is recommended to override these via environment variables:

```bash
export JWT_SECRET=your_super_secret_secure_key_that_is_long_enough
export JWT_EXPIRATION=86400000
```

### 3. Build & Run

To build the application:
```bash
./mvnw clean install
```

To run the application:
```bash
./mvnw spring-boot:run
```

## API Documentation

Once the application is running, you can access the Swagger UI documentation at:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Seed Users

The database is automatically seeded with two test accounts on the first run:

- **Admin Account**: Username: `admin`, Password: `admin123`
- **User Account**: Username: `user`, Password: `user123`
