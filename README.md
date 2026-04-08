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
VITE_API_BASE_URL=http://localhost:8080/api
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

## Local MySQL Setup

Recommended one-time setup:

1. Copy `src/main/resources/application-local.yml.example`
2. Rename the copy to `src/main/resources/application-local.yml`
3. Put your MySQL username/password in that local file
4. Start the backend with:

```powershell
.\start-local.ps1
```

`application-local.yml` is gitignored, so your password stays local.
