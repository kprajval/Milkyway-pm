# Milkyway Portfolio Manager (StockView)

A lightweight Spring Boot web application for managing a simulated investment portfolio. It provides a dashboard for holdings and purse balance, buy/sell operations, watchlist and market lookup features, and transaction history with PDF export.

This README documents the project structure, setup instructions, runtime endpoints, frontend behavior, database schema, and developer notes to help you run, maintain and extend the application.

---

## Table of Contents

- Project overview
- Repository structure
- Prerequisites
- Run locally
- Application endpoints (REST/UI)
- Frontend notes (templates & JS behavior)
- Database / Entities
- Important implementation details
- Troubleshooting
- Development tasks and suggestions

---

## Project overview

`Milkyway Portfolio Manager` (also referenced across templates as *StockView*) is a demo portfolio manager used for learning and small experiments. The app holds an internal `purse` (cash balance), a list of holdings (stock symbol, quantity, invested amount), and a transaction history tracking operations and purse changes.

Key features:
- Dashboard with asset allocation (doughnut chart), holdings table and purse controls
- Buy and Sell endpoints that update holdings and create transaction records
- Manual purse add/deduct endpoints that create `PURSE ADD` / `PURSE DEDUCT` transactions
- Transaction history page with PDF export using html2pdf
- Market lookup and history proxy (the repository includes a local proxy script `proxy.py` used for fetching price data in development)

---

## Repository structure

- `pom.xml` — Maven build file
- `mvnw`, `mvnw.cmd` — Maven wrappers
- `src/main/java/com/neueda/pm_milkyway/` — Java sources
	- `PmMilkywayApplication.java` — Spring Boot entrypoint
	- `controller/` — Spring MVC controllers (UI views + REST API)
	- `service/` — Business logic (transactions, holdings, market lookup helpers)
	- `repo/` — Spring Data JPA repositories
	- `entity/` — JPA entity classes mapping to the database tables
	- `sqlConfig/schema.sql` — SQL schema used to initialize the DB (if applicable)
- `src/main/resources/templates/` — Thymeleaf HTML templates for UI (`Dashboard.html`, `TransactionHistory.html`, `MarketLookup.html`, `Performance.html`)
- `src/main/resources/application.properties` — Spring Boot configuration
- `proxy.py` — Optional Python proxy used in development to fetch market data (used by frontend JS to avoid CORS issues)

---

## Prerequisites

- Java 17+ (JDK 17 recommended)
- Maven 3.6+ (you can use the included `mvnw` wrapper)
- A running relational database (H2, MySQL, PostgreSQL, etc.) configured in `application.properties` — this project expects JPA/Hibernate
- Node or Python only if you want to run the `proxy.py` script locally for market data (optional). The app's frontend calls `http://localhost:3000` in some templates for price lookups.

---

## Run locally

1. Configure database connection in `src/main/resources/application.properties`.
2. Build and run with Maven wrapper:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

Or use your IDE to run `com.neueda.pm_milkyway.PmMilkywayApplication`.

The application typically starts at `http://localhost:8080/`.

If using the local market proxy, run it separately (example):

```bash
python proxy.py
```

---

## Application Endpoints

UI pages (Thymeleaf):
- `GET /` — Dashboard view (renders `Dashboard.html`)
- `GET /market-lookup` — Market lookup UI
- `GET /performance` — Performance UI
- `GET /transaction-history` — Transaction history UI

REST API endpoints (under `/api`):
- `GET /api/purse-value` — Returns latest purse balance (derived from last transaction or default starting value)
- `POST /api/purse/add?amount={amount}` — Add amount to purse; records a `PURSE ADD` transaction
- `POST /api/purse/deduct?amount={amount}` — Deduct amount from purse; records a `PURSE DEDUCT` transaction
- `POST /api/transactions/buy?symbol={symbol}&quantity={q}&price={p}` — Execute purchase: updates holdings, records `BUY {symbol}` transaction and updates purse
- `POST /api/transactions/sell?symbol={symbol}&quantity={q}&price={p}` — Execute sale: updates holdings, records `SELL {symbol}` transaction and updates purse
- `POST /api/holdings/adjust?symbol={symbol}&action=PLUS|MINUS&price={p}` — Adjust holdings by one unit and create corresponding `BUY/SELL` transaction
- `GET /api/dashboard/stats` — Returns JSON with `holdings` and `purse` to help UI chat/actions

Repository interfaces expose the standard Spring Data JPA operations for `transactions`, `holdings`, and watchlist items.

---

## Frontend notes (templates & JS behavior)

- `Dashboard.html`:
	- Uses Chart.js to render an asset allocation doughnut via `<canvas id="allocationChart">`.
	- Fetches current purse from `/api/purse-value` and updates the `#stat-purse` element.
	- Iterates holdings rendered by Thymeleaf and calls a local proxy (`http://localhost:3000/quote/{symbol}`) to fetch current prices for each stock.
	- After fetching prices, the dashboard calculates market values and prepares the doughnut chart dataset. Be sure the price proxy is running or update the fetch URLs.
	- Small interactive features: show per-stock graph modal (30-day history) using Chart.js line chart; a simple chat widget that can trigger buy/sell actions.
	- New purse controls: a numeric input with `Add Purse` and `Deduct Purse` buttons which call the endpoints `/api/purse/add` and `/api/purse/deduct`.

- `TransactionHistory.html`:
	- Renders `transactions` server-side and includes a `Generate Report` button which uses `html2pdf` to export a styled PDF. The PDF generator forces landscape orientation and fixed table layout to include the last column reliably.

---

## Database / Entities

Primary entities:

- `HoldingsEntity` (table `holdings`):
	- `id` (PK)
	- `stock` (symbol string)
	- `quantity` (integer)
	- `total_invested` (double) — total cost basis for the holding

- `TransactionEntity` (table `transactions`):
	- `id` (PK)
	- `date` (LocalDate)
	- `type` (String) — e.g., `BUY AAPL`, `SELL TSLA`, `PURSE ADD`, `PURSE DEDUCT`
	- `transactionValue` (BigDecimal) — monetary value of the operation
	- `purseValue` (BigDecimal) — resulting purse value after the transaction
	- `status` (Boolean)

Notes:
- The application derives the current purse by reading the latest entry in the `transactions` table and using its `purseValue`. If the table is empty, a default starting balance (e.g., 100000.0) is returned.

---

## Important implementation details

- Purse logic: `TransactionsService#getPurseValue()` reads the latest transaction's `purseValue` as the current balance. All buy/sell and purse add/deduct operations create a `TransactionEntity` row containing the updated `purseValue`.
- Chart handling: When re-rendering Chart.js charts, the code explicitly destroys the previous chart instance (if any) before creating a new one to avoid Chart.js errors.
- PDF export: `TransactionHistory.html` uses `html2pdf` with `jsPDF` in landscape orientation and `table-layout: fixed` to ensure all columns export properly.

---

## Troubleshooting

- If the asset allocation donut is empty or not rendering:
	- Ensure the frontend price proxy is running (if used) or update the JS to fetch prices from a working API.
	- Check browser console for JS exceptions; a null DOM lookup or Chart.js errors will prevent rendering.

- If transactions/purse do not persist:
	- Confirm DB configuration in `application.properties` and that migrations / schema were applied (see `src/main/resources/sqlConfig/schema.sql`).

- If PDF column is clipped:
	- The generator now forces landscape orientation. Adjust `html2pdf` options or lower font sizes if you have extremely wide content.

---

## Development notes & suggestions

- Tests: Add unit tests for `TransactionsService` covering buy, sell, and purse operations.
- Validation: Consider adding request validation on controller endpoints (e.g., `@Valid` + DTOs) and more specific error responses (JSON) for AJAX consumers.
- Concurrency: `getPurseValue()` reads latest transaction by sorting all transactions. For high concurrency or performance, a separate table/column to store singleton purse state would be more efficient.
- Security: Add authentication/authorization (Spring Security) when moving beyond a demo.

---

If you'd like, I can:
- Add API documentation (OpenAPI/Swagger)
- Add unit tests for the `TransactionsService`
- Add a small script to initialise the DB with sample holdings and transactions

---

_README generated and updated to provide a complete developer and user overview of the project._
