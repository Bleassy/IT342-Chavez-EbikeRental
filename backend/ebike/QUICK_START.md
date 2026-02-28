# E-Bike Rental Backend - Quick Start Guide

## ⚡ 5-Minute Setup

### Step 1: Create Database
```bash
# Open MySQL command line or Workbench
mysql -u root -p

# Create database
CREATE DATABASE IF NOT EXISTS ebike_rental_db;
USE ebike_rental_db;

# Exit
EXIT;
```

### Step 2: Navigate to Backend
```bash
cd backend/ebike
```

### Step 3: Build Project
```bash
mvn clean install
```

### Step 4: Run Application
```bash
mvn spring-boot:run
```

**✅ Backend is now running at:** `http://localhost:8080/api`

---

## 🧪 Test the API

### Test 1: Health Check
```bash
curl http://localhost:8080/api
```

Expected Response:
```json
{
  "success": true,
  "message": "E-Bike Rental API is running successfully",
  "data": "API Version 1.0"
}
```

### Test 2: Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@test.com",
    "password": "pass123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890"
  }'
```

### Test 3: Login User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@test.com",
    "password": "pass123"
  }'
```

### Test 4: Get All Bikes
```bash
curl http://localhost:8080/api/bikes
```

### Test 5: Get Available Bikes
```bash
curl http://localhost:8080/api/bikes/available
```

---

## 📊 Load Sample Data (Optional)

### Option 1: Using SQL Script
```bash
# In MySQL command line
USE ebike_rental_db;
SOURCE backend/ebike/src/main/resources/sample-data.sql;
```

### Option 2: Create Test Data via API

**Create a Bike:**
```bash
curl -X POST http://localhost:8080/api/bikes \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Bike created successfully",
  "data": {
    "id": 1,
    "bikeCode": "BIKE001",
    ...
  }
}
```

---

## 🔑 Get User ID for Booking

**Register and get your user ID:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "mybooking@test.com",
    "password": "pass123",
    "firstName": "Jane",
    "lastName": "Smith",
    "phone": "9876543210"
  }'
```

Response contains the user ID in the `data` object.

---

## 📅 Create a Booking

**Format:** `YYYY-MM-DDTHH:mm:ss`

Example times:
- Start: `2024-01-20T10:00:00`
- End: `2024-01-20T14:00:00` (4 hours)

**Create Booking:**
```bash
curl -X POST "http://localhost:8080/api/bookings?userId=1&bikeId=1&startTime=2024-01-20T10:00:00&endTime=2024-01-20T14:00:00"
```

---

## 💳 Process Payment

**After creating a booking, get the booking ID from response:**

```bash
curl -X POST "http://localhost:8080/api/payments?bookingId=1&paymentMethod=CREDIT_CARD"
```

**Complete Payment:**
```bash
curl -X PUT http://localhost:8080/api/payments/1/complete
```

---

## 📚 Useful Endpoints

### Users & Auth
- `POST /auth/register` - Register
- `POST /auth/login` - Login
- `GET /admin/users` - Get all users (Admin)
- `GET /admin/users/{id}` - Get specific user (Admin)

### Bikes
- `GET /bikes` - All bikes
- `GET /bikes/available` - Available only
- `GET /bikes/{id}` - Specific bike
- `POST /bikes` - Create bike (Admin)
- `PUT /bikes/{id}` - Update bike
- `DELETE /bikes/{id}` - Delete bike (Admin)

### Bookings
- `POST /bookings` - Create booking
- `GET /bookings/user/{userId}` - User's bookings
- `GET /bookings/user/{userId}/history` - Completed bookings
- `PUT /bookings/{id}/confirm` - Confirm
- `PUT /bookings/{id}/complete` - Complete
- `PUT /bookings/{id}/cancel` - Cancel

### Payments
- `POST /payments` - Process payment
- `GET /payments/{id}` - Get payment
- `PUT /payments/{id}/complete` - Complete payment
- `PUT /payments/{id}/refund` - Refund payment

---

## 🔍 Database Inspection

### Using MySQL Workbench:
1. Connect to MySQL (User: root, Password: chavez01)
2. Select database: `ebike_rental_db`
3. View tables: users, bikes, bookings, payments

### View Table Data:
```sql
SELECT * FROM users;
SELECT * FROM bikes;
SELECT * FROM bookings;
SELECT * FROM payments;
```

---

## 🆘 Common Issues & Solutions

### ❌ Error: "Connection refused" 
**Solution:** Ensure MySQL is running
```bash
# Windows CMD
net start MySQL80

# Or check Services app and start MySQL service
```

### ❌ Error: "Access denied for user 'root'"
**Solution:** Check MySQL password in `application.properties` is `chavez01`

### ❌ Port 8080 already in use
**Solution:** Change port in `application.properties`:
```properties
server.port=8081
```

### ❌ Maven not found
**Solution:** Use Maven wrapper included in project:
```bash
# Windows
mvnw clean install

# Linux/Mac
./mvnw clean install
```

### ❌ Java version error
**Solution:** Ensure Java 17+ is installed:
```bash
java -version
```

---

## 📝 Configuration Changes

### Change MySQL Password:
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.password=YOUR_PASSWORD
```

### Change Server Port:
```properties
server.port=9000
```

### Change Database Name:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/YOUR_DB_NAME
```

---

## 🚀 Next Steps

1. **Setup Frontend Connection:**
   - Update frontend API calls to `http://localhost:8080/api`

2. **Add Authentication:**
   - Implement JWT tokens in production
   - Add password encryption (BCrypt)

3. **Integrate Payment Gateway:**
   - Stripe
   - PayPal

4. **Add Email Notifications:**
   - Booking confirmations
   - Payment receipts

5. **Deploy to Cloud:**
   - AWS (RDS + EC2)
   - Azure (MySQL + App Service)
   - Heroku

---

## 📖 Documentation Files

- **API_DOCUMENTATION.md** - Complete API reference with all endpoints
- **BACKEND_README.md** - Detailed setup and features
- **PROJECT_STRUCTURE.md** - Code organization and architecture

---

## 💡 Tips

✅ Use Postman or Insomnia for testing APIs before connecting frontend
✅ Logs show SQL queries with `spring.jpa.show-sql=true`
✅ Sample data loads required initial test bikes and users
✅ Price calculation automatic: (days × daily rate) + (hours × hourly rate)
✅ Bike status updates automatically with booking lifecycle

---

## 📞 Support

For detailed documentation, refer to:
- `API_DOCUMENTATION.md` - All endpoints & parameters
- `BACKEND_README.md` - Setup & configuration
- `PROJECT_STRUCTURE.md` - Architecture & code organization

---

**Ready to run?** Follow the 5-minute setup above! 🎉
