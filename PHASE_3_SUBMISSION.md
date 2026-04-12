# IT342 Phase 3 – Web Main Feature Completed
## E-Bike Rental System - Web Application Development

---

## 1. GitHub Repository Link
**Repository:** https://github.com/Bleassy/IT342-Chavez-EbikeRental.git

**Branch:** `main`

---

## 2. Final Commit Information

### Commit Details
- **Message:** IT342 Phase 3 – Web Main Feature Completed
- **Full Commit Hash:** `ff8a610e43400111e738cebb2dc1cf00f2d99080`
- **Short Hash:** `ff8a610`
- **Commit Link:** https://github.com/Bleassy/IT342-Chavez-EbikeRental/commit/ff8a610e43400111e738cebb2dc1cf00f2d99080

### Statistics
- **Files Changed:** 49 files
- **Additions:** 6,465 insertions(+)
- **Deletions:** 47 deletions(-)

---

## 3. Project Implementation Summary

### 3.1 Main Feature Description
**Feature: E-Bike Rental Booking System**

The main feature of our E-Bike Rental System allows users to:
- Browse available bikes with detailed information
- Filter and search bikes by location and availability
- Book bikes for specific date ranges
- View booking history and rental status
- Manage bike inventory (Admin panel)
- Track rental transactions

### 3.2 Key Functionalities Implemented

#### User Features
1. **Bike Listing Page** - Display all available bikes with details
2. **Bike Details Page** - Show comprehensive bike information
3. **Booking Page** - Allow users to select dates and submit bookings
4. **Booking Confirmation** - Display booking success with confirmation details
5. **Rental History** - Show users their past and current rentals
6. **Admin Panel** - Manage bikes, bookings, and user data

#### Authentication
- User Login/Registration functionality
- Google OAuth 2.0 Integration
- JWT token-based authentication
- Secure password management

---

## 4. Technology Stack

### Frontend (Web)
- **Framework:** React with TypeScript
- **Build Tool:** Vite
- **Styling:** Tailwind CSS
- **UI Components:** Shadcn/ui
- **HTTP Client:** Axios
- **State Management:** React Context API

### Backend
- **Framework:** Spring Boot 3.x
- **Database:** MySQL
- **ORM:** JPA/Hibernate
- **Authentication:** Spring Security + JWT
- **API:** RESTful Web Services

### Mobile (Android)
- **Language:** Kotlin
- **Architecture:** MVVM with Jetpack Compose
- **HTTP Client:** Retrofit
- **Authentication:** Google Sign-In SDK

---

## 5. API Endpoints Used

### Authentication Endpoints
```
POST /api/auth/register     - User registration
POST /api/auth/login        - User login
POST /api/auth/google-auth  - Google OAuth authentication
POST /api/auth/refresh      - Refresh JWT token
```

### Bike Endpoints
```
GET  /api/bikes              - Get all bikes
GET  /api/bikes/{id}         - Get bike details
POST /api/bikes              - Create bike (Admin)
PUT  /api/bikes/{id}         - Update bike (Admin)
DELETE /api/bikes/{id}       - Delete bike (Admin)
```

### Booking Endpoints
```
POST /api/bookings           - Create booking
GET  /api/bookings           - Get user's bookings
GET  /api/bookings/{id}      - Get booking details
PUT  /api/bookings/{id}      - Update booking
DELETE /api/bookings/{id}    - Cancel booking
GET  /api/bookings/admin/all - Get all bookings (Admin)
```

### User Endpoints
```
GET  /api/users/profile      - Get user profile
PUT  /api/users/profile      - Update user profile
GET  /api/users              - Get all users (Admin)
```

---

## 6. Database Schema

### Tables Involved

#### Users Table
```sql
- user_id (Primary Key)
- username (Unique)
- email (Unique)
- password (Hashed)
- first_name
- last_name
- phone_number
- role (USER, ADMIN)
- created_at
- updated_at
```

#### Bikes Table
```sql
- bike_id (Primary Key)
- model_name
- description
- location
- price_per_hour
- availability_status
- bike_type
- specifications
- image_url
- created_at
- updated_at
```

#### Bookings Table
```sql
- booking_id (Primary Key)
- user_id (Foreign Key -> Users)
- bike_id (Foreign Key -> Bikes)
- start_date
- end_date
- total_price
- booking_status
- payment_status
- created_at
- updated_at
```

#### Returns Table
```sql
- return_id (Primary Key)
- booking_id (Foreign Key -> Bookings)
- actual_return_date
- condition_report
- late_fee
- created_at
```

---

## 7. Inputs and Validations

### Registration/Login Validation
- **Email:** Must be valid email format
- **Password:** Minimum 8 characters, must include uppercase, lowercase, number, and special character
- **Username:** Minimum 3 characters, alphanumeric only

### Bike Booking Validation
- **Start Date:** Cannot be in the past
- **End Date:** Must be after start date
- **Bike Selection:** Bike must be available for selected dates
- **User Authentication:** Must be logged in to book

### Admin Controls
- **Bike Details:** All fields must be filled
- **Price:** Must be positive number
- **Location:** Valid location from system options

---

## 8. Feature Workflow

### User Booking Flow
1. User registers/logs in to system
2. User browses available bikes on Bike List page
3. User clicks on bike to view details
4. User clicks "Book Now" button
5. System validates date availability
6. User selects rental dates on Booking page
7. System calculates total price
8. User confirms booking
9. System processes payment (simulated)
10. User receives booking confirmation
11. Booking is saved to database

### Admin Management Flow
1. Admin logs in with admin role
2. Admin accesses Admin Panel
3. Admin can:
   - View all bikes inventory
   - Add new bikes
   - Update bike information
   - Delete bikes
   - View all bookings
   - Manage user accounts
4. Changes are reflected in database

---

## 9. Components & Pages Implemented

### Web Components
- **BikeCard.tsx** - Display bike information in grid format
- **Navbar.tsx** - Navigation header with user menu
- **NavLink.tsx** - Navigation link component
- **ProtectedRoute.tsx** - Route protection for authenticated users
- **UI Components** - Shadcn/ui components (Alert, Button, Card, Dialog, Form, etc.)

### Web Pages
- **Index.tsx** - Home page with featured bikes
- **Login.tsx** - User login page with email/password and OAuth
- **Register.tsx** - User registration page
- **BikeList.tsx** - List of all available bikes
- **BikeDetails.tsx** - Detailed bike information
- **BookingPage.tsx** - Booking form and confirmation
- **BookingConfirmation.tsx** - Booking success page
- **RentalHistory.tsx** - User's rental history
- **Dashboard.tsx** - User dashboard
- **AdminPanel.tsx** - Admin management interface
- **GoogleCallback.tsx** - OAuth callback handler

---

## 10. Authentication & Security

### JWT Implementation
- Token generation on login/registration
- Token stored in secure HTTP-only cookies
- Token refresh mechanism for expired sessions
- Authorization header middleware

### OAuth 2.0 (Google)
- Google Sign-In integration on web and mobile
- Automatic token mapping to user accounts
- OAuth callback handling

### Password Security
- Bcrypt hashing for passwords
- Salt generation for each password
- No plain-text storage

---

## 11. Files & Directory Structure Committed

```
web/
├── src/
│   ├── components/
│   │   ├── BikeCard.tsx
│   │   ├── Navbar.tsx
│   │   ├── NavLink.tsx
│   │   ├── ProtectedRoute.tsx
│   │   └── ui/
│   ├── pages/
│   │   ├── AdminPanel.tsx
│   │   ├── BikeDetails.tsx
│   │   ├── BikeList.tsx
│   │   ├── BookingConfirmation.tsx
│   │   ├── BookingPage.tsx
│   │   ├── Dashboard.tsx
│   │   ├── GoogleCallback.tsx
│   │   ├── Index.tsx
│   │   ├── Login.tsx
│   │   ├── NotFound.tsx
│   │   ├── Register.tsx
│   │   └── RentalHistory.tsx
│   ├── contexts/
│   │   └── AuthContext.tsx
│   ├── hooks/
│   ├── lib/
│   └── App.tsx
├── pom.xml
├── vite.config.ts
└── tailwind.config.ts

backend/ebike/
├── src/
│   ├── main/
│   │   ├── java/com/ebike/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   ├── config/
│   │   │   └── EBikeApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml

ebikemobile/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ebike/mobile/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── api/
│   │   │   │   ├── auth/
│   │   │   │   ├── data/
│   │   │   │   └── ui/
│   │   │   └── res/
│   │   └── test/
│   └── build.gradle
└── build.gradle
```

---

## 12. Development Progress & Commits

### Commit Timeline
- **Initial Setup:** Project setup with Spring Boot, React, and Mobile framework
- **Backend Development:** API endpoints for bikes, bookings, and users
- **Database Setup:** MySQL schema with all required tables
- **Frontend Development:** Web components and pages
- **Authentication:** JWT and OAuth 2.0 implementation
- **Mobile App:** Android Kotlin implementation
- **Integration Testing:** Connect frontend to backend
- **Final Commit:** Phase 3 completion with all features integrated

### Latest Commit
- **Hash:** `ff8a610`
- **Date:** April 6, 2026
- **Message:** IT342 Phase 3 – Web Main Feature Completed
- **Files:** 49 changed, 6,465 additions

---

## 13. Testing & Validation

### Manual Testing Performed
- ✅ User registration with validation
- ✅ User login with JWT authentication
- ✅ Google OAuth login flow
- ✅ Bike listing and filtering
- ✅ Bike booking with date validation
- ✅ Booking confirmation and history
- ✅ Admin panel functionality
- ✅ Database persistence
- ✅ Error handling and user feedback

### API Testing
- ✅ All REST endpoints functional
- ✅ JWT token validation
- ✅ Request/response formatting
- ✅ Error codes and messages

---

## 14. How to Run the Project

### Backend
```bash
cd backend/ebike
mvn clean install
mvn spring-boot:run
```

### Frontend (Web)
```bash
cd web
npm install
npm run dev
```

### Mobile (Android)
```bash
cd ebikemobile
./gradlew build
# Open in Android Studio and run
```

### Database
- MySQL server running on localhost:3306
- Database: `ebike_rental`
- Credentials configured in `application.properties`

---

## 15. Deployment & Production Ready

### Environment Configuration
- Production database credentials
- Environment variables setup
- JWT secret key configuration
- OAuth credentials stored securely

### Build & Deployment
- Frontend: Vite production build
- Backend: Spring Boot JAR package
- Mobile: APK/AAB build

---

## 16. Additional Notes

### Features Beyond Requirements
- Google OAuth 2.0 integration
- Mobile Android app development
- Admin dashboard with full management
- Real-time booking confirmations
- Comprehensive error handling
- Database audit trails

### Known Limitations
- Payment processing is simulated
- Mobile app Android-only (no iOS)
- No real-time notifications yet

### Future Enhancements
- Payment gateway integration
- Real-time chat support
- GPS tracking for bikes
- Advanced analytics dashboard
- iOS mobile app

---

## 17. Team Information

**Project:** E-Bike Rental System  
**Repository:** https://github.com/Bleassy/IT342-Chavez-EbikeRental.git  
**Phase:** 3 - Web Main Feature Completed  
**Date:** April 6, 2026

---

*This document fulfills all requirements for IT342 Phase 3 submission.*
