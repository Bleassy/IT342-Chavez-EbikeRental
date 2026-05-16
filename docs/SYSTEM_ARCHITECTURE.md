# eBike Rental System - Architecture Diagram

## System Overview

```mermaid
graph TB
    subgraph "USERS & ACTORS"
        Admin["👤 Admin User"]
        Customer["👤 Customer/User"]
        Guest["👤 Guest/Anonymous"]
    end

    subgraph "PRESENTATION LAYER"
        Web["🌐 Web Frontend<br/>React + TypeScript<br/>Port: 5173"]
        Mobile["📱 Mobile App<br/>Android/Kotlin<br/>Gradle"]
        API["📡 REST API Documentation<br/>Swagger/OpenAPI"]
    end

    subgraph "GATEWAY & SECURITY"
        CORS["🔒 CORS Configuration"]
        JWT["🔐 JWT Authentication<br/>Token-based Auth"]
        SecurityConfig["⚙️ Security Filter Chain"]
    end

    subgraph "APPLICATION LAYER"
        AuthModule["🔑 Authentication Module<br/>- User Registration<br/>- User Login<br/>- JWT Token Generation<br/>- OAuth2 (Google Auth)"]
        
        BikeModule["🚲 Bike Management Module<br/>- Get All Bikes<br/>- Get Bike Details<br/>- Bike Availability Status<br/>- Pricing Configuration"]
        
        BookingModule["📅 Booking Module<br/>- Create Booking<br/>- Get User Bookings<br/>- Cancel Booking<br/>- Price Calculation"]
        
        PaymentModule["💳 Payment Module<br/>- Stripe Integration<br/>- GCash Integration<br/>- Payment Processing<br/>- Transaction History"]
        
        ProfileModule["👤 User Profile Module<br/>- Update Profile<br/>- View Profile<br/>- Manage Preferences"]
        
        AdminModule["⚙️ Admin Module<br/>- Manage Users<br/>- Manage Bikes<br/>- View Analytics<br/>- System Reports"]
    end

    subgraph "DATA ACCESS LAYER"
        JPA["🗄️ JPA/Hibernate ORM<br/>Object-Relational Mapping"]
        Repository["📚 Repository Pattern<br/>UserRepository<br/>BikeRepository<br/>BookingRepository<br/>PaymentRepository"]
    end

    subgraph "DATABASE LAYER"
        PostgreSQL["🗄️ PostgreSQL Database<br/>Neon Cloud Hosting<br/>Production Environment"]
        H2["💾 H2 In-Memory DB<br/>Testing Environment"]
    end

    subgraph "EXTERNAL SERVICES"
        Stripe["💰 Stripe API<br/>Payment Processing"]
        GCash["💵 GCash Payment<br/>Alternative Payment"]
        GoogleAuth["🔐 Google OAuth 2.0<br/>Social Login"]
    end

    subgraph "INFRASTRUCTURE & TOOLS"
        Maven["🛠️ Maven Build Tool<br/>Dependency Management"]
        Testing["✅ JUnit 5 + Spring Test<br/>MockMvc Testing"]
        Logging["📝 Application Logging<br/>SLF4J/Logback"]
    end

    %% User Connections
    Admin -->|Uses| Web
    Admin -->|Uses| API
    Customer -->|Uses| Web
    Customer -->|Uses| Mobile
    Guest -->|Browses| Web

    %% Frontend to Security
    Web -->|HTTP Requests| CORS
    Mobile -->|HTTP Requests| CORS
    
    %% Security to Auth & Gateway
    CORS -->|Authenticates| JWT
    JWT -->|Validates| SecurityConfig

    %% Security to Application Layer
    SecurityConfig -->|Routes to| AuthModule
    SecurityConfig -->|Routes to| BikeModule
    SecurityConfig -->|Routes to| BookingModule
    SecurityConfig -->|Routes to| PaymentModule
    SecurityConfig -->|Routes to| ProfileModule
    SecurityConfig -->|Routes to| AdminModule

    %% Application Layer to Data Layer
    AuthModule -->|Uses| JPA
    BikeModule -->|Uses| JPA
    BookingModule -->|Uses| JPA
    PaymentModule -->|Uses| JPA
    ProfileModule -->|Uses| JPA
    AdminModule -->|Uses| JPA

    %% Data Layer to Repository
    JPA -->|Implements| Repository

    %% Repository to Database
    Repository -->|Reads/Writes| PostgreSQL
    Repository -->|Tests with| H2

    %% External Services
    PaymentModule -->|Processes Payment| Stripe
    PaymentModule -->|Alternative Payment| GCash
    AuthModule -->|Social Login| GoogleAuth

    %% Infrastructure Support
    Maven -.->|Builds| Web
    Maven -.->|Builds| Mobile
    Maven -.->|Builds| AuthModule
    Testing -.->|Tests All Modules| Repository
    Logging -.->|Logs Events| PostgreSQL

    style Admin fill:#e1f5ff
    style Customer fill:#e1f5ff
    style Guest fill:#e1f5ff
    style Web fill:#fff3e0
    style Mobile fill:#fff3e0
    style API fill:#fff3e0
    style CORS fill:#f3e5f5
    style JWT fill:#f3e5f5
    style SecurityConfig fill:#f3e5f5
    style AuthModule fill:#c8e6c9
    style BikeModule fill:#c8e6c9
    style BookingModule fill:#c8e6c9
    style PaymentModule fill:#c8e6c9
    style ProfileModule fill:#c8e6c9
    style AdminModule fill:#c8e6c9
    style JPA fill:#ffccbc
    style Repository fill:#ffccbc
    style PostgreSQL fill:#bbdefb
    style H2 fill:#bbdefb
    style Stripe fill:#ffe0b2
    style GCash fill:#ffe0b2
    style GoogleAuth fill:#ffe0b2
```

---

## Architecture Components Breakdown

### 1. **Users & Actors**
- **Admin Users**: Manage system, view analytics, manage bikes and users
- **Customers/Registered Users**: Rent bikes, make payments, view booking history
- **Guest Users**: Browse available bikes (limited access)

### 2. **Presentation Layer**
| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Web Frontend** | React 18 + TypeScript + Tailwind | User-facing web interface on port 5173 |
| **Mobile App** | Android/Kotlin + Gradle | Native mobile app for on-the-go access |
| **API** | REST API + Swagger | API documentation and testing interface |

### 3. **Gateway & Security Layer**
| Component | Function |
|-----------|----------|
| **CORS Configuration** | Cross-Origin Resource Sharing for frontend access |
| **JWT Authentication** | Token-based authentication for secure requests |
| **Security Filter Chain** | Spring Security filters for endpoint authorization |

### 4. **Application Layer (Core Business Logic)**

#### Authentication Module (`/auth`)
- User registration with email validation
- User login with JWT token generation
- OAuth2 integration with Google
- Token refresh and validation

#### Bike Management Module (`/bikes`)
- Display all available bikes with filters
- Get detailed bike information
- Check bike availability status
- Display pricing (hourly & daily rates)
- Manage bike inventory

#### Booking Module (`/bookings`)
- Create new bike reservations
- View user's booking history
- Calculate rental prices based on duration
- Handle booking cancellations
- Manage booking status (PENDING, CONFIRMED, COMPLETED, CANCELLED)

#### Payment Module (`/payments`)
- Process payments via Stripe
- Process payments via GCash
- Store transaction history
- Handle payment failures and retries
- Generate payment receipts

#### User Profile Module (`/profile`)
- Update user information
- View profile details
- Manage saved payment methods
- Update preferences

#### Admin Module (`/admin`)
- Manage users (view, edit, deactivate)
- Manage bikes (create, update, delete)
- View system analytics
- Generate reports
- Access control management

### 5. **Data Access Layer**
| Technology | Purpose |
|-----------|---------|
| **JPA/Hibernate** | Object-Relational Mapping (ORM) framework |
| **Repository Pattern** | Data access abstraction layer |

**Repositories:**
- UserRepository
- BikeRepository
- BookingRepository
- PaymentRepository

### 6. **Database Layer**

#### Production Database
- **PostgreSQL** hosted on **Neon** (Cloud Database)
- Tables:
  - `users` - User accounts and authentication
  - `bikes` - Bike inventory and specifications
  - `bookings` - Rental reservations
  - `payments` - Payment transactions
  - `bike_maintenance` - Service records

#### Testing Database
- **H2 In-Memory Database** - For unit and integration tests
- Automatically created during test runs
- Configured with `create-drop` DDL strategy

### 7. **External Services Integration**

| Service | Purpose | Integration |
|---------|---------|-------------|
| **Stripe** | Credit card payments | Payment processing API |
| **GCash** | Mobile payment | Alternative payment method |
| **Google OAuth 2.0** | Social authentication | User login with Google account |

### 8. **Infrastructure & Tools**

| Tool | Purpose |
|-----|---------|
| **Maven (mvnw)** | Build automation and dependency management |
| **Spring Boot 3.5.11** | Framework for backend application |
| **Java 17** | Programming language |
| **JUnit 5** | Unit testing framework |
| **MockMvc** | Testing REST controllers |
| **SLF4J/Logback** | Application logging |
| **Spring Security** | Authentication and authorization |

---

## Data Flow Example: Bike Booking Flow

```
1. User logs in
   └─> JWT Token received
   
2. User browses bikes
   └─> BikeController.getAllBikes()
   └─> BikeRepository queries PostgreSQL
   └─> Bikes data returned to frontend
   
3. User creates booking
   └─> BookingController.createBooking()
   └─> BookingService calculates price
   └─> Booking saved to PostgreSQL
   
4. User pays
   └─> PaymentController processes payment
   └─> PaymentService calls Stripe/GCash API
   └─> Transaction recorded in PostgreSQL
   
5. Booking confirmed
   └─> Email confirmation sent
   └─> Bike status updated to RENTED
```

---

## Technology Stack Summary

**Backend:**
- Java 17 + Spring Boot 3.5.11
- Spring Security + JWT
- Spring Data JPA + Hibernate
- PostgreSQL (Neon)

**Frontend:**
- React 18 + TypeScript
- Tailwind CSS
- Vite (build tool)

**Mobile:**
- Android (Kotlin)
- Gradle

**DevOps & Testing:**
- Maven build tool
- JUnit 5 + Spring Test
- H2 in-memory database for tests
- Git version control

**Payment Integration:**
- Stripe API
- GCash API

---

## System Security Features

✅ JWT Token-based authentication
✅ Spring Security filter chain
✅ Password encryption (BCrypt)
✅ CORS protection
✅ CSRF disabled for stateless API
✅ Role-based access control (ADMIN, USER roles)
✅ Input validation on all endpoints
✅ Secure payment processing

---

## Deployment & Port Configuration

| Component | Port | Environment |
|-----------|------|-------------|
| Web Frontend | 5173 | Development |
| Backend API | 8083 | Production (Neon) |
| Backend API | 8080/8081 | Development/Testing |
| PostgreSQL | 5432 | Cloud (Neon) |

---

*Last Updated: May 11, 2026*
*Architecture Version: 1.0*
