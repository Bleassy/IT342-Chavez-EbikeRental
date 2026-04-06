# E-Bike Mobile App - Quick Start Guide

## 5-Minute Setup

### 1. Prerequisites Check
- ✅ Android Studio Iguana (2023.2.1+) installed
- ✅ JDK 17 or higher
- ✅ Backend running: `java -jar backend/ebike/target/ebike-0.0.1-SNAPSHOT.jar`

### 2. Open Project
```bash
cd ebikemobile
```
In Android Studio: **File → Open → Select ebikemobile folder**

### 3. Configure Backend URL
Edit `app/build.gradle` and update `BASE_URL`:

```gradle
// For Emulator (default):
buildConfigField "String", "BASE_URL", "\"http://10.0.2.2:8080/api/\""

// For Device (replace with your IP):
buildConfigField "String", "BASE_URL", "\"http://192.168.1.X:8080/api/\""
```

### 4. Sync & Build
- Wait for Gradle sync to complete
- Click **Run 'app'** or press **Shift+F10**
- Select emulator or connected device
- Click **OK**

### 5. Test Login
```
Email: user@example.com
Password: user123
```
Or register a new account.

## App Features Implemented

### ✅ Authentication
- Email/Password login
- User registration
- JWT token management
- Secure token storage with DataStore
- Google OAuth ready (framework in place)

### ✅ Bikes
- Browse all available bikes
- Search bikes by name/model
- View detailed bike information
- Battery level, pricing, availability
- Nearby bikes feature (framework ready)

### ✅ Bookings
- Create bike bookings with date/time
- View booking history
- Cancel bookings with reason
- Track active rentals
- Complete bookings

### ✅ User Profile
- Profile information display
- Account management
- Rental history access

### ✅ UI/UX
- Material Design 3
- Jetpack Compose
- Responsive layouts
- Loading & error states
- Smooth navigation

## Project Files Created

```
ebikemobile/
├── app/
│   ├── build.gradle                     # App dependencies
│   ├── proguard-rules.pro              # Code obfuscation rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/ebike/mobile/
│           ├── MainActivity.kt
│           ├── api/
│           │   ├── BikeRentalApi.kt        # REST endpoints
│           │   ├── AuthInterceptor.kt      # JWT handling
│           │   └── RetrofitClient.kt       # HTTP client
│           ├── data/
│           │   ├── local/
│           │   │   └── TokenManager.kt     # Token storage
│           │   ├── models/
│           │   │   └── Models.kt           # Data classes
│           │   └── repository/
│           │       ├── AuthRepository.kt   # Auth logic
│           │       ├── BikeRepository.kt   # Bikes logic
│           │       └── BookingRepository.kt # Bookings logic
│           ├── ui/
│           │   ├── screens/
│           │   │   ├── Navigation.kt       # Route setup
│           │   │   ├── LoginScreen.kt
│           │   │   ├── RegisterScreen.kt
│           │   │   ├── DashboardScreen.kt
│           │   │   ├── BikeListScreen.kt
│           │   │   └── DetailScreens.kt
│           │   ├── theme/
│           │   │   └── Theme.kt            # Material Design 3
│           │   └── viewmodels/
│           │       ├── AuthViewModel.kt
│           │       ├── BikeViewModel.kt
│           │       └── BookingViewModel.kt
├── build.gradle                         # Root config
├── settings.gradle                      # Project structure
├── gradle.properties                    # Gradle settings
├── README.md                            # Project documentation
├── SETUP.md                             # Detailed setup guide
├── QUICK_START.md                       # This file
└── .gitignore                           # Git ignore rules
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                             │
│  Screens (Compose) + ViewModels + Theme                 │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────────┐
│                 ViewModel Layer                          │
│  AuthViewModel, BikeViewModel, BookingViewModel         │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────────┐
│               Repository Layer                          │
│  AuthRepository, BikeRepository, BookingRepository      │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────────┐
│                 API/Network Layer                        │
│  Retrofit + OkHttp + AuthInterceptor + JWT Handling     │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────┴──────────────────────────────────────┐
│              Backend (Spring Boot)                       │
│    REST API at http://10.0.2.2:8080/api/               │
└─────────────────────────────────────────────────────────┘
```

## Key Dependencies

- **Jetpack Compose**: Modern declarative UI framework
- **Retrofit 2**: Type-safe HTTP client
- **OkHttp 3**: HTTP interceptor for JWT
- **DataStore**: Secure preferences storage
- **Coroutines**: Asynchronous operations
- **Material3**: Design system
- **Timber**: Logging utility

## Common Tasks

### Check Backend Connection
```bash
curl http://10.0.2.2:8080/api/bikes
```

### Clean Build
```bash
./gradlew clean build
```

### View Device Logs
In Android Studio Terminal:
```bash
./gradlew --info run
```

### Run Tests
```bash
./gradlew test
```

## Environment Variables (Optional)

Create `.env` or set in `app/build.gradle`:
```gradle
buildConfigField "String", "BASE_URL", "\"http://YOUR_BACKEND_URL/api/\""
buildConfigField "String", "GOOGLE_CLIENT_ID", "\"YOUR_CLIENT_ID\""
```

## Troubleshooting Quick Reference

| Issue | Solution |
|-------|----------|
| Can't connect to backend | Check `BASE_URL` in build.gradle, ensure backend running |
| App crashes on startup | Check logcat for errors, try clean build |
| Gradle sync fails | File > Invalidate Caches > Restart |
| Google Sign-In not working | Verify Client ID, check SHA-1 fingerprint |
| Emulator too slow | Use hardware acceleration or switch to physical device |

## Next Features to Implement

- [ ] Real-time GPS tracking for bikes
- [ ] Google Maps integration for nearby bikes
- [ ] Push notifications for booking updates
- [ ] In-app payment integration
- [ ] Ride analytics dashboard
- [ ] Dark mode support
- [ ] Offline support with local caching
- [ ] Unit and integration tests
- [ ] CI/CD pipeline configuration

## Support & Contact

For issues or questions:
1. Check SETUP.md for detailed setup instructions
2. Review Android logcat for error messages
3. Verify backend is running and accessible
4. Check internet connectivity on device
5. Review API response in Network Inspector

---

**Version**: 1.0.0  
**Min SDK**: 24  
**Target SDK**: 34  
**Last Updated**: March 2026
