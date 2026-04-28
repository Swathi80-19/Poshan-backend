# Poshan Backend

Standalone Spring Boot backend for the Poshan frontend.

## Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Validation
- MySQL for local runtime
- H2 for tests

## Repo Structure

- `src/main/java/com/poshan/backend/config`
- `src/main/java/com/poshan/backend/controller`
- `src/main/java/com/poshan/backend/dto`
- `src/main/java/com/poshan/backend/entity`
- `src/main/java/com/poshan/backend/enums`
- `src/main/java/com/poshan/backend/repository`
- `src/main/java/com/poshan/backend/service`

## Frontend Integration

Set this in the frontend `.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Core Endpoints

- `POST /api/auth/members/register`
- `POST /api/auth/members/login`
- `POST /api/auth/nutritionists/register`
- `POST /api/auth/nutritionists/login`
- `POST /api/auth/logout`
- `GET /api/member/profile`
- `PUT /api/member/profile`
- `GET /api/member/food-logs`
- `POST /api/member/food-logs`
- `GET /api/member/activity-logs`
- `POST /api/member/activity-logs`
- `GET /api/member/dashboard`
- `GET /api/member/appointments`
- `GET /api/member/payments`
- `GET /api/nutritionists`
- `GET /api/nutritionists/me/patients`
- `GET /api/nutritionists/me/dashboard`
- `POST /api/appointments`
- `GET /api/appointments/member`
- `GET /api/appointments/nutritionist`
- `POST /api/payments`
- `POST /api/reports`
- `GET /api/reports/nutritionist`

## Auth Rules

- Registration creates the account only. It does not act as login.
- Login returns an `accessToken`.
- Every protected route must send:

```http
Authorization: Bearer <accessToken>
```

- Member data endpoints now resolve the logged-in member from the token, so one member cannot read another member's profile, logs, payments, or dashboard by changing an id in the URL.

## Notes

- This scaffold is designed as a separate backend repo beside the frontend repo.
- It uses empty/real database-backed records only, not seeded demo data.
- Nutritionist accounts and member assignments are stored in MySQL, so the nutritionist workspace and member search use the same live records.

## Local Startup

The backend now uses a single runtime config file: `src/main/resources/application.yml`.
Local startup uses MySQL, so data is stored in the `poshan` schema and visible in MySQL Workbench.

1. If needed, set your local MySQL credentials:

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="your_password"
```

2. Configure SMTP for real email verification:

```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_app_password"
$env:MAIL_FROM="your_email@gmail.com"
$env:FRONTEND_BASE_URL="http://localhost:5173"
```

For Gmail, use an App Password instead of your normal mailbox password.

3. Start the backend with:

```powershell
.\start-local.ps1
```

By default it connects to:

- host: `localhost`
- port: `3306`
- database: `poshan`

## Render Postgres

For Render deployment, use a Render Postgres database and set standard Spring datasource variables on the web service:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://<internal-host>:5432/<database-name>
SPRING_DATASOURCE_USERNAME=<database-user>
SPRING_DATASOURCE_PASSWORD=<database-password>
CORS_ALLOWED_ORIGINS=https://<your-frontend>.vercel.app
FRONTEND_BASE_URL=https://<your-frontend>.vercel.app
```

Use the internal connection details from your Render Postgres dashboard when the web service and database are in the same region.

## Email Verification Flow

- New member and nutritionist accounts are created as unverified.
- Registration sends a real email verification link through SMTP to the deployed frontend `/verify-email` page.
- Login is blocked until that email link is opened successfully.
- Users can request a fresh verification email from the frontend verification screen.
