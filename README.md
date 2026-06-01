# 🍔 Food Delivery App — Backend

REST API backend for the Food Delivery application. Built with **Spring Boot**, secured with **JWT**, connected to **MongoDB Atlas**, and integrated with **Razorpay** for payments.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x |
| Language | Java 17+ |
| Database | MongoDB Atlas |
| Authentication | JWT (JSON Web Tokens) |
| Security | Spring Security |
| Payment | Razorpay |
| Build Tool | Maven |
| File Upload | Spring Multipart |

---

## Project Structure

```
backend/
├── src/
│   └── main/
│       ├── java/com/fooddelivery/
│       │   ├── config/
│       │   │   ├── SecurityConfig.java       # Spring Security + CORS config
│       │   │   └── JwtConfig.java            # JWT filter & token util
│       │   ├── controller/
│       │   │   ├── AuthController.java       # /api/register, /api/login
│       │   │   ├── FoodController.java       # /api/foods
│       │   │   ├── CartController.java       # /api/cart
│       │   │   └── OrderController.java      # /api/orders
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── Food.java
│       │   │   ├── Cart.java
│       │   │   ├── Order.java
│       │   │   └── OrderItem.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── FoodRepository.java
│       │   │   ├── CartRepository.java
│       │   │   └── OrderRepository.java
│       │   └── service/
│       │       ├── AuthService.java
│       │       ├── FoodService.java
│       │       ├── CartService.java
│       │       └── OrderService.java
│       └── resources/
│           ├── application.properties        # Local config
│           └── application-prod.properties   # Production config (uses env vars)
├── pom.xml
└── README.md
```

---

## Prerequisites

- Java JDK 17+ → https://adoptium.net
- Maven 3.8+ → https://maven.apache.org
- MongoDB Atlas account → https://cloud.mongodb.com ✅ (already provisioned)

---

## Setup & Run Locally

### 1. Clone the repository

```bash
git clone https://github.com/Souravkadam/Fooddelivery_app.git
cd Fooddelivery_app/backend
```

### 2. Configure application.properties

Edit `src/main/resources/application.properties`:

```properties
# ── MongoDB Atlas ──────────────────────────────────────────────
spring.data.mongodb.uri=mongodb+srv://foodadmin:<your-password>@cluster0.v41emax.mongodb.net/fooddelivery?retryWrites=true&w=majority&appName=Cluster0
spring.data.mongodb.database=fooddelivery

# ── JWT ────────────────────────────────────────────────────────
jwt.secret=your_jwt_secret_key_min_32_chars_here
jwt.expiration=86400000

# ── Razorpay ───────────────────────────────────────────────────
razorpay.key.id=rzp_test_S5KOFCUas1wpGU
razorpay.key.secret=your_razorpay_key_secret

# ── File Upload ────────────────────────────────────────────────
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# ── CORS ───────────────────────────────────────────────────────
cors.allowed-origins=http://localhost:5173,http://localhost:5174

# ── Server ─────────────────────────────────────────────────────
server.port=8080
```

> ⚠️ Never commit `application.properties` with real secrets to Git. Add it to `.gitignore`.

### 3. Run the server

```bash
# Option A — Maven wrapper (recommended)
./mvnw spring-boot:run

# Option B — Build JAR then run
./mvnw clean package -DskipTests
java -jar target/fooddelivery-*.jar
```

Server starts at **http://localhost:8080**

---

## pom.xml Dependencies

```xml
<dependencies>

    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MongoDB -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Razorpay -->
    <dependency>
        <groupId>com.razorpay</groupId>
        <artifactId>razorpay-java</artifactId>
        <version>1.4.3</version>
    </dependency>

    <!-- Lombok (optional, reduces boilerplate) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```

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
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

---

### Foods

#### Get all foods
```
GET /api/foods
```
Response `200 OK`:
```json
[
  {
    "id": "64abc...",
    "name": "Chicken Biryani",
    "description": "Aromatic basmati rice...",
    "price": 250.0,
    "category": "Biryani",
    "imageUrl": "http://localhost:8080/images/biryani.jpg"
  }
]
```

---

#### Get food by ID
```
GET /api/foods/{id}
```
Response `200 OK`: single food object

---

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
Response `201 Created`: created food object

---

#### Delete food (Admin)
```
DELETE /api/foods/{id}
Authorization: Bearer <admin_token>
```
Response `200 OK`: `{ "message": "Food deleted" }`

---

### Cart

#### Get cart
```
GET /api/cart
Authorization: Bearer <token>
```
Response `200 OK`:
```json
{
  "items": {
    "64abc...": 2,
    "64def...": 1
  }
}
```

---

#### Add item to cart
```
POST /api/cart
Authorization: Bearer <token>
Content-Type: application/json

{ "foodId": "64abc..." }
```
Response `200 OK`: updated cart

---

#### Remove one quantity
```
POST /api/cart/remove
Authorization: Bearer <token>
Content-Type: application/json

{ "foodId": "64abc..." }
```
Response `200 OK`: updated cart

---

#### Clear cart
```
DELETE /api/cart/clear
Authorization: Bearer <token>
```
Response `200 OK`: `{ "message": "Cart cleared" }`

---

### Orders

#### Create order
```
POST /api/orders/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "userAddress": "John Doe, 123 Main St, Mumbai, Maharashtra, 400001",
  "phoneNumber": "9876543210",
  "email": "john@example.com",
  "amount": 385.00,
  "orderStatus": "preparing",
  "orderedItems": [
    {
      "foodId": "64abc...",
      "name": "Chicken Biryani",
      "description": "...",
      "category": "Biryani",
      "imageUrl": "...",
      "quantities": 2,
      "price": 500.00
    }
  ]
}
```
Response `201 Created`:
```json
{
  "id": "64xyz...",
  "razorpayOrderId": "order_ABC123",
  "amount": 38500,
  "currency": "INR"
}
```

---

#### Verify payment
```
POST /api/orders/verify
Authorization: Bearer <token>
Content-Type: application/json

{
  "razorpay_payment_id": "pay_ABC...",
  "razorpay_order_id": "order_ABC...",
  "razorpay_signature": "abc123..."
}
```
Response `200 OK`: `{ "message": "Payment verified" }`

---

#### Get user's orders
```
GET /api/orders
Authorization: Bearer <token>
```
Response `200 OK`: array of order objects

---

#### Get all orders (Admin)
```
GET /api/orders/all
Authorization: Bearer <admin_token>
```
Response `200 OK`: array of all orders

---

#### Update order status (Admin)
```
PUT /api/orders/{id}/status
Authorization: Bearer <admin_token>
Content-Type: application/json

{ "orderStatus": "out for delivery" }
```
Response `200 OK`: updated order

---

#### Delete order
```
DELETE /api/orders/{id}
Authorization: Bearer <token>
```
Response `200 OK`: `{ "message": "Order deleted" }`

---

## MongoDB Document Models

### User
```java
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;       // BCrypt hashed
    private String role = "USER";  // USER | ADMIN
}
```

### Food
```java
@Document(collection = "foods")
public class Food {
    @Id
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
}
```

### Cart
```java
@Document(collection = "carts")
public class Cart {
    @Id
    private String id;
    private String userId;
    private Map<String, Integer> items = new HashMap<>(); // foodId → qty
}
```

### Order
```java
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String userId;
    private String userAddress;
    private String phoneNumber;
    private String email;
    private List<OrderItem> orderedItems;
    private double amount;
    private String orderStatus = "preparing";
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt = LocalDateTime.now();
}
```

### OrderItem (embedded in Order)
```java
public class OrderItem {
    private String foodId;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private int quantities;
    private double price;
}
```

---

## MongoDB Atlas Setup

1. Go to [MongoDB Atlas](https://cloud.mongodb.com)
2. **Network Access** → Add IP `0.0.0.0/0` (dev) or your server IP (prod)
3. **Database Access** → Confirm user `foodadmin` has `readWrite` on `fooddelivery`
4. Collections are created automatically on first insert — no migrations needed

---

## Environment Variables (Production)

Use `application-prod.properties` with environment variables — never hardcode secrets:

```properties
spring.data.mongodb.uri=${MONGODB_URI}
jwt.secret=${JWT_SECRET}
razorpay.key.id=${RAZORPAY_KEY_ID}
razorpay.key.secret=${RAZORPAY_KEY_SECRET}
cors.allowed-origins=${CORS_ORIGINS}
```

Activate the prod profile:
```bash
java -jar target/fooddelivery-*.jar --spring.profiles.active=prod
```

---

## Deployment

### Render (recommended free tier)

1. Push backend to GitHub
2. New Web Service on [render.com](https://render.com)
3. Build command: `./mvnw clean package -DskipTests`
4. Start command: `java -jar target/fooddelivery-*.jar`
5. Add env vars in the Render dashboard

### Railway

1. New project on [railway.app](https://railway.app)
2. Deploy from GitHub
3. Add env vars — no DB plugin needed (using Atlas)

### VPS (Ubuntu + systemd)

```bash
# Copy JAR
scp target/fooddelivery-*.jar user@yourserver:/opt/fooddelivery/

# /etc/systemd/system/fooddelivery.service
[Unit]
Description=Food Delivery Backend
After=network.target

[Service]
User=ubuntu
EnvironmentFile=/opt/fooddelivery/.env
ExecStart=/usr/bin/java -jar /opt/fooddelivery/fooddelivery.jar --spring.profiles.active=prod
Restart=always

[Install]
WantedBy=multi-user.target

# Start
sudo systemctl enable fooddelivery
sudo systemctl start fooddelivery
sudo systemctl status fooddelivery
```

---

## Security Notes

- Passwords are hashed with **BCrypt** — never stored in plain text
- JWT tokens expire after **24 hours** (configurable via `jwt.expiration`)
- Admin endpoints should be protected by role check (`ROLE_ADMIN`)
- Never commit `application.properties` with real credentials — use `.gitignore`
- Rotate your MongoDB Atlas password if it was ever exposed publicly

---

## .gitignore (add to backend root)

```
target/
*.jar
*.class
src/main/resources/application.properties
src/main/resources/application-prod.properties
.env
```

---

## License

MIT
