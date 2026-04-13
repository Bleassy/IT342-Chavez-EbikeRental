# E-Bike Rental Backend API

A complete Spring Boot REST API for managing e-bike rental operations.

## Features

- **User Management**: Registration, login, and user profile management
- **Bike Management**: CRUD operations for bikes, status management, and availability tracking
- **Booking System**: Create, confirm, complete, and cancel bike bookings with automatic price calculation
- **Payment Processing**: Process payments with multiple payment methods and transaction tracking
- **Admin Dashboard**: Administrative functions for managing users and system resources

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher
- MySQL Workbench (optional, for database management)

## Setup Instructions

### 1. Create Database

Open MySQL Workbench or command line and create the database:

```sql
CREATE DATABASE IF NOT EXISTS ebike_rental_db;
USE ebike_rental_db;
```

### 2. Clone and Configure

1. Navigate to the backend directory:
```bash
cd backend/ebike
```

2. The `application.properties` is already configured with:
   - Database URL: `jdbc:mysql://localhost:3306/ebike_rental_db`
   - Username: `root`
   - Password: `chavez01`
   - JPA Hibernate will auto-create tables on startup with `ddl-auto=update`

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or:

```bash
java -jar target/ebike-0.0.1-SNAPSHOT.jar
```

The API will start on `http://localhost:8080/api`

## API Endpoints

### Health Check
- `GET /` - API health check
- `GET /health` - Server health status

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login user

### Bikes
- `GET /bikes` - Get all bikes
- `GET /bikes/available` - Get available bikes
- `GET /bikes/{id}` - Get bike by ID
- `GET /bikes/code/{bikeCode}` - Get bike by code
- `GET /bikes/location/{location}` - Find bikes by location
- `POST /bikes` - Create new bike (Admin)
- `PUT /bikes/{id}` - Update bike
- `DELETE /bikes/{id}` - Delete bike (Admin)
- `PUT /bikes/{id}/status` - Update bike status

### Bookings
- `GET /bookings` - Get all bookings
- `GET /bookings/{id}` - Get booking by ID
- `GET /bookings/user/{userId}` - Get user's bookings
- `GET /bookings/user/{userId}/history` - Get booking history
- `GET /bookings/bike/{bikeId}` - Get bike's bookings
- `GET /bookings/active` - Get active bookings
- `POST /bookings` - Create new booking
- `PUT /bookings/{id}/confirm` - Confirm booking
- `PUT /bookings/{id}/complete` - Complete booking
- `PUT /bookings/{id}/cancel` - Cancel booking
- `DELETE /bookings/{id}` - Delete booking

### Payments
- `GET /payments` - Get all payments
- `GET /payments/{id}` - Get payment by ID
- `GET /payments/transaction/{transactionId}` - Get payment by transaction ID
- `GET /payments/booking/{bookingId}` - Get payment by booking ID
- `POST /payments` - Process payment
- `PUT /payments/{id}/complete` - Complete payment
- `PUT /payments/{id}/fail` - Mark payment as failed
- `PUT /payments/{id}/refund` - Refund payment
- `DELETE /payments/{id}` - Delete payment

### Admin
- `GET /admin/users` - Get all users
- `GET /admin/users/active` - Get active users
- `GET /admin/users/{id}` - Get user by ID
- `PUT /admin/users/{id}` - Update user
- `DELETE /admin/users/{id}` - Delete user

## Database Schema

The application automatically creates the following tables:

### Users Table
- Stores user information and roles (ADMIN, USER)
- Tracks account creation and updates

### Bikes Table
- Maintains bike inventory
- Tracks status (AVAILABLE, RENTED, MAINTENANCE, UNAVAILABLE)
- Stores bike specifications and pricing

### Bookings Table
- Records bike rental transactions
- Tracks booking status (PENDING, CONFIRMED, CANCELLED, COMPLETED)
- Calculates total price based on rental duration

### Payments Table
- Manages payment transactions
- Supports multiple payment methods
- Tracks payment status

## Request/Response Examples

### Register User
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "1234567890"
}
```

### Login User
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### Create Bike
```bash
POST http://localhost:8080/api/bikes
Content-Type: application/json

{
  "bikeCode": "BIKE001",
  "model": "Mountain Pro",
  "brand": "Trek",
  "color": "Red",
  "year": 2024,
  "type": "MOUNTAIN",
  "pricePerHour": 5.00,
  "pricePerDay": 30.00,
  "status": "AVAILABLE",
  "condition": "EXCELLENT",
  "location": "Downtown",
  "batteryLevel": 100
}
```

### Create Booking
```bash
POST http://localhost:8080/api/bookings?userId=1&bikeId=1&startTime=2024-01-15T10:00:00&endTime=2024-01-15T14:00:00
Content-Type: application/json
```

## Configuration Details

### MySQL Configuration
- Host: localhost
- Port: 3306
- Database: ebike_rental_db
- Username: root
- Password: chavez01

### Spring Boot Properties
- Server Port: 8080
- Context Path: /api
- JPA Auto DDL: update
- SQL Logging: enabled for debugging

## Notes

- Password encoding is commented in UserService - implement BCryptPasswordEncoder in production
- CORS is enabled for all origins - restrict in production
- JWT authentication not yet implemented - recommended for production
- All endpoints return consistent API response format with success status and data

## Troubleshooting

### Database Connection Error
- Ensure MySQL is running
- Verify credentials in application.properties
- Check if database exists

### Port Already in Use
- Change server.port in application.properties
- Or kill the process using port 8080

### Build Failures
- Run `mvn clean` before rebuilding
- Ensure Maven dependencies are updated

## Future Enhancements

- JWT authentication and authorization
- Rate limiting and throttling
- Advanced payment integration (Stripe, PayPal)
- Email notifications
- Real-time bike location tracking
- User reviews and ratings system

## Deploy on Render with Supabase

Yes, you can deploy this backend to Render and use Supabase as your online database.

### 1. Supabase database values

From Supabase project settings, copy your PostgreSQL connection details:

- Host
- Port
- Database name
- Username
- Password

Build the JDBC URL in this format:

```text
jdbc:postgresql://<HOST>:<PORT>/<DATABASE>?sslmode=require
```

### 2. Render environment variables

Set these in your Render service:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<HOST>:<PORT>/<DATABASE>?sslmode=require
SPRING_DATASOURCE_USERNAME=<USERNAME>
SPRING_DATASOURCE_PASSWORD=<PASSWORD>
JWT_SECRET=<LONG_RANDOM_SECRET>
JWT_EXPIRATION=86400000
GOOGLE_CLIENT_ID=<YOUR_GOOGLE_CLIENT_ID>
GOOGLE_CLIENT_SECRET=<YOUR_GOOGLE_CLIENT_SECRET>
```

Notes:

- Render automatically provides `PORT`, and the app now uses it.
- Use a strong `JWT_SECRET` in production.
- For Google OAuth, add your Render frontend callback URL in Google Cloud Console.

### 3. Build and start commands on Render

```text
Build Command: ./mvnw clean package -DskipTests
Start Command: java -jar target/ebike-0.0.1-SNAPSHOT.jar
```

### 4. Frontend API URL

If you deploy the web app too, point it to your Render backend:

```text
VITE_API_URL=https://<your-render-backend-domain>
```
