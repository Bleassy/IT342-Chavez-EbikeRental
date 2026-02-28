# E-Bike Rental API - Complete Endpoint Documentation

**Base URL:** `http://localhost:8080/api`

## Table of Contents
1. [Health Check](#health-check)
2. [Authentication](#authentication)
3. [Bikes](#bikes)
4. [Bookings](#bookings)
5. [Payments](#payments)
6. [Admin](#admin)

---

## Health Check

### Get API Status
```
GET /
```
**Response:**
```json
{
  "success": true,
  "message": "E-Bike Rental API is running successfully",
  "data": "API Version 1.0"
}
```

### Check Server Health
```
GET /health
```
**Response:**
```json
{
  "success": true,
  "message": "Health check passed",
  "data": "Server is healthy"
}
```

---

## Authentication

### Register New User
```
POST /auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "1234567890"
}
```

**Success Response (201):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890",
    "address": null,
    "role": "USER",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### Login User
```
POST /auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890",
    "address": null,
    "role": "USER",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

## Bikes

### Get All Bikes
```
GET /bikes
```

**Response:**
```json
{
  "success": true,
  "message": "Bikes retrieved successfully",
  "data": [
    {
      "id": 1,
      "bikeCode": "BIKE001",
      "model": "Mountain Pro X",
      "brand": "Trek",
      "color": "Red",
      "year": 2024,
      "type": "MOUNTAIN",
      "pricePerHour": 5.00,
      "pricePerDay": 30.00,
      "status": "AVAILABLE",
      "description": "High-performance mountain bike",
      "imageUrl": "https://...",
      "condition": "EXCELLENT",
      "batteryLevel": 100,
      "location": "Downtown Station",
      "createdAt": "2024-01-15T08:00:00"
    }
  ]
}
```

### Get Available Bikes
```
GET /bikes/available
```

### Get Bike by ID
```
GET /bikes/{id}
```

**Path Parameters:**
- `id` (Long) - Bike ID

### Get Bike by Code
```
GET /bikes/code/{bikeCode}
```

**Path Parameters:**
- `bikeCode` (String) - Unique bike code

### Find Bikes by Location
```
GET /bikes/location/{location}
```

**Path Parameters:**
- `location` (String) - Bike location

### Create New Bike (Admin Only)
```
POST /bikes
Content-Type: application/json
```

**Request Body:**
```json
{
  "bikeCode": "BIKE011",
  "model": "City Commute Plus",
  "brand": "Giant",
  "color": "Blue",
  "year": 2024,
  "type": "HYBRID",
  "pricePerHour": 3.50,
  "pricePerDay": 20.00,
  "status": "AVAILABLE",
  "description": "Perfect for city commuting",
  "imageUrl": "https://...",
  "condition": "EXCELLENT",
  "batteryLevel": 100,
  "location": "West Park"
}
```

### Update Bike
```
PUT /bikes/{id}
Content-Type: application/json
```

**Path Parameters:**
- `id` (Long) - Bike ID

**Request Body:** (Include fields to update)
```json
{
  "condition": "GOOD",
  "batteryLevel": 85,
  "location": "South Gateway"
}
```

### Delete Bike (Admin Only)
```
DELETE /bikes/{id}
```

**Path Parameters:**
- `id` (Long) - Bike ID

### Update Bike Status
```
PUT /bikes/{id}/status
```

**Path Parameters:**
- `id` (Long) - Bike ID

**Query Parameters:**
- `status` (BikeStatus) - AVAILABLE, RENTED, MAINTENANCE, UNAVAILABLE

---

## Bookings

### Get All Bookings
```
GET /bookings
```

### Get Booking by ID
```
GET /bookings/{id}
```

**Path Parameters:**
- `id` (Long) - Booking ID

### Get User's Bookings
```
GET /bookings/user/{userId}
```

**Path Parameters:**
- `userId` (Long) - User ID

### Get User's Booking History
```
GET /bookings/user/{userId}/history
```

**Path Parameters:**
- `userId` (Long) - User ID

### Get Bike's Bookings
```
GET /bookings/bike/{bikeId}
```

**Path Parameters:**
- `bikeId` (Long) - Bike ID

### Get Active Bookings
```
GET /bookings/active
```

### Create New Booking
```
POST /bookings
```

**Query Parameters:**
- `userId` (Long) - User ID (required)
- `bikeId` (Long) - Bike ID (required)
- `startTime` (LocalDateTime) - Booking start time (required) - Format: `YYYY-MM-DDTHH:mm:ss`
- `endTime` (LocalDateTime) - Booking end time (required) - Format: `YYYY-MM-DDTHH:mm:ss`

**Example:**
```
POST /bookings?userId=1&bikeId=1&startTime=2024-01-15T10:00:00&endTime=2024-01-15T14:00:00
```

**Response:**
```json
{
  "success": true,
  "message": "Booking created successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "bikeId": 1,
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-15T14:00:00",
    "status": "PENDING",
    "totalPrice": 20.00,
    "notes": null,
    "createdAt": "2024-01-15T09:30:00"
  }
}
```

### Confirm Booking
```
PUT /bookings/{id}/confirm
```

**Path Parameters:**
- `id` (Long) - Booking ID

### Complete Booking
```
PUT /bookings/{id}/complete
```

**Path Parameters:**
- `id` (Long) - Booking ID

### Cancel Booking
```
PUT /bookings/{id}/cancel
```

**Path Parameters:**
- `id` (Long) - Booking ID

### Delete Booking
```
DELETE /bookings/{id}
```

**Path Parameters:**
- `id` (Long) - Booking ID

---

## Payments

### Get All Payments
```
GET /payments
```

### Get Payment by ID
```
GET /payments/{id}
```

**Path Parameters:**
- `id` (Long) - Payment ID

### Get Payment by Transaction ID
```
GET /payments/transaction/{transactionId}
```

**Path Parameters:**
- `transactionId` (String) - Transaction ID

### Get Payment by Booking ID
```
GET /payments/booking/{bookingId}
```

**Path Parameters:**
- `bookingId` (Long) - Booking ID

### Process Payment
```
POST /payments
```

**Query Parameters:**
- `bookingId` (Long) - Booking ID (required)
- `paymentMethod` (PaymentMethod) - CREDIT_CARD, DEBIT_CARD, PAYPAL, WALLET (required)

**Example:**
```
POST /payments?bookingId=1&paymentMethod=CREDIT_CARD
```

**Response:**
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "id": 1,
    "booking": { ... },
    "amount": 20.00,
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "PENDING",
    "transactionId": "TXN20240115001",
    "notes": null,
    "createdAt": "2024-01-15T09:30:00"
  }
}
```

### Complete Payment
```
PUT /payments/{id}/complete
```

**Path Parameters:**
- `id` (Long) - Payment ID

### Mark Payment as Failed
```
PUT /payments/{id}/fail
```

**Path Parameters:**
- `id` (Long) - Payment ID

### Refund Payment
```
PUT /payments/{id}/refund
```

**Path Parameters:**
- `id` (Long) - Payment ID

### Delete Payment
```
DELETE /payments/{id}
```

**Path Parameters:**
- `id` (Long) - Payment ID

---

## Admin

### Get All Users
```
GET /admin/users
```

### Get Active Users
```
GET /admin/users/active
```

### Get User by ID
```
GET /admin/users/{id}
```

**Path Parameters:**
- `id` (Long) - User ID

### Update User
```
PUT /admin/users/{id}
Content-Type: application/json
```

**Path Parameters:**
- `id` (Long) - User ID

**Request Body:** (Include fields to update)
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "9876543210",
  "address": "123 New Street"
}
```

### Delete User
```
DELETE /admin/users/{id}
```

**Path Parameters:**
- `id` (Long) - User ID

---

## Enums Reference

### User Role
- `ADMIN` - Administrator user
- `USER` - Regular user

### Bike Type
- `STANDARD` - Standard bike
- `ELECTRIC` - Electric bike
- `MOUNTAIN` - Mountain bike
- `HYBRID` - Hybrid bike

### Bike Status
- `AVAILABLE` - Available for rental
- `RENTED` - Currently rented
- `MAINTENANCE` - Under maintenance
- `UNAVAILABLE` - Not available

### Bike Condition
- `EXCELLENT` - Excellent condition
- `GOOD` - Good condition
- `FAIR` - Fair condition
- `POOR` - Poor condition

### Booking Status
- `PENDING` - Awaiting confirmation
- `CONFIRMED` - Confirmed booking
- `CANCELLED` - Cancelled booking
- `COMPLETED` - Completed booking

### Payment Method
- `CREDIT_CARD` - Credit card payment
- `DEBIT_CARD` - Debit card payment
- `PAYPAL` - PayPal payment
- `WALLET` - Wallet payment

### Payment Status
- `PENDING` - Payment pending
- `COMPLETED` - Payment completed
- `FAILED` - Payment failed
- `REFUNDED` - Payment refunded

---

## Error Responses

All error responses follow this format:
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

**Common HTTP Status Codes:**
- `200 OK` - Successful request
- `201 CREATED` - Resource created successfully
- `400 BAD REQUEST` - Invalid request
- `401 UNAUTHORIZED` - Authentication failed
- `404 NOT FOUND` - Resource not found
- `409 CONFLICT` - Resource conflict (e.g., duplicate)
- `500 INTERNAL SERVER ERROR` - Server error

---

## Notes

1. **Price Calculation** - Automatically calculated based on:
   - Number of full days × Daily Rate
   - Remaining hours × Hourly Rate

2. **Bike Status Updates** - Automatically managed:
   - Booking creation: Bike status → RENTED
   - Booking completion: Bike status → AVAILABLE
   - Booking cancellation: Bike status → AVAILABLE

3. **CORS** - All endpoints support cross-origin requests from any domain

4. **Transaction ID** - Auto-generated UUID for each payment transaction
