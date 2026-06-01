# Foodies API — Backend

REST API backend for the Food Delivery application. Built with **Spring Boot**, secured with **JWT**, connected to **MongoDB Atlas**, and integrated with **Razorpay** for payments.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x |
| Language | Java 21 |
| Database | MongoDB Atlas |
| Authentication | JWT (JSON Web Tokens) |
| Security | Spring Security |
| Payment | Razorpay |
| Build Tool | Maven |
| File Upload | Spring Multipart |

---

## Project Structure

```
foodiesapi-backend/
├── src/
│   └── main/
│       ├── java/in/souravkadam/foodiesapi/
│       │   ├── config/
│       │   │   ├── SecurityConfig.java        # Spring Security + CORS config
│       │   │   ├── WebConfig.java             # Jackson / serialization config
│       │   │   └── AdminSeeder.java           # Auto-creates admin user on startup
│       │   ├── controller/
│       │   │   ├── AuthController.java        # /api/register, /api/login
│       │   │   ├── FoodController.java        # /api/foods
│       │   │   ├── CartController.java        # /api/cart
│       │   │   ├── OrderController.java       # /api/orders
│       │   │   ├── UserController.java        # /api/users (admin)
│       │   │   └── DashboardController.java   # /api/admin/dashboard
│       │   ├── Entity/
│       │   │   ├── UserEntity.java
│       │   │   ├── FoodEntity.java
│       │   │   ├── CartEntity.java
│       │   │   ├── OrderEntity.java
│       │   │   └── LoginHistory.java
│       │   ├── io/                            # Request/Response DTOs
│       │   ├── repository/                    # Spring Data MongoDB repos
│       │   ├── service/                       # Business logic interfaces + impls
│       │   ├── filters/                       # JWT request filter
│       │   └── util/                          # Helper utilities
│       └── resources/
│           ├── application.properties         # Local config (uses env var defaults)
│           └── application-prod.properties    # Production config (env vars only)
├── Dockerfile                                 # Multi-stage Docker build for Render
├── system.properties                          # Java version hint
├── pom.xml
└── README.md
```

---

## Prerequisites

- Java JDK 21 → https://adoptium.net
- Maven 3.9+ → https://maven.apache.org
- MongoDB Atlas account → https://cloud.mongodb.com

---

## Setup & Run Locally

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd foodiesapi-backend
```

### 2. Configure environment variables

The app reads secrets from environment variables. Set these before running:

| Variable | Description |
|----------|-------------|
| `MONGODB_URI` | Your MongoDB Atlas connection string |
| `JWT_SECRET` | A random string (min 32 chars) for signing tokens |
| `RAZORPAY_KEY` | Your Razorpay API key ID |
| `RAZORPAY_SECRET` | Your Razorpay API key secret |
| `CORS_ORIGINS` | Comma-separated allowed frontend URLs |
| `PORT` | Server port (default: `8080`) |
| `UPLOAD_PATH` | Local file upload directory (default: `uploads`) |

**Option A — Set in your shell (recommended for local dev):**

```bash
# Windows CMD
set MONGODB_URI=your_mongodb_connection_string
set JWT_SECRET=your_jwt_secret_key_min_32_chars

# Windows PowerShell
$env:MONGODB_URI="your_mongodb_connection_string"
$env:JWT_SECRET="your_jwt_secret_key_min_32_chars"

# macOS / Linux
export MONGODB_URI=your_mongodb_connection_string
export JWT_SECRET=your_jwt_secret_key_min_32_chars
```

**Option B — Edit `application.properties` locally (never commit with real values):**

```properties
spring.data.mongodb.uri=YOUR_MONGODB_URI
jwt.secret.key=YOUR_JWT_SECRET
razorpay.key=YOUR_RAZORPAY_KEY_ID
razorpay.secret=YOUR_RAZORPAY_SECRET
cors.allowed-origins=http://localhost:5173,http://localhost:5174
```

> Never commit `application.properties` with real credentials. Add it to `.gitignore`.

### 3. Run the server

```bash
# Windows — Maven wrapper
mvnw.cmd spring-boot:run

# macOS / Linux — Maven wrapper
./mvnw spring-boot:run

# Or build a JAR first, then run
mvnw.cmd clean package -DskipTests
java -jar target/foodiesapi-0.0.1-SNAPSHOT.jar
```

Server starts at **http://localhost:8080**

---

## Admin Account

An admin account is automatically created on first startup via `AdminSeeder.java`.

Set the admin credentials using environment variables:

| Variable | Description |
|----------|-------------|
| `ADMIN_EMAIL` | Admin login email |
| `ADMIN_PASSWORD` | Admin login password |
| `ADMIN_NAME` | Admin display name |

If not set, the seeder uses safe defaults defined in `AdminSeeder.java`.

To grant admin access to an existing user, set their `role` field to `ADMIN` in MongoDB Atlas.

---

## API Reference

All endpoints are prefixed with `/api`. Protected routes require:
```
Authorization: Bearer <jwt_token>
```

---

### Auth

#### Register
```
POST /api/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123"
}
```
Response `201 Created`:
```json
{ "message": "User registered successfully" }
```

---

#### Login
```
POST /api/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "secret123"
}
```
Response `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "USER",
  "name": "John Doe",
  "userId": "64abc..."
}
```

---

### Foods

#### Get all foods
```
GET /api/foods
```

#### Add food (Admin)
```
POST /api/foods
Authorization: Bearer <admin_token>
Content-Type: multipart/form-data

name=Chicken Biryani
description=Aromatic basmati rice with spices
price=250
category=Biryani
image=<file>
```

#### Delete food (Admin)
```
DELETE /api/foods/{id}
Authorization: Bearer <admin_token>
```

---

### Cart

#### Get cart
```
GET /api/cart
Authorization: Bearer <token>
```

#### Add item
```
POST /api/cart
Authorization: Bearer <token>
Content-Type: application/json

{ "foodId": "64abc..." }
```

#### Remove one quantity
```
POST /api/cart/remove
Authorization: Bearer <token>
Content-Type: application/json

{ "foodId": "64abc..." }
```

#### Clear cart
```
DELETE /api/cart/clear
Authorization: Bearer <token>
```

---

### Orders

#### Create order
```
POST /api/orders/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "userAddress": "123 Main St, Mumbai",
  "phoneNumber": "9876543210",
  "email": "john@example.com",
  "amount": 385.00,
  "orderedItems": [...]
}
```

#### Verify payment
```
POST /api/orders/verify
Authorization: Bearer <token>
Content-Type: application/json

{
  "razorpay_payment_id": "pay_...",
  "razorpay_order_id": "order_...",
  "razorpay_signature": "..."
}
```

#### Get user's orders
```
GET /api/orders
Authorization: Bearer <token>
```

#### Get all orders (Admin)
```
GET /api/orders/all
Authorization: Bearer <admin_token>
```

#### Update order status (Admin)
```
PUT /api/orders/{id}/status
Authorization: Bearer <admin_token>
Content-Type: application/json

{ "orderStatus": "out for delivery" }
```

---

### Users (Admin)

#### Get all users
```
GET /api/users
Authorization: Bearer <admin_token>
```

#### Update user status
```
PUT /api/users/{id}/status
Authorization: Bearer <admin_token>
Content-Type: application/json

{ "accountStatus": "BLOCKED" }
```

#### Delete user
```
DELETE /api/users/{id}
Authorization: Bearer <admin_token>
```

---

## MongoDB Collections

Collections are created automatically on first insert — no migrations needed.

| Collection | Description |
|------------|-------------|
| `users` | Registered users and admins |
| `foods` | Food menu items |
| `carts` | Per-user shopping carts |
| `orders` | All placed orders |
| `loginhistory` | User login tracking |

---

## MongoDB Atlas Setup

1. Go to [MongoDB Atlas](https://cloud.mongodb.com)
2. **Network Access** → Add IP `0.0.0.0/0` for development, or your server IP for production
3. **Database Access** → Create a user with `readWrite` access on your database
4. Copy the connection string and set it as `MONGODB_URI`

---

## Environment Variables (Production)

Use `application-prod.properties` — all values come from environment variables:

```properties
spring.data.mongodb.uri=${MONGODB_URI}
jwt.secret.key=${JWT_SECRET}
razorpay.key=${RAZORPAY_KEY}
razorpay.secret=${RAZORPAY_SECRET}
cors.allowed-origins=${CORS_ORIGINS}
server.port=${PORT:8080}
```

Activate the prod profile:
```bash
java -jar target/foodiesapi-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## Deployment

### Render (Docker — recommended)

Render does not have a native Java runtime. Use the included `Dockerfile` instead.

1. Push backend to GitHub
2. Go to [render.com](https://render.com) → **New Web Service**
3. Connect your GitHub repo
4. **Runtime:** select **Docker** (not Java)
5. Render will automatically use the `Dockerfile` in the repo root
6. Add environment variables in the Render dashboard:

| Key | Value |
|-----|-------|
| `MONGODB_URI` | Your Atlas connection string |
| `JWT_SECRET` | Random 32+ char string |
| `RAZORPAY_KEY` | Your Razorpay key ID |
| `RAZORPAY_SECRET` | Your Razorpay key secret |
| `CORS_ORIGINS` | Your Vercel frontend URLs (comma-separated) |

7. Deploy — Render builds the Docker image and starts the container

The included `Dockerfile` uses a multi-stage build:
- **Stage 1:** Maven + JDK 21 builds the JAR
- **Stage 2:** JRE 21 runs the JAR (smaller final image)

---

### Railway

1. New project on [railway.app](https://railway.app)
2. Deploy from GitHub
3. Add environment variables in the Railway dashboard
4. Railway auto-detects the `Dockerfile`

---

### VPS (Ubuntu + systemd)

```bash
# Build JAR locally
mvnw.cmd clean package -DskipTests

# Copy to server
scp target/foodiesapi-0.0.1-SNAPSHOT.jar user@yourserver:/opt/foodiesapi/

# Create /etc/systemd/system/foodiesapi.service
[Unit]
Description=Foodies API Backend
After=network.target

[Service]
User=ubuntu
EnvironmentFile=/opt/foodiesapi/.env
ExecStart=/usr/bin/java -jar /opt/foodiesapi/foodiesapi-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always

[Install]
WantedBy=multi-user.target

# Enable and start
sudo systemctl enable foodiesapi
sudo systemctl start foodiesapi
sudo systemctl status foodiesapi
```

---

## Security Notes

- Passwords are hashed with **BCrypt** — never stored in plain text
- JWT tokens expire after **24 hours** (configurable via `jwt.expiration`)
- Admin endpoints are protected by role check (`ADMIN`)
- Never commit `application.properties` with real credentials
- Use environment variables for all secrets in production

---

## .gitignore

Add these to your `.gitignore` to avoid committing secrets:

```
target/
*.jar
*.class
.env
src/main/resources/application-local.properties
```

The main `application.properties` uses `${ENV_VAR:default}` syntax — safe to commit as long as defaults don't contain real production secrets.

---

## License

MIT
