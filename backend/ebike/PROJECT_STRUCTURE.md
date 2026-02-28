# E-Bike Rental Backend - Project Structure Overview

## Complete File Structure

```
backend/ebike/
├── src/
│   ├── main/
│   │   ├── java/com/ebike/rental/
│   │   │   ├── EbikeApplication.java          # Main Spring Boot Application
│   │   │   │
│   │   │   ├── entity/                         # JPA Entity Classes
│   │   │   │   ├── User.java                   # User entity with roles
│   │   │   │   ├── Bike.java                   # Bike inventory entity
│   │   │   │   ├── Booking.java                # Booking/Rental entity
│   │   │   │   └── Payment.java                # Payment transaction entity
│   │   │   │
│   │   │   ├── dto/                            # Data Transfer Objects
│   │   │   │   ├── UserDTO.java                # User response DTO
│   │   │   │   ├── BikeDTO.java                # Bike response DTO
│   │   │   │   ├── BookingDTO.java             # Booking response DTO
│   │   │   │   ├── LoginRequest.java           # Login request DTO
│   │   │   │   ├── RegisterRequest.java        # Registration request DTO
│   │   │   │   └── ApiResponse.java            # Generic API response wrapper
│   │   │   │
│   │   │   ├── repository/                     # JPA Repository Interfaces
│   │   │   │   ├── UserRepository.java         # User CRUD & queries
│   │   │   │   ├── BikeRepository.java         # Bike CRUD & queries
│   │   │   │   ├── BookingRepository.java      # Booking CRUD & queries
│   │   │   │   └── PaymentRepository.java      # Payment CRUD & queries
│   │   │   │
│   │   │   ├── service/                        # Business Logic Services
│   │   │   │   ├── UserService.java            # User management logic
│   │   │   │   ├── BikeService.java            # Bike management logic
│   │   │   │   ├── BookingService.java         # Booking & price calculation
│   │   │   │   └── PaymentService.java         # Payment processing logic
│   │   │   │
│   │   │   ├── controller/                     # REST API Controllers
│   │   │   │   ├── HealthController.java       # Health check endpoints
│   │   │   │   ├── AuthController.java         # Login/Register endpoints
│   │   │   │   ├── BikeController.java         # Bike management endpoints
│   │   │   │   ├── BookingController.java      # Booking management endpoints
│   │   │   │   ├── PaymentController.java      # Payment endpoints
│   │   │   │   └── AdminController.java        # Admin user management
│   │   │   │
│   │   │   ├── config/                         # Configuration Classes
│   │   │   │   └── CorsConfig.java             # CORS configuration
│   │   │   │
│   │   │   └── security/                       # Security (Future: JWT, OAuth)
│   │   │       └── (placeholder for auth)
│   │   │
│   │   └── resources/
│   │       ├── application.properties          # Spring Boot config
│   │       └── sample-data.sql                 # Sample database data
│   │
│   └── test/                                   # Unit & Integration Tests
│       └── java/com/ebike/rental/
│
├── pom.xml                                     # Maven dependencies
├── BACKEND_README.md                           # Setup & running instructions
├── API_DOCUMENTATION.md                        # Complete API reference
└── mvnw, mvnw.cmd                             # Maven wrapper
```

## Component Descriptions

### 1. Entity Classes (entity/)
**Purpose:** Define database table structures using JPA annotations.

#### User.java
- Represents system users (Admin & Regular users)
- Fields: id, email, password, name, phone, address, role, isActive
- Relationships: One-to-Many with Bookings
- Timestamps: createdAt, updatedAt

#### Bike.java
- Represents rental bikes
- Fields: id, bikeCode, model, brand, color, year, type, price, status, condition
- Pricing: Hourly and daily rates
- Tracking: Battery level, location, condition
- Relationships: One-to-Many with Bookings

#### Booking.java
- Represents bike rental transactions
- Fields: id, startTime, endTime, status, totalPrice, notes
- Relationships: Many-to-One with User and Bike, One-to-One with Payment
- Status: PENDING, CONFIRMED, CANCELLED, COMPLETED
- Auto-calculates total price based on duration

#### Payment.java
- Represents payment transactions
- Fields: id, amount, paymentMethod, paymentStatus, transactionId
- Relationships: One-to-One with Booking
- Methods: CREDIT_CARD, DEBIT_CARD, PAYPAL, WALLET
- Status: PENDING, COMPLETED, FAILED, REFUNDED

### 2. DTO Classes (dto/)
**Purpose:** Transfer data between layers and API endpoints.

- **UserDTO**: Excludes password, used for responses
- **BikeDTO**: Converts enum values to strings for JSON
- **BookingDTO**: Simplifies complex relationships
- **LoginRequest/RegisterRequest**: Validates incoming data
- **ApiResponse\<T\>**: Wraps all API responses for consistency

### 3. Repository Interfaces (repository/)
**Purpose:** Handle database operations using Spring Data JPA.

- Extend `JpaRepository<Entity, Long>`
- Spring auto-implements CRUD methods
- Custom query methods for specific searches (e.g., findByEmail, findByStatus)
- Enable test queries with @Query annotation

### 4. Service Classes (service/)
**Purpose:** Implement business logic and validation.

#### UserService
- Register & login users
- User CRUD operations
- Fetch by email, phone, or role
- Manage active/inactive users

#### BikeService
- Create and manage bikes
- Filter by status, type, location, condition
- Update bike details and status
- Query available bikes

#### BookingService
- Create bookings with automatic price calculation
- Confirm, complete, or cancel bookings
- Get user booking history
- Manage bike availability status
- Calculate price: days × daily rate + remaining hours × hourly rate

#### PaymentService
- Process payments for bookings
- Generate unique transaction IDs
- Handle payment status (pending, completed, failed, refunded)
- Retrieve payment history

### 5. Controller Classes (controller/)
**Purpose:** Handle HTTP requests and send responses.

#### HealthController (GET /)
- Basic health check endpoints
- Verifies API is running

#### AuthController (POST /auth/...)
- User registration
- User login
- Validates email uniqueness

#### BikeController (GET/POST/PUT /bikes/...)
- All bike management operations
- Filter bikes by status, location, type
- Update bike information and status

#### BookingController (GET/POST/PUT /bookings/...)
- Create and manage bookings
- Get user's booking history
- Confirm, complete, or cancel bookings
- Auto-update bike status

#### PaymentController (GET/POST/PUT /payments/...)
- Process payments
- Track payment status
- Support refunds

#### AdminController (GET/PUT/DELETE /admin/users/...)
- Manage all users (admin-only)
- Deactivate users
- Edit user information

### 6. Configuration (config/)
**Purpose:** Spring Boot application configuration.

#### CorsConfig.java
- Enables Cross-Origin Resource Sharing
- Allows frontend to communicate with backend
- Configured for all methods: GET, POST, PUT, DELETE

### 7. application.properties
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/ebike_rental_db
spring.datasource.username=root
spring.datasource.password=chavez01

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update  # Auto-creates tables
spring.jpa.show-sql=true              # Logs SQL queries

# Server Configuration
server.port=8080
server.servlet.context-path=/api      # All APIs prefixed with /api
```

## Data Flow Example: Create Booking

```
1. Frontend sends POST request
   POST /api/bookings?userId=1&bikeId=1&...

2. BookingController receives request
   └─ Validates parameters

3. BookingService.createBooking() executes
   ├─ Retrieves User from UserRepository
   ├─ Retrieves Bike from BikeRepository
   ├─ Calculates price
   ├─ Creates Booking entity
   ├─ Updates Bike status to RENTED
   └─ Saves via BookingRepository

4. Database operations
   ├─ INSERT into bookings
   └─ UPDATE bikes SET status='RENTED'

5. Controller converts to DTO
   └─ Returns BookingDTO in ApiResponse

6. Frontend receives JSON response
   {
     "success": true,
     "message": "Booking created successfully",
     "data": { booking details }
   }
```

## Key Features

### 1. Price Calculation
- Hourly rate: $2.50 - $7.00
- Daily rate: $15.00 - $40.00
- Automatic calculation based on duration
- Price = (Days × Daily Rate) + (Remaining Hours × Hourly Rate)

### 2. Bike Availability Management
- Status automatically updates with bookings
- Available → Rented (on booking) → Available (on completion)
- Admins can set MAINTENANCE or UNAVAILABLE status

### 3. User Role System
- **ADMIN**: Full system access, manage users and bikes
- **USER**: Can view bikes and create bookings

### 4. Booking Status Workflow
```
PENDING → CONFIRMED → COMPLETED
        └─→ CANCELLED
```

### 5. Payment Processing
- Multiple payment methods supported
- Transaction ID tracking
- Refund capability
- Status management

## Database Schema

### Tables Created Automatically

1. **users** (id, email, password, first_name, last_name, phone, address, role, is_active, created_at, updated_at)

2. **bikes** (id, bike_code, model, brand, color, year, type, price_per_hour, price_per_day, status, description, image_url, condition, battery_level, location, created_at, updated_at)

3. **bookings** (id, user_id, bike_id, start_time, end_time, status, total_price, notes, created_at, updated_at)

4. **payments** (id, booking_id, amount, payment_method, payment_status, transaction_id, notes, created_at, updated_at)

## Running the Application

### 1. Build
```bash
mvn clean install
```

### 2. Run
```bash
mvn spring-boot:run
```

### 3. Test
Access: `http://localhost:8080/api`

### 4. Import Sample Data (Optional)
In MySQL Workbench:
```sql
USE ebike_rental_db;
SOURCE sample-data.sql;
```

## Future Enhancements

1. **Authentication & Security**
   - JWT token-based authentication
   - Role-based access control (RBAC)
   - Password encryption with BCrypt

2. **Advanced Features**
   - Real-time bike tracking (GPS)
   - User reviews and ratings
   - Promotional codes/discounts
   - Email notifications
   - SMS notifications

3. **Payment Integration**
   - Stripe integration
   - PayPal integration
   - Apple Pay/Google Pay

4. **Analytics**
   - Usage statistics
   - Revenue reports
   - Popular bikes/locations
   - User analytics

5. **Mobile App**
   - React Native app
   - Location-based services
   - Push notifications

## Dependencies

- **Spring Boot 3.5.11**
- **Spring Data JPA**
- **Spring Web** (REST)
- **MySQL Connector Java**
- **Java 17**

## Support & Documentation

- API Documentation: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- Setup Guide: [BACKEND_README.md](BACKEND_README.md)
- Sample Data: [sample-data.sql](sample-data.sql)
