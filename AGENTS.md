# AGENTS.md

## Cursor Cloud specific instructions

This is a **Java 21 / Spring Boot 4.0.3** server-rendered web application (Dynamic Price Manager) using Maven Wrapper, Thymeleaf, Spring Security, and an embedded H2 in-memory database.

### Quick reference

| Action | Command |
|---|---|
| Install/resolve dependencies | `./mvnw dependency:resolve` |
| Run tests | `./mvnw test` |
| Start dev server | `./mvnw spring-boot:run` (runs on port 8080) |
| Compile only | `./mvnw compile` |
| Package JAR | `./mvnw package` |

### Key notes

- **No external services required.** The app uses an embedded H2 in-memory database (`jdbc:h2:mem:testdb`). No Docker, PostgreSQL, or other services need to be started.
- **Seeded admin account:** On every startup, `DataInitializer` creates an admin user `admin@system.com` with password `admin` (role `ADMIN`). If the account already exists, the initializer is a no-op.
- **CSRF is enabled** (Spring Security default). Form-based POST requests (login, register) require a `_csrf` token. The login/register Thymeleaf templates include the token automatically.
- **Roles:** `ADMIN` accesses `/admin/**`, `CLIENT` accesses `/client/**`. New registrations via `/register` get the `CLIENT` role.
- **H2 Console** is available at `/h2-console` during dev (CSRF disabled for that path).
- **Java 21** is required (`<java.version>21</java.version>` in `pom.xml`). The VM already has OpenJDK 21 installed.
