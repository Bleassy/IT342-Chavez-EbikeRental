# JWT Authentication Implementation - Complete Guide

## ✅ JWT Security Implemented

Your E-Bike Rental Backend now has complete JWT (JSON Web Token) authentication with password encryption.

---

## 🔐 What Was Added

### 1. **Password Encryption**
- BCrypt hashing for all passwords
- Passwords are encrypted when registering
- Password comparison during login uses BCrypt matching
- No plain text passwords stored

### 2. **JWT Token Generation**
- Tokens generated on successful login/registration
- Token contains: email, userId, role, expiration time
- Token expires after 24 hours by default
- Unique secret key for signing tokens

### 3. **JWT Token Validation**
- All protected endpoints validate JWT tokens
- Tokens verified on each request
- Invalid/expired tokens are rejected
- User info extracted from token

### 4. **Role-Based Access Control**
- ADMIN endpoints require admin role
- USER endpoints require authentication
- Public endpoints accessible without token

---

## 📋 New Files Created

1. **JwtTokenProvider.java** - Generates and validates JWT tokens
2. **JwtAuthenticationFilter.java** - Intercepts requests to validate tokens
3. **JwtUserDetails.java** - Holds user info extracted from token
4. **SecurityConfig.java** - Spring Security configuration
5. **AuthResponse.java** - Response DTO with JWT token

---

## 🚀 How to Use JWT Authentication

### Step 1: Register User

**Request:**
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "1234567890"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
    "role": "USER"
  }
}
```

**Save the token** from the response!

### Step 2: Login User

**Request:**
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
    "role": "USER"
  }
}
```

### Step 3: Use Token in Protected Requests

Add the token to the `Authorization` header:

**Header Format:**
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Example Request:**
```bash
POST http://localhost:8080/api/bookings?userId=1&bikeId=1&startTime=2024-01-20T10:00:00&endTime=2024-01-20T14:00:00
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
```

**Or with curl:**
```bash
curl -X POST "http://localhost:8080/api/bookings?userId=1&bikeId=1&startTime=2024-01-20T10:00:00&endTime=2024-01-20T14:00:00" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🔓 Public vs Protected Endpoints

### Public Endpoints (No Token Required)
- `GET /` - Health check
- `GET /health` - Server health
- `POST /auth/register` - Register user
- `POST /auth/login` - Login user
- `GET /bikes` - Get all bikes
- `GET /bikes/*` - Get specific bikes

### Protected Endpoints (Token Required)
- `POST /bookings` - Create booking
- `GET /bookings/*` - Get bookings
- `POST /payments` - Process payment
- `GET /payments/*` - Get payments
- `PUT /payments/*` - Update payment

### Admin Endpoints (Admin Token Required)
- `POST /bikes` - Create bike
- `PUT /bikes/*` - Update bike
- `DELETE /bikes/*` - Delete bike
- `GET /admin/users` - Get all users
- `PUT /admin/users/*` - Update user
- `DELETE /admin/users/*` - Delete user

---

## 🔑 Token Structure

JWT tokens have 3 parts: `header.payload.signature`

**Payload contains:**
```json
{
  "email": "user@example.com",
  "userId": 1,
  "role": "USER",
  "iat": 1705316400,
  "exp": 1705402800
}
```

**Token expires after:** 24 hours (86400000 ms)

---

## 🧪 Test JWT Authentication

### Using Postman:

1. **Register/Login**
   - Set method to POST
   - URL: `http://localhost:8080/api/auth/login`
   - Body (JSON):
     ```json
     {
       "email": "user@example.com",
       "password": "password123"
     }
     ```
   - Click Send
   - Copy the `token` from response

2. **Use Token for Protected Endpoint**
   - Set method to POST
   - URL: `http://localhost:8080/api/bookings?userId=1&bikeId=1&startTime=2024-01-20T10:00:00&endTime=2024-01-20T14:00:00`
   - Go to Headers tab
   - Add header: `Authorization` = `Bearer <PASTE_TOKEN_HERE>`
   - Click Send

3. **Expected Response**
   - If token is valid: Successful response with data
   - If token is invalid/missing: 401 Unauthorized error

---

## 🛡️ Security Features

### What's Protected:
✅ Passwords encrypted with BCrypt  
✅ JWT tokens signed with HMAC-SHA-512  
✅ Token expiration (24 hours)  
✅ Role-based access control  
✅ Filter validates token on every request  

### What Still Needs:
⚠️ HTTPS/SSL in production  
⚠️ Secure token storage on frontend  
⚠️ Token refresh mechanism  
⚠️ Rate limiting  
⚠️ CORS origin restrictions  

---

## 💻 Frontend Integration Example

### React/JavaScript Example:

```javascript
// Login and get token
async function login(email, password) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await response.json();
  
  if (data.success) {
    // Save token to localStorage
    localStorage.setItem('jwtToken', data.data.token);
    return data.data;
  }
}

// Use token in protected requests
async function createBooking(userId, bikeId, startTime, endTime) {
  const token = localStorage.getItem('jwtToken');
  
  const response = await fetch(
    `http://localhost:8080/api/bookings?userId=${userId}&bikeId=${bikeId}&startTime=${startTime}&endTime=${endTime}`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  return response.json();
}

// Clear token on logout
function logout() {
  localStorage.removeItem('jwtToken');
}
```

---

## ⚙️ Configuration

### JWT Settings (application.properties):
```properties
# Secret key for signing tokens
jwt.secret=ebike_rental_system_secure_key_2024_very_long_secret_key_for_production_environment

# Token expiration time (24 hours in milliseconds)
jwt.expiration=86400000
```

### Change Token Expiration:
- For 1 hour: `jwt.expiration=3600000`
- For 7 days: `jwt.expiration=604800000`

### Change Secret Key (Production):
- Generate a secure random string
- Update `jwt.secret` in application.properties
- **Important:** Keep secret key secure and never commit to version control

---

## 🔍 Troubleshooting

### Issue: "401 Unauthorized" without token
**Solution:** Add `Authorization: Bearer <TOKEN>` header to request

### Issue: "Invalid token" error
**Solution:** 
- Token may be expired (24 hour limit)
- Try logging in again to get new token
- Check token is not corrupted

### Issue: Password doesn't match after registration
**Solution:** 
- Passwords are now BCrypt encrypted
- Old passwords in database won't work
- Delete user and register again

### Issue: CORS error with token
**Solution:** Token should work with CORS enabled endpoints
- Check `Authorization` header is included
- Verify Bearer prefix is correct: `Bearer <TOKEN>`

---

## 📚 Security Best Practices

1. **Never log tokens** - Don't print tokens to console
2. **Use HTTPS** - Always use HTTPS in production
3. **Secure storage** - Store tokens in httpOnly cookies or secure storage
4. **Token rotation** - Implement token refresh for long sessions
5. **Environment variables** - Never hardcode secrets
6. **Rate limiting** - Prevent brute force attacks
7. **Validation** - Always validate user input

---

## 🔐 Password Security

### Before (Plain Text - NOT SECURE):
```java
if (user.getPassword().equals(request.getPassword())) { ... }
```

### After (BCrypt Encrypted - SECURE):
```java
if (passwordEncoder.matches(request.getPassword(), user.getPassword())) { ... }
```

---

## 📊 User Session Flow

```
1. User Registration
   └─ Password encrypted with BCrypt
   └─ User saved to database
   └─ JWT token generated
   └─ Token sent to frontend

2. User Login
   └─ Email and password received
   └─ Password checked with BCrypt
   └─ JWT token generated
   └─ Token sent to frontend

3. Protected API Request
   └─ Token received in Authorization header
   └─ Token validated by JwtAuthenticationFilter
   └─ Token checked for expiration
   └─ User info extracted from token
   └─ Request processed if valid
   └─ 401 error if invalid

4. Token Expiration
   └─ After 24 hours, token expires
   └─ User must login again to get new token
   └─ Old token becomes invalid
```

---

## ✅ Implementation Complete

Your backend now has:
✅ **Password Encryption** - BCrypt hashing  
✅ **JWT Tokens** - Secure authentication  
✅ **Token Validation** - On every protected request  
✅ **Role-Based Access** - ADMIN vs USER  
✅ **Expiration** - 24-hour token lifetime  
✅ **Security Filter** - Automatic token checking  

**Ready for production use!** Just update:
1. Secret key to a strong random string
2. JWT expiration based on your needs
3. CORS settings for your frontend URL
4. Add HTTPS/SSL certificates

---

## 🚀 Next Steps

1. Update frontend to use JWT tokens
2. Store tokens securely on frontend
3. Add logout functionality
4. Implement token refresh mechanism
5. Add rate limiting
6. Enable HTTPS/SSL
7. Monitor and log authentication events
