# Course Enrolment System

A full-stack course enrolment platform built as a capstone project. Students can browse
and enrol in courses; admins manage the course catalog, review all enrolments, and view
enrolment reports. The backend is a Spring Boot REST API secured with JWT; the frontend
is a React single-page app.

## Tech stack

| Layer    | Technology |
|----------|------------|
| Backend  | Java 21, Spring Boot 4.1 (Web MVC, Security, Validation, Data MongoDB), Maven |
| Auth     | Stateless JWT (`jjwt` 0.12.6, `BCrypt` password hashing) |
| Database | MongoDB |
| API docs | springdoc-openapi + Swagger UI |
| Frontend | React 19, Vite 8, React Router 8 |

## Project structure

```
Capstone-Project/
├── backend/    Spring Boot REST API (Maven)
└── frontend/   React + Vite single-page app
```

## Prerequisites

- **Java 21** (JDK)
- **Maven** — or use the bundled wrapper (`./mvnw`, no local install needed)
- **Node.js** 18+ and npm
- **MongoDB** running locally (or reachable) — e.g. via [Laragon](https://laragon.org/), Docker, or a native install, on the default port `27017`

## Getting started

### 1. Clone and configure the backend

```bash
cd backend
cp .env.example .env
```

Edit `backend/.env` with your own values:

| Variable | Default | Description |
|---|---|---|
| `MONGODB_HOST` | `localhost` | MongoDB host |
| `MONGODB_PORT` | `27017` | MongoDB port |
| `MONGODB_DATABASE` | `course_enrolment_db` | Database name |
| `MONGODB_AUTH_DATABASE` | `course_enrolment_db` | Database MongoDB authenticates the user against |
| `MONGODB_USERNAME` | *(empty)* | MongoDB username, if auth is enabled |
| `MONGODB_PASSWORD` | *(empty)* | MongoDB password, if auth is enabled |
| `JWT_SECRET` | *(dev default in `application.properties`)* | HMAC-SHA256 signing key — **must be at least 32 characters**; set a real random value outside local dev |
| `JWT_EXPIRATION_MINUTES` | `60` | How long issued tokens stay valid |

`.env` is loaded automatically at startup (`BackendApplication`) and is gitignored, so
real credentials never get committed.

### 2. Run the backend

```bash
cd backend
./mvnw spring-boot:run        # macOS/Linux
mvnw.cmd spring-boot:run      # Windows
```

The API starts on **http://localhost:8080**.

On first run, two seed accounts and five sample courses are created automatically if the
database is empty:

| Email | Password | Role |
|---|---|---|
| `admin@example.com` | `Admin@12345` | `ADMIN` |
| `student@example.com` | `Student@12345` | `STUDENT` |

### 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The app starts on **http://localhost:5173** (Vite default) and proxies any request to
`/api/**` to `http://localhost:8080` (see `frontend/vite.config.js`), so no frontend
environment variables are required in dev.

Other frontend scripts: `npm run build` (production bundle), `npm run preview` (serve the
build locally), `npm run lint` (ESLint).

> **Note:** there is no CORS configuration in the backend. This is fine in dev because
> Vite proxies API requests same-origin. If you deploy the frontend and backend on
> different origins, you'll need to add CORS config (or a reverse proxy) yourself.

## API documentation

The backend exposes interactive, always-up-to-date API docs via [springdoc-openapi](https://springdoc.org/):

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Raw OpenAPI 3 spec (JSON):** http://localhost:8080/v3/api-docs

To try protected endpoints from Swagger UI: call `POST /api/auth/login` (or `/register`),
copy the returned `token`, click **Authorize**, and paste it in (Swagger adds the
`Bearer ` prefix automatically). Every subsequent request from the UI will include it.

### Endpoint summary

All request/response bodies are JSON. Validation and business-rule errors return a
consistent shape: `{ message, status, timestamp, errors[] }`.

#### Auth — `/api/auth` (public)

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account (always role `STUDENT`); returns a JWT |
| POST | `/api/auth/login` | Authenticate with email/password; returns a JWT |

#### Courses — `/api/courses`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/courses` | Any logged-in user | Search/paginate courses — query params: `keyword`, `category`, `level`, `active`, `page`, `size` (max 50), `sortBy` (`title`\|`category`\|`level`\|`capacity`\|`createdAt`), `direction` (`asc`\|`desc`) |
| GET | `/api/courses/{id}` | Any logged-in user | Get a course by id |
| POST | `/api/courses` | `ADMIN` | Create a course |
| PUT | `/api/courses/{id}` | `ADMIN` | Update a course (rejected if new capacity < current enrolled count) |
| PATCH | `/api/courses/{id}/deactivate` | `ADMIN` | Soft-delete: blocks new enrolments, keeps history |
| PATCH | `/api/courses/{id}/activate` | `ADMIN` | Reactivate a course |

#### Enrolments — `/api/enrolments`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/enrolments` | `STUDENT` | Enrol yourself in a course (fails if inactive, full, or already enrolled) |
| GET | `/api/enrolments/my` | Any logged-in user | List your own enrolments |
| GET | `/api/enrolments` | `ADMIN` | List every enrolment |
| DELETE | `/api/enrolments/{id}` | Any logged-in user | Cancel an enrolment — students may only cancel their own (403 otherwise), admins may cancel any |

#### Reports — `/api/reports` (`ADMIN` only)

| Method | Path | Description |
|---|---|---|
| GET | `/api/reports/enrolments-by-course` | Enrolment counts grouped by course |
| GET | `/api/reports/enrolments-by-category` | Enrolment counts grouped by course category |
| GET | `/api/reports/monthly-enrolments` | Enrolment counts grouped by month |

## Authentication & authorization

- Auth is **stateless JWT** — no server-side sessions. Send `Authorization: Bearer <token>`
  on every request to a protected endpoint.
- Tokens are issued by `/api/auth/login` and `/api/auth/register`, signed with HMAC-SHA256
  using `JWT_SECRET`, and expire after `JWT_EXPIRATION_MINUTES`.
- Two roles exist: `STUDENT` and `ADMIN`. New registrations are always `STUDENT`; there is
  no self-service way to become `ADMIN` — use the seeded admin account or create one
  directly in MongoDB.
- Passwords are hashed with BCrypt.

## Running tests

```bash
cd backend
./mvnw test
```

The frontend currently has no automated test suite (`npm run lint` is available for static checks).

## Database

MongoDB collections are created automatically on first use via Spring Data MongoDB:

| Collection | Model | Notes |
|---|---|---|
| `users` | `AppUser` | Unique index on `email` |
| `courses` | `Course` | Indexes on `title`, `category`, `level`, `active` |
| `enrolments` | `Enrolment` | Indexes on `studentId`, `courseId`, `courseCategory`, `status` |

No connection string format is used — connection details come from the individual
`spring.mongodb.*` properties in `backend/src/main/resources/application.properties`,
populated from the environment variables above.
