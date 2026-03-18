Part 1 - Start point
# Online Food Ordering and Delivery Platform

A full-stack cloud-native food ordering and delivery platform built as a university project. Features real-time order tracking with SSE notifications, role-based dashboards for customers, restaurant owners, and delivery drivers, and a modern dark/light theme UI.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 14, TypeScript, Tailwind CSS, Zustand |
| Backend | Java 21, Spring Boot 3.2, Maven |
| Database | MongoDB 7 |
| Auth | JWT (jjwt) + BCrypt |
| Real-time | Server-Sent Events (SSE) |
| Containerization | Docker + Docker Compose |
| Testing | JUnit 5 + Mockito (backend), Jest + React Testing Library (frontend) |

## Features

- **11 restaurants** across 12 cuisine types with full menus (10-15 items each)
- **1650+ orders** with 3 months of realistic data history
- **Real-time order tracking** with SSE notifications across all roles
- **Order flow**: PENDING → CONFIRMED → PREPARING → (5s auto) → READY → PICKED_UP → ARRIVED → DELIVERED
- **Role-based dashboards**: Customer order history, Owner order management, Driver delivery workflow
- **Cart & checkout** with promo code support and simulated payments
- **Admin panel** for user and order management
- **Owner analytics** dashboard with revenue charts
- **Reviews & ratings** system (~400 seeded reviews)
- **Dark/light theme** with localStorage persistence
- **Responsive design** with Tailwind CSS

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
| Mongo Express | http://localhost:8081 |
| MongoDB | localhost:27017 |

To stop all services:

```bash
./stop.sh
```

To reset data (rebuild with fresh seed):

```bash
docker compose down -v
docker compose up --build -d
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
| owner1@food.com – owner11@food.com | Owner123! | RESTAURANT_OWNER |
| customer1@food.com – customer10@food.com | Customer123! | CUSTOMER |
| driver1@food.com, driver2@food.com | Driver123! | DELIVERY_DRIVER |

## Order Flow

```
PENDING ──► CONFIRMED ──► PREPARING ──(5s auto)──► READY ──► PICKED_UP ──► ARRIVED ──► DELIVERED
   │                                                  │
   └──► CANCELLED                                     └── notifies all drivers via SSE
```

- **Customer** places order → status PENDING
- **Owner** confirms (→ CONFIRMED), starts preparing (→ PREPARING)
- **System** auto-transitions PREPARING → READY after 5 seconds
- **Driver** picks up (→ PICKED_UP), arrives (→ ARRIVED), delivers (→ DELIVERED)
- SSE notifications keep all roles in sync in real-time

## Seeded Data

| Entity | Count | Details |
|---|---|---|
| Restaurants | 11 | Italian, Japanese, American, Mexican, Indian, Thai, French, Mediterranean, Brazilian, Korean, Vegan |
| Menu Items | ~140 | 10-15 items per restaurant across 3 categories each |
| Customers | 10 | Realistic profiles |
| Drivers | 2 | Assigned to delivered orders |
| Orders | ~1750 | Spread across 3 months with realistic time distribution |
| Reviews | ~400 | Rating distribution: 40% 5-star, 30% 4-star, 15% 3-star |
| Promo Codes | 7 | Various discount types |

## Testing

### Backend (125 tests)

```bash
cd backend
mvn test
```

| Category | Tests | Coverage |
|---|---|---|
| Service unit tests | ~81 | OrderService, CartService, RestaurantService, MenuService, AdminService, AuthService, NotificationService |
| Controller integration tests | ~32 | AuthController, CartController, RestaurantController, MenuController, AdminController, OrderController |
| Model/DTO/Security tests | ~12 | Order, ApiResponse, JwtUtil |

### Frontend (47 tests)

```bash
cd frontend
npx jest
```

| Category | Tests | Coverage |
|---|---|---|
| Component tests | ~19 | OrderStatusStepper, ProtectedRoute, Navbar, SearchBar |
| Context tests | ~6 | AuthContext, ThemeContext |
| Lib tests | ~11 | Axios interceptors, cart store |
| Page integration tests | ~11 | Owner dashboard, Driver dashboard, Order detail |

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

### Notifications

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/notifications/stream` | JWT | SSE stream for real-time notifications |

### Reviews

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/reviews` | CUSTOMER | Submit a review for a delivered order |
| GET | `/api/reviews/restaurant/:id` | No | Get reviews for a restaurant |
| GET | `/api/reviews/my` | CUSTOMER | Get current user's reviews |

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
│   ├── src/test/java/com/foodplatform/
│   │   ├── controller/      # Controller integration tests
│   │   ├── dto/             # DTO unit tests
│   │   ├── model/           # Model unit tests
│   │   ├── security/        # JWT unit tests
│   │   └── service/         # Service unit tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── __tests__/       # Jest test suites
│   │   ├── app/             # Next.js App Router pages
│   │   ├── components/      # Reusable UI components
│   │   ├── context/         # React context providers (Auth, Theme)
│   │   ├── lib/             # Axios instance, Zustand cart store
│   │   └── types/           # TypeScript interfaces
│   ├── Dockerfile
│   ├── jest.config.ts
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
