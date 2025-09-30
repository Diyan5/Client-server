## Technologies Used

- **Java 17** — no web framework (pure JDK).
- **HTTP server:** built-in `com.sun.net.httpserver.HttpServer` with custom GET/POST handlers.
- **Database:** **MySQL 8**, accessed via **JDBC** (MySQL Connector/J).
- **Password security:** `PBKDF2WithHmacSHA256` (~120k iterations), **16B salt** + **32B hash**, salts from `SecureRandom`.
- **Sessions / auth:** in-memory `SessionManager` + `SESSIONID` cookie (`HttpOnly`, `SameSite=Lax`, `Path=/`).
- **Front-end:** HTML5, custom CSS, vanilla JS (`auth-guard.js` to guard against back/forward cache).
- **Static assets & templates:** `resources/static` (css/images/js) and `resources/templates` (html) served by `StaticHandler` / `TemplateHandler`.
- **Standard Java APIs:** `java.sql.*`, `javax.crypto.*`, `java.security.*`, `java.util.*`, and core I/O.

## Implemented Features and How They Were Built

### 1) Registration (`/register`, GET/POST)
- **What it does:** Creates a new user from `name`, `email`, `password`.
- **How it’s implemented:**
  - **Server-side validation:** `Validation.isName/isEmail/isStrongPassword`.
  - **Duplicate email check:** `UsersDao.findByEmail(email)` → if present, render error.
  - **Password hashing:** `PasswordHasher` (PBKDF2WithHmacSHA256, ~120k iterations, **16B salt** + **32B hash**), stored in DB as `pass_salt BINARY(16)` and `pass_hash BINARY(32)`.
  - **DB insert:** `UsersDao.insert(name, email, passHash, passSalt)` (JDBC, PreparedStatement).
  - **UI errors:** injected into the template via `<!-- ERROR_REGISTER -->` (fixed `.alert-slot` so the button doesn’t jump).
  - **On success:** redirect to `/login`.

### 2) Login (`/login`, GET/POST)
- **What it does:** Authenticates by email/password.
- **How it’s implemented:**
  - **Lookup:** `UsersDao.findByEmail(email)`.
  - **Password verify:** `PasswordHasher.verify(input, salt, hash)` (constant-time comparison).
  - **Session:** `SessionManager.create(...)` issues a `SESSIONID` (Base64 URL-safe) with `userId/name/email`, **TTL ~30 min**.
  - **Cookie:** `Set-Cookie: SESSIONID=...; HttpOnly; SameSite=Lax; Path=/`.
  - **UI errors:** placeholder `<!-- ERROR_LOGIN -->`.
  - **On success:** redirect to `/home`.

### 3) Protected Home page (`/home`, GET)
- **What it does:** Shows “Hello, **{{username}}**!” only for authenticated users.
- **How it’s implemented:**
  - **Guard:** reads `SESSIONID` from cookie → if missing/expired → redirect to `/login`.
  - **Template:** replaces `{{username}}` in `templates/home.html` (HTML-escaped to prevent XSS).
  - **Cache control:** sends `Cache-Control: no-store, no-cache, must-revalidate`, `Pragma: no-cache`, `Expires: 0`, `Vary: Cookie`.
  - **bfcache guard:** `static/js/auth-guard.js` listens to `pageshow`; if no `SESSIONID`, does `location.replace('/login')`.

### 4) Logout (`/logout`, GET/POST)
- **What it does:** Logs out and returns to the landing page.
- **How it’s implemented:**
  - **Destroy session:** `SessionManager.destroy(sid)`.
  - **Delete cookie:** `SESSIONID=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax`.
  - **Cache control:** same no-cache headers to avoid returning Home from (bf)cache.
  - **Redirect:** to `/`.

### 5) Static assets & templates
- **Static files:** `StaticHandler` serves `/css/*`, `/images/*`, `/js/*` from `resources/static/...` (correct `Content-Type` via `MimeTypes`).
- **Templates:** `TemplateHandler` returns HTML from `resources/templates/*` (no template engine).
- **Router:** `Router` registers all contexts/routes.

### 6) Security & validation
- **Passwords:** PBKDF2 (unique salt + slow KDF), **never** stored in plaintext.
- **Validation:** always **server-side**; HTML `required/pattern` is UX only.
- **XSS protection:** `escape(String)` applied when injecting user text into HTML.
- **SQL injection:** avoided by PreparedStatement everywhere (no string concatenation).
- **Sessions:** `HttpOnly` cookie, `SameSite=Lax`, and no sensitive data stored client-side.

### 7) DB access (DAO layer)
- **`Db`** – central JDBC connection (URL/user/pass).
- **`UsersDao`** – `insert(...)`, `findByEmail(...)`, `findIdByEmail(...)`.
- **Schema:** `users(id, name, email UNIQUE, pass_hash BINARY(32), pass_salt BINARY(16), created_at)`.

### 8) UX details
- **Fixed error slot** (`.alert-slot` with `min-height`) so buttons keep their position.
- **Friendly error messages** for invalid input / duplicate email.
- **Navigation:** explicit redirects after success.

## Built-in APIs Used (short)

- **HTTP (JDK):** `HttpServer`, `HttpExchange` (headers/body/status).
- **JDBC:** `DriverManager`, `PreparedStatement`, `ResultSet` (parameterized queries, generated keys).
- **Crypto:** `SecretKeyFactory` (PBKDF2WithHmacSHA256), `PBEKeySpec`, `SecureRandom` (salt/SESSIONID).
- **Collections/Utility:** `ConcurrentHashMap` (sessions), `Base64` (URL-safe ids), `Optional`.
- **I/O & Encoding:** `readAllBytes`, `URLDecoder`, `StandardCharsets.UTF_8`.
- **Front-end:** HTML5 attributes (`required`, `type="email"`), JS `pageshow` + `performance.getEntriesByType('navigation')`.

## What each file does 

**Java (backend)**
- `registrationApp/Main.java` — boots the embedded HTTP server.
- `registrationApp/http/Router.java` — registers routes (`/`, `/register`, `/login`, `/home`, `/logout`, `/css`, `/images`, `/js`).
- `registrationApp/http/RegisterHandler.java` — GET form; POST validation + PBKDF2 + DB insert.
- `registrationApp/http/LoginHandler.java` — GET form; POST password verify, creates session, redirects to `/home`.
- `registrationApp/http/HomeHandler.java` — protected page; requires `SESSIONID`; no-cache headers; renders `{{username}}`.
- `registrationApp/http/LogoutHandler.java` — destroys session, deletes cookie, no-cache, redirects to `/`.
- `registrationApp/http/StaticHandler.java` — serves `/css/*`, `/images/*`, `/js/*` from `resources/static`.
- `registrationApp/http/TemplateHandler.java` — returns HTML files from `resources/templates`.
- `registrationApp/http/HttpUtil.java` — helpers: read `x-www-form-urlencoded`, `sendHtml`, `redirect`.
- `registrationApp/http/ResourceUtil.java` — reads classpath resources (templates/static).
- `registrationApp/http/MimeTypes.java` — sets `Content-Type` by file extension.
- `registrationApp/http/Cookies.java` — simple Cookie header parser.

**Database**
- `registrationApp/db/Db.java` — creates JDBC connection to MySQL (URL/USER/PASS).
- `registrationApp/db/UsersDao.java` — `insert(...)`, `findByEmail(...)`, `findIdByEmail(...)`.

**Model**
- `registrationApp/model/User.java` — `record(id, name, email, passHash, passSalt)`.

**Security**
- `registrationApp/security/PasswordHasher.java` — PBKDF2WithHmacSHA256 (16B salt, 32B hash, ~120k iterations).
- `registrationApp/security/SessionManager.java` — in-memory sessions (`SESSIONID` cookie, ~30 min TTL).

**Resources (frontend)**
- `src/main/resources/templates/index.html` — landing page.
- `src/main/resources/templates/register.html` — registration (`<!-- ERROR_REGISTER -->` placeholder).
- `src/main/resources/templates/login.html` — login (`<!-- ERROR_LOGIN -->` placeholder).
- `src/main/resources/templates/home.html` — protected page (`{{username}}`).
- `src/main/resources/static/css/*.css` — styles.
- `src/main/resources/static/images/th.png` — logo/image.
- `src/main/resources/static/js/auth-guard.js` — prevents returning to Home after logout (bfcache guard).

