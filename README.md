# Foodies API — Backend

REST API backend for the Foodies food delivery platform. Built with Spring Boot, secured with JWT, connected to MongoDB Atlas, and integrated with Razorpay for payments.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.3.5 |
| Language | Java 21 |
| Database | MongoDB Atlas |
| Authentication | JWT (JSON Web Tokens) |
| Security | Spring Security |
| Payment | Razorpay |
| Build Tool | Maven |
| File Upload | Spring Multipart |
| Containerization | Docker (multi-stage build) |

---

## Project Structure

```
foodiesapi-backend/
├── src/
│   └── main/
│       ├── java/in/souravkadam/foodiesapi/
│       │   ├── config/
│       │   │   ├── SecurityConfig.java     # Spring Security + CORS
│       │   │   ├── WebConfig.java          # Jackson serialization config
│       │   │   └── AdminSeeder.java        # Auto-creates admin on startup
│       │   ├── controller/
│       │   │   ├── AuthController.java     # /api/register, /api/login
│       │   │   ├── FoodController.java     # /api/foods
│       │   │   ├── CartController.java     # /api/cart
│       │   │   ├── OrderController.java    # /api/orders
│       │   │   ├── UserController.java     # /api/users (admin)
│       │   │   └── DashboardController.java
│       │   ├── Entity/
│       │   │   ├── UserEntity.java
│       │   │   ├── FoodEntity.java
│       │   │   ├── CartEntity.java
│       │   │   ├── OrderEntity.java
│       │   │   └── LoginHistory.java
│       │   ├── io/           # Request/Response DTOs
│       │   ├── repository/   # Spring Data MongoDB repositories
│       │   ├── service/      # Business logic (interfaces + impls)
│       │   ├── filters/      # JWT authentication filter
│       │   └── util/         # JwtUtil and helpers
│       └── resources/
│           ├── application.properties       # Local config
│           └── application-prod.properties  # Production config
├── Dockerfile          # Multi-stage Docker build for Render
├── system.properties   # Java 21 version hint
├── pom.xml
└── README.md
```

---

## Prerequisites

- Java JDK 21 — https://adoptium.net
- Maven 3.9+ — https://maven.apache.org
- MongoDB Atlas account — https://cloud.mongodb.com

---

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd foodiesapi-backend
```

### 2. Set environment variables

The app reads all secrets from environment variables. Set these before running:

| Variable | Description |
|----------|-------------|
| `MONGODB_URI` | MongoDB Atlas connection string |
| `JWT_SECRET` | Secret key for JWT signing (min 32 chars) |
| `RAZORPAY_KEY` | Razorpay API key ID |
| `RAZORPAY_SECRET` | Razorpay API key secret |
| `CORS_ORIGINS` | Comma-separated allowed frontend origins |
| `PORT` | Server port (default: 8080) |
| `UPLOAD_PATH` | Local upload directory (default: uploads) |

Windows CMD:
```cmd
set MONGODB_URI=your_connection_string
set JWT_SECRET=your_secret_key
set RAZORPAY_KEY=your_razorpay_key
set RAZORPAY_SECRET=your_razorpay_secret
set CORS_ORIGINS=http://localhost:5173,http://localhost:5174
```

Windows PowerShell:
```powershell
$env:MONGODB_URI="your_connection_string"
$env:JWT_SECRET="your_secret_key"
```

macOS / Linux:
```bash
export MONGODB_URI=your_connection_string
export JWT_SECRET=your_secret_key
```

### 3. Run the server

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

Server starts at **http://localhost:8080**

---

## Admin Account

An admin account is automatically created on first startup via `AdminSeeder.java`.

If the admin user already exists with the wrong role, the seeder automatically upgrades the role to `ADMIN` on startup.

To manually grant admin access to an existing user, set their `role` field to `ADMIN` in MongoDB Atlas.

---

## API Reference

All endpoints are prefixed with `/api`. Protected routes require:
```
Authorization: Bearer <jwt_token>
```

### Auth

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/register` | No | Register new user |
| POST | `/api/login` | No | Login → returns JWT token + role |

Login response includes:
```json
{
  "token": "...",
  "role": "USER or ADMIN",
  "name": "User Name",
  "userId": "..."
}
```

### Foods

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/foods` | No | Get all food items |
| GET | `/api/foods/{id}` | No | Get food by ID |
| POST | `/api/foods` | Admin JWT | Add food (multipart) |
| DELETE | `/api/foods/{id}` | Admin JWT | Delete food |

### Cart

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/cart` | JWT | Get user cart |
| POST | `/api/cart` | JWT | Add item to cart |
| POST | `/api/cart/remove` | JWT | Remove one quantity |
| DELETE | `/api/cart/clear` | JWT | Clear cart |

### Orders

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/orders/create` | JWT | Create order + Razorpay order |
| POST | `/api/orders/verify` | JWT | Verify Razorpay payment |
| GET | `/api/orders` | JWT | Get user's orders |
| DELETE | `/api/orders/{id}` | JWT | Delete order |
| GET | `/api/orders/all` | Admin JWT | Get all orders |
| PATCH | `/api/orders/status/{id}` | Admin JWT | Update order status |

### Users (Admin)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users` | Admin JWT | Get all users |
| PUT | `/api/users/{id}` | Admin JWT | Update user details |
| PATCH | `/api/users/{id}/block` | Admin JWT | Block user |
| PATCH | `/api/users/{id}/unblock` | Admin JWT | Unblock user |
| DELETE | `/api/users/{id}` | Admin JWT | Delete user |

---

## MongoDB Collections

Collections are created automatically on first insert — no migrations needed.

| Collection | Description |
|------------|-------------|
| `users` | Registered users and admins |
| `foods` | Food menu items with images |
| `carts` | Per-user shopping carts |
| `orders` | All placed orders |
| `loginhistory` | User login tracking |

---

## MongoDB Atlas Setup

1. Go to cloud.mongodb.com
2. Network Access → Add IP `0.0.0.0/0` (dev) or your server IP (prod)
3. Database Access → Create user with `readWrite` on your database
4. Get the connection string → set as `MONGODB_URI`

---

## Deployment on Render (Docker)

Render does not have a native Java runtime. Use the included `Dockerfile`.

1. Push backend to GitHub
2. Render → New Web Service → Connect GitHub repo
3. Runtime: select **Docker**
4. Render uses the `Dockerfile` automatically
5. Add environment variables in Render dashboard:

| Key | Description |
|-----|-------------|
| `MONGODB_URI` | MongoDB Atlas connection string |
| `JWT_SECRET` | Random string, min 32 characters |
| `RAZORPAY_KEY` | Razorpay key ID |
| `RAZORPAY_SECRET` | Razorpay key secret |
| `CORS_ORIGINS` | Vercel frontend URLs, comma-separated |

6. Deploy — Render builds Docker image and starts container

The Dockerfile uses a multi-stage build:
- Stage 1: Maven + JDK 21 compiles and packages the JAR
- Stage 2: JRE 21 runs the JAR (smaller final image)

---

## Security

- Passwords hashed with BCrypt — never stored in plain text
- JWT tokens expire after 7 days
- CORS configured via `CORS_ORIGINS` environment variable
- Never commit secrets to Git — use environment variables
- `.gitignore` excludes `target/`, `*.jar`, `.env`

---

## License

MIT
