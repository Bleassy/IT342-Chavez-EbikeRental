# E-Bike Mobile App (Android Kotlin)

A modern Android mobile application for the E-Bike rental system, built with Kotlin and Jetpack Compose.

## Features

- **Authentication**
  - Email/Password login and registration
  - Google OAuth integration
  - JWT token-based authentication
  - Secure token storage with DataStore

- **Bike Management**
  - Browse available bikes with search functionality
  - View detailed bike information
  - Battery level and pricing information
  - Real-time availability status
  - Nearby bikes detection with geolocation

- **Booking System**
  - Easy bike booking with date/time selection
  - Active rental tracking
  - Booking history with detailed records
  - Cancel rentals with reason capture
  - Total cost calculation

- **User Profile**
  - Profile photo upload
  - Account information management
  - Rental history and statistics
  - Booking management

- **User Experience**
  - Material Design 3 UI
  - Smooth navigation with Jetpack Navigation
  - Loading states and error handling
  - Responsive design for all screen sizes

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM with Repository pattern
- **Networking**: Retrofit 2 + OkHttp
- **Local Storage**: DataStore + Room Database
- **Authentication**: JWT + Google Sign-In
- **Dependency Injection**: Hilt (optional, not currently implemented)
- **Logging**: Timber
- **Image Loading**: Coil

## Project Structure

```
app/src/main/java/com/ebike/mobile/
├── api/
│   ├── BikeRentalApi.kt          (Retrofit interface)
│   ├── AuthInterceptor.kt        (JWT token interceptor)
│   └── RetrofitClient.kt         (Retrofit setup)
├── data/
│   ├── local/
│   │   └── TokenManager.kt       (Secure token storage)
│   ├── models/
│   │   └── Models.kt             (Data classes)
│   └── repository/
│       ├── AuthRepository.kt     (Auth logic)
│       ├── BikeRepository.kt     (Bike operations)
│       └── BookingRepository.kt  (Booking operations)
├── ui/
│   ├── screens/
│   │   ├── Navigation.kt         (Navigation setup)
│   │   ├── LoginScreen.kt        (Login UI)
│   │   ├── RegisterScreen.kt     (Registration UI)
│   │   ├── DashboardScreen.kt    (Main dashboard)
│   │   ├── BikeListScreen.kt     (Bikes list)
│   │   └── DetailScreens.kt      (Detail & other screens)
│   ├── theme/
│   │   └── Theme.kt              (Material Design 3 theme)
│   └── viewmodels/
│       ├── AuthViewModel.kt      (Auth logic)
│       ├── BikeViewModel.kt      (Bike operations)
│       └── BookingViewModel.kt   (Booking operations)
└── MainActivity.kt               (Entry point)
```

## Setup Instructions

### Prerequisites

- Android Studio (Iguana 2023.2.1+)
- JDK 17+
- Android SDK 34+
- Min SDK 24+

### Configuration

1. **Update Backend URL**
   - Open `app/build.gradle`
   - Update `BASE_URL` in `buildConfigField`:
   ```gradle
   buildConfigField "String", "BASE_URL", "\"http://YOUR_BACKEND_URL/api/\""
   ```
   - For emulator: `http://10.0.2.2:8080/api/`
   - For device: `http://YOUR_IP:8080/api/`

2. **Google OAuth Setup**
   - Add your Google Client ID in `app/build.gradle`:
   ```gradle
   buildConfigField "String", "GOOGLE_CLIENT_ID", "\"YOUR_GOOGLE_CLIENT_ID_HERE\""
   ```

3. **Build and Run**
   ```bash
   # Build
   ./gradlew build
   
   # Run on emulator/device
   ./gradlew installDebug
   ```

## API Integration

The app connects to the Spring Boot backend at `/api` endpoints:

- **Auth**: POST `/auth/login`, `/auth/register`, `/auth/google`
- **Users**: GET/PUT `/users/profile`
- **Bikes**: GET `/bikes`, GET `/bikes/{id}`, GET `/bikes/search`
- **Bookings**: POST/GET `/bookings`, PUT `/bookings/{id}/cancel`

## Key Implementation Notes

### JWT Authentication
- Tokens are stored in encrypted DataStore
- Auto-attached to all requests via `AuthInterceptor`
- 24-hour expiration (backend-configured)

### Database Sync
- Uses Retrofit to sync with MySQL backend
- Local caching ready with Room (framework in place)
- Real-time updates on booking/rental changes

### Google OAuth Flow
1. User clicks "Continue with Google"
2. Google sign-in dialog appears
3. Token sent to backend
4. Backend authenticates and returns JWT
5. App stores JWT and navigates to dashboard

## Troubleshooting

### Cannot connect to backend
- Check `BASE_URL` in build.gradle
- Ensure backend is running on specified port
- For emulator, use `10.0.2.2` instead of `localhost`
- Check firewall settings

### Build errors
- Clean: `./gradlew clean`
- Invalidate cache: In Android Studio: File → Invalidate Caches
- Sync: `./gradlew --refresh-dependencies`

### Sign-in not working
- Verify Google Client ID in `build.gradle`
- Check internet connection
- Review backend logs for auth errors

## Next Steps

- Implement geolocation for nearby bikes
- Add payment integration
- Implement real-time GPS tracking
- Add ride analytics dashboard
- Implement push notifications
- Add offline support with local caching
- Unit and integration tests

## License

This project is part of the E-Bike Rental System. All rights reserved.

## Contact

For issues or questions, contact the development team.
