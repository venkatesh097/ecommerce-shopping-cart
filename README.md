# E-Commerce Shopping Cart Backend

A modular Spring Boot backend service for the E-Commerce Shopping Cart college project. Built for Java 17 and 21 compatibility using Maven, Spring Data JPA, PostgreSQL, and JUnit 5.

---

## 🛠️ Tech Stack & Architecture

- **Java**: Java 17 / 21
- **Framework**: Spring Boot 3.3.5
- **Web**: Spring MVC (RESTful API endpoints)
- **Data Persistence**: Spring Data JPA / Hibernate ORM
- **Database**: PostgreSQL (with in-memory H2 test profile for isolated automated testing)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Testing**: JUnit 5, Mockito, AssertJ, Spring Boot Test

---

## 📁 Package Architecture

The project adheres to a clean layered architecture under `com.example.ecommerce`:

```
src/main/java/com/example/ecommerce/
├── config/       # Spring configuration classes (CORS, security, custom beans)
├── controller/   # REST API controllers
├── dto/          # Request & response data transfer objects
├── entity/       # JPA entities / ORM domain models
├── exception/    # Custom exceptions & global exception handlers (@ControllerAdvice)
├── repository/   # Spring Data JPA repositories
├── service/      # Service interfaces and business logic implementations
└── EcommerceApplication.java # Spring Boot application entry point

src/main/resources/
└── application.properties # Main application properties driven by environment variables

src/test/
├── java/com/example/ecommerce/EcommerceApplicationTests.java
└── resources/application.properties # Isolated H2 in-memory test configuration
```

---

## ⚙️ Environment Variables & Database Configuration

Passwords and sensitive database credentials must **never** be hard-coded. The application reads configuration from environment variables with sensible defaults for local development:

| Variable | Description | Default Value |
|---|---|---|
| `DB_URL` | Full JDBC connection URL | `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}` |
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database schema name | `ecommerce_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database user password | *(empty)* |
| `SERVER_PORT` | Embedded server port | `8080` |
| `JPA_DDL_AUTO` | Hibernate schema management | `validate` |
| `JPA_SHOW_SQL` | Hibernate SQL logging | `false` |

---

## 🚀 Building & Running

### Prerequisites
- JDK 17 or JDK 21+ installed
- PostgreSQL 14+ (or set environment variables pointing to PostgreSQL instance)
- Maven 3.8+ (or use the provided Maven wrapper `./mvnw`)

### Build
To compile and package the project:
```bash
./mvnw clean package
# or
mvn clean package
```

### Run Tests
To run unit and integration tests (uses isolated in-memory H2 configuration):
```bash
./mvnw test
# or
mvn test
```

### Run the Application
```bash
# Set your environment variables
export DB_USERNAME=postgres
export DB_PASSWORD=your_secure_password
export DB_NAME=ecommerce_db

# Run with Spring Boot plugin
./mvnw spring-boot:run

# Or run the packaged jar
java -jar target/ecommerce-backend-0.0.1-SNAPSHOT.jar
```
