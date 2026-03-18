# Online Food Ordering and Delivery Platform

A full-stack cloud-native food ordering and delivery platform built as a university project. Features real-time order tracking, role-based access control, and a modern dark/light theme UI.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 14, TypeScript, Tailwind CSS |
| Backend | Java 21, Spring Boot 3.x, Maven |
| Database | MongoDB 7 |
| Auth | JWT (jjwt) + BCrypt |
| Containerization | Docker + Docker Compose |

## Prerequisites

- Docker Desktop (required for Quick Start)
- Node.js 20+ (for local frontend dev)
- Java 21+ (for local backend dev)
- Maven 3.9+ (for local backend dev)

## Quick Start (Docker)

```bash
git clone <repo-url>
cd Cloud-Native-Project
chmod +x start.sh stop.sh
./start.sh
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| MongoDB | localhost:27017 |

To stop all services:

```bash
./stop.sh
```

## Manual Local Development

### 1. Start MongoDB

```bash
docker-compose up mongo -d
```

### 2. Start Backend

```bash
cd backend
mvn spring-boot:run
```

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGO_URI` | `mongodb://admin:password@localhost:27017/foodplatform?authSource=admin` | MongoDB connection string |
| `JWT_SECRET` | `your-256-bit-secret-key-for-food-platform-app` | JWT signing key |
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080` | Backend API URL for frontend |

## Test Accounts

The database is automatically seeded on first run with the following accounts:

| Email | Password | Role |
|---|---|---|
| admin@food.com | Admin123! | ADMIN |
| owner1@food.com | Owner123! | RESTAURANT_OWNER |
| owner2@food.com | Owner123! | RESTAURANT_OWNER |
| owner3@food.com | Owner123! | RESTAURANT_OWNER |
| customer@food.com | Customer123! | CUSTOMER |
| driver@food.com | Driver123! | DELIVERY_DRIVER |

## API Reference

### Auth (Public)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register a new user |
| POST | `/api/auth/login` | No | Login and get JWT token |
| GET | `/api/auth/me` | JWT | Get current user profile |

### Restaurants

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/restaurants` | No | List restaurants (supports `?search=&cuisine=`) |
| GET | `/api/restaurants/:id` | No | Get restaurant details |
| POST | `/api/restaurants` | RESTAURANT_OWNER | Create a restaurant |
| PUT | `/api/restaurants/:id` | RESTAURANT_OWNER | Update own restaurant |
| DELETE | `/api/restaurants/:id` | RESTAURANT_OWNER | Delete own restaurant |

### Menu

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/restaurants/:id/menu` | No | Get restaurant menu |
| POST | `/api/restaurants/:id/menu` | RESTAURANT_OWNER | Add menu item |
| PUT | `/api/restaurants/:id/menu/:itemId` | RESTAURANT_OWNER | Update menu item |
| DELETE | `/api/restaurants/:id/menu/:itemId` | RESTAURANT_OWNER | Delete menu item |

### Cart

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/cart` | CUSTOMER | Get cart |
| POST | `/api/cart/add` | CUSTOMER | Add item to cart |
| PUT | `/api/cart/update` | CUSTOMER | Update cart item quantity |
| DELETE | `/api/cart/clear` | CUSTOMER | Clear cart |

### Orders

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/orders` | CUSTOMER | Place order from cart |
| GET | `/api/orders` | JWT | Get orders (role-filtered) |
| GET | `/api/orders/:id` | JWT | Get order details |
| PUT | `/api/orders/:id/status` | JWT | Update order status (role-based) |
| POST | `/api/orders/:id/pay` | CUSTOMER | Simulate payment |
| PUT | `/api/orders/:id/cancel` | CUSTOMER | Cancel pending order |

### Admin

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/admin/users` | ADMIN | List all users |
| GET | `/api/admin/orders` | ADMIN | List all orders |
| PUT | `/api/admin/users/:id/role` | ADMIN | Update user role |

## Folder Structure

```
Cloud-Native-Project/
├── backend/
│   ├── src/main/java/com/foodplatform/
│   │   ├── config/          # Security, CORS, exception handler, data seeder
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/             # Request/Response DTOs
│   │   ├── model/           # MongoDB document models
│   │   ├── repository/      # Spring Data MongoDB repositories
│   │   ├── security/        # JWT utility and auth filter
│   │   ├── service/         # Business logic services
│   │   └── FoodPlatformApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── app/             # Next.js App Router pages
│   │   ├── components/      # Reusable UI components
│   │   ├── context/         # React context providers (Auth, Theme)
│   │   ├── lib/             # Axios instance, Zustand cart store
│   │   └── types/           # TypeScript interfaces
│   ├── Dockerfile
│   ├── next.config.ts
│   ├── tailwind.config.ts
│   └── package.json
├── docker-compose.yml
├── start.sh
├── stop.sh
└── README.md
```

## Cloud Migration Note

The commit labelled "Starting Point" marks the baseline before Azure migration (Cosmos DB, App Service, Container Registry).

