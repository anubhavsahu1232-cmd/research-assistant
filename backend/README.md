# Research Assistant — Core Backend
Java 21, Spring Boot 3.4.5, MySQL. Existing database schema in main branch: `new schema.sql`. Import it before startup. Schema remains unchanged.

## Run
Set `DB_URL` (default `jdbc:mysql://localhost:3306/research_assistant_db`), `DB_USER`, `DB_PASSWORD`, and **`JWT_SECRET`** (at least 32 UTF-8 bytes). Optional `UPLOAD_DIR`.
From `backend/`: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
Run tests: `./mvnw test`.

## Endpoints
- POST /api/auth/register and POST /api/auth/login
- GET, PUT /api/users/me (Bearer JWT)
- GET /api/admin/users; PATCH /api/admin/users/{id}/role (ADMIN)
- GET /api/categories, /api/categories/{id}; POST/PUT/DELETE /api/categories (ADMIN)
- GET /api/topics, /api/topics/{id}; POST/PUT/DELETE /api/topics (ADMIN)
- GET /api/papers?q=&categoryId=&topicId=&year=&page=0&size=10; GET /api/papers/{id} (public)
- POST /api/papers; PUT/DELETE /api/papers/{id} (owner or ADMIN)
- POST /api/papers/{id}/pdf (multipart field `file`, owner or ADMIN); GET /api/papers/{id}/pdf (public)

Security note: the schema includes sample users with placeholder password hashes. Those sample accounts cannot log in until their passwords are replaced with BCrypt hashes. Do not use sample accounts for authentication testing; register a new user. Protect JWT_SECRET and DB credentials via environment variables, never commit real secrets.
