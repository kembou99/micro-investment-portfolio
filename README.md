# Micro Investment Portfolio

Micro Investment Portfolio is a Spring Boot 3.5 application designed to help users manage micro-investments through automatic round-ups from their purchases.

## 🚀 Features

*   **User Management**: Create and manage user profiles.
*   **Account Management**: Support for multiple accounts per user with a default account system.
*   **Automatic Round-Ups**: Automatically calculates and invests the difference to the next euro/dollar for each purchase.
*   **Transaction History**: Paginated history of all purchases, investments, and withdrawals.
*   **Fee Deduction**: Background task to deduct monthly fees from active accounts.
*   **Tax Calculation**: Automatic 15% capital gains tax deduction on withdrawals.

## 🛠 Tech Stack

*   **Java 21**
*   **Spring Boot 3.5.14**
*   **PostgreSQL 16**
*   **Spring Data JPA** & **Flyway** (Migrations)
*   **MapStruct** (DTO Mapping)
*   **SpringDoc OpenAPI** (Swagger UI)
*   **Testcontainers** (Integration Testing)

---

## 🏃 How to Run the Project

### Prerequisites
*   **Docker Desktop** (Running)
*   **JDK 21** (If running locally without Docker)
*   **Maven** (Or use the provided `./mvnw`)

### Option 1: Run with Docker Compose (Recommended)
This is the easiest way to start the application along with its database.

```powershell
# Build and start all services
docker compose up --build
```

The application will be available at: `http://localhost:6200`
Swagger UI: `http://localhost:6200/swagger-ui.html`
Database: `localhost:5433` (User: `postgres`, Pass: `postgres`)

### Option 2: Run Locally (Maven)
1.  **Start only the database**:
    ```powershell
    docker compose up -d db
    ```
2.  **Run the application**:
    ```powershell
    ./mvnw spring-boot:run
    ```

---

## 🧪 Testing

The project includes a robust suite of unit and integration tests.

### Run All Tests
```powershell
./mvnw test
```

### Run Only Unit Tests
Unit tests are fast and don't require Docker.
```powershell
./mvnw test -Dtest="*Test,!*IntegrationTest,!MicroInvestmentApplicationTests"
```

### Run Integration Tests (Testcontainers)
Integration tests require **Docker** to be running. They spin up a real PostgreSQL container.
```powershell
./mvnw test -Dtest="*IntegrationTest"
```

### Coverage Overview
*   **Entities**: Logic for balance calculations (credit/debit) and transaction factories.
*   **Services**: Core business logic (round-up calculation, tax on withdrawal).
*   **Controllers**: REST endpoints validation and HTTP responses (using MockMvc).
*   **Scheduler**: Validation of background fee deduction tasks.

---

## 📖 API Documentation

Once the app is running, visit:
[http://localhost:6200/swagger-ui.html](http://localhost:6100/swagger-ui.html)

### Key Endpoints
*   `POST /api/v1/users`: Create a user.
*   `POST /api/v1/accounts`: Create a savings account.
*   `POST /api/v1/transactions/purchases`: Record a purchase (triggers investment).
*   `POST /api/v1/transactions/withdrawals`: Withdraw funds (applies 15% tax).
*   `GET /api/v1/accounts/{id}/balance`: Check account balance.
*   `GET /api/v1/transactions/{accountId}/transactions`: View transaction history.

---

## 📝 Important Notes`
*   **Rounding Logic**: Round-ups are calculated using `Ceiling` mode to 4 decimal places.
