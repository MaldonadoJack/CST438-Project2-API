# FitTrack API

FitTrack API is the Spring Boot backend for the FitTrack Android application. It stores users, exercises, workouts, and progress data in PostgreSQL, accepts Google ID tokens for authentication, and exposes interactive OpenAPI documentation through Swagger UI.

## Links

- Live API: <https://fittrack-api-noemhi.onrender.com/>
- Swagger UI: <https://fittrack-api-noemhi.onrender.com/swagger-ui.html>
- Android application: <https://github.com/MaldonadoJack/cst438-Project2>
- API contract: [`docs/openapi.yaml`](docs/openapi.yaml)

The live API root may return `401 Unauthorized`. This still confirms that the service is running; protected endpoints require a valid Google ID token.

## Technology

- Kotlin and Java 21
- Spring Boot 3
- Spring Security and Google OAuth
- Spring Data JPA
- PostgreSQL (hosted with Supabase for the deployed application)
- Gradle
- Docker
- Springdoc OpenAPI and Swagger UI

## Requirements

Install the following before running the backend locally:

- JDK 21
- PostgreSQL, or access to the team's Supabase PostgreSQL database
- Docker Desktop, if you want to run the container
- Node.js, only if you want to lint the OpenAPI file with Redocly

## Database setup

The database files are located in [`database/`](database/):

- `schema.sql` creates the users, exercises, workouts, and workout-exercise tables.
- `migrations/001_add_workout_completion.sql` contains the workout-completion migration.

For a new database, run `database/schema.sql` first and then apply the numbered migrations in order. You can run the files in the Supabase SQL Editor or with PostgreSQL's `psql` command.

```bash
psql "postgresql://USER:PASSWORD@HOST:5432/postgres" -f database/schema.sql
psql "postgresql://USER:PASSWORD@HOST:5432/postgres" -f database/migrations/001_add_workout_completion.sql
```

Do not put a real password in the repository or in a committed script.

## Environment variables

The backend reads its configuration from environment variables.

| Variable | Purpose | Example format |
| --- | --- | --- |
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://HOST:5432/postgres` |
| `DB_USERNAME` | PostgreSQL database user | `postgres.PROJECT_ID` |
| `DB_PASSWORD` | PostgreSQL database password | `your-database-password` |
| `GOOGLE_CLIENT_ID` | Google OAuth web client ID accepted by the API | `123456.apps.googleusercontent.com` |

Use your own values locally. Never commit credentials, client secrets, `.env` files, or database passwords.

## Run locally

From the repository root, set the environment variables and start Spring Boot:

```bash
export DB_URL="jdbc:postgresql://HOST:5432/postgres"
export DB_USERNAME="postgres.PROJECT_ID"
export DB_PASSWORD="your-database-password"
export GOOGLE_CLIENT_ID="your-web-client-id.apps.googleusercontent.com"

./gradlew bootRun
```

The API starts at <http://localhost:8080>. Local Swagger UI is available at <http://localhost:8080/swagger-ui.html>.

## Test and build

Run the complete backend test suite:

```bash
./gradlew test
```

Build the executable Spring Boot JAR:

```bash
./gradlew clean bootJar
```

The JAR is written to `build/libs/`. GitHub Actions also runs the tests, builds the JAR, and saves it as a workflow artifact on pushes and pull requests to `main`.

## Docker

Build the production image from the multi-stage Dockerfile:

```bash
docker build -t fittrack-api .
```

Run the image with the required environment variables:

```bash
docker run --rm -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://HOST:5432/postgres" \
  -e DB_USERNAME="postgres.PROJECT_ID" \
  -e DB_PASSWORD="your-database-password" \
  -e GOOGLE_CLIENT_ID="your-web-client-id.apps.googleusercontent.com" \
  fittrack-api
```

The `.dockerignore` file excludes local builds, Git history, IDE settings, and `.env` files from the image context.

## OpenAPI contract

The hand-written contract is [`docs/openapi.yaml`](docs/openapi.yaml). Lint it before opening a pull request:

```bash
npx @redocly/cli lint docs/openapi.yaml
```

The deployed Swagger page is generated from the running Spring controllers. When an endpoint changes, update the hand-written contract and its tests in the same pull request.

## Authentication

Protected routes expect a Google ID token:

```text
Authorization: Bearer GOOGLE_ID_TOKEN
```

The token's audience must match `GOOGLE_CLIENT_ID`. The Android application obtains the token through Google Credential Manager and sends it to the API.

## Project structure

```text
database/                 PostgreSQL schema and migrations
docs/                     API contract and design documentation
src/main/kotlin/          Spring controllers, services, repositories, and models
src/main/resources/       Spring configuration
src/test/kotlin/          Unit and integration tests
Dockerfile                Multi-stage production container
```

## Troubleshooting

- **Database authentication failed:** verify `DB_USERNAME`, `DB_PASSWORD`, and the Supabase connection-pooler URL.
- **Database connection refused:** confirm the database is running and that the host and port are reachable.
- **Schema validation failed:** apply `database/schema.sql` and all migrations to the selected database.
- **401 Unauthorized:** supply a current Google ID token whose audience matches `GOOGLE_CLIENT_ID`.
- **Render is slow on the first request:** a free Render service may need time to wake up.
