# E-Bike Mobile App - Development Guide

## Architecture & Design Patterns

### MVVM Architecture
```
View (Composable) → ViewModel → Repository → API
     ↓                                        ↓
  State Updates      Local Storage (DataStore)
```

### Clean Architecture Principles
- **Separation of Concerns**: Each layer has distinct responsibilities
- **Testability**: ViewModels and Repositories can be tested independently
- **Reusability**: Repositories can be used by multiple ViewModels
- **Maintainability**: Changes in one layer don't affect others

## Code Organization

### api/
Handles all HTTP operations and networking:
- `BikeRentalApi.kt`: Retrofit interface defining REST endpoints
- `AuthInterceptor.kt`: OkHttp interceptor that adds JWT to requests
- `RetrofitClient.kt`: Singleton Retrofit instance factory

### data/
Manages data operations:
#### local/
- `TokenManager.kt`: Secure token storage using DataStore, encrypts tokens at rest

#### models/
- `Models.kt`: Kotlin data classes with @Parcelize for serialization
  - User, Bike, Booking (match backend entities exactly)
  - DTO classes for API requests
  - ApiResponse wrapper for all API responses

#### repository/
- `AuthRepository.kt`: Authentication logic (login, register, logout)
- `BikeRepository.kt`: Bike operations (fetch, search, filter)
- `BookingRepository.kt`: Booking operations (create, cancel, view history)

Repositories handle:
- API calls via Retrofit
- Error handling and Result wrapping
- Data transformation from API responses
- Token/auth management integration

### ui/
All UI components and state management:

#### screens/
- `Navigation.kt`: Navigation graph setup using Jetpack Navigation Compose
- `*Screen.kt`: Composable UI screens
  - LoginScreen: Email/password + Google OAuth
  - RegisterScreen: New user signup
  - DashboardScreen: Main app hub with quick actions
  - BikeListScreen: Browsable list with filtering
  - DetailScreens.kt: Bike details, booking history, profile

State flow: Screen → ViewModel → Repository → API/Storage

#### theme/
- `Theme.kt`: Material Design 3 color scheme and typography
- Green primary color (#10B981) matches website branding

#### viewmodels/
- `AuthViewModel.kt`: Login/register/logout state management
- `BikeViewModel.kt`: Bike list, filter, detail state
- `BookingViewModel.kt`: Booking operations and history

Each ViewModel:
- Manages UI state with `StateFlow`
- Handles business logic via repositories
- Exposes state as immutable flows
- Launches coroutines for async operations

## Data Flow Example: Login

```
user inputs credentials
        ↓
onSubmit clicked
        ↓
AuthViewModel.login()
        ↓
AuthRepository.login()
        ↓
BikeRentalApi.login() → HTTP POST /auth/login
        ↓
Backend validates
        ↓
Returns JWT + User
        ↓
TokenManager.saveToken()
TokenManager.saveUserData()
        ↓
ViewModel updates isLoggedIn StateFlow
        ↓
LoginScreen observes isLoggedIn
        ↓
Navigate to Dashboard
```

## Authentication Flow

### JWT Implementation
1. User logs in with credentials
2. Backend returns JWT token (24-hour expiration)
3. Token stored in encrypted DataStore
4. AuthInterceptor automatically adds: `Authorization: Bearer <token>`
5. Every request includes token in header
6. On token expiry, user logged out automatically

### Google OAuth (Ready for Implementation)
1. User clicks "Continue with Google"
2. Google Sign-In SDK handles credential collection
3. Returns ID token
4. Send to backend: `POST /auth/google` with token
5. Backend validates with Google, returns JWT
6. Same flow as email login after this point

## Error Handling

All repositories return `Result<T>`:
- `Result.success(data)`: Operation successful
- `Result.failure(exception)`: Operation failed

ViewModels:
```kotlin
result.onSuccess { data -> /* Update state */ }
       .onFailure { error -> /* Show error UI */ }
```

Screens:
```kotlin
errorMessage?.let { ShowErrorDialog(it) }
```

## State Management

### Using StateFlow for UI State
```kotlin
private val _bikes = MutableStateFlow<List<Bike>>(emptyList())
val bikes: StateFlow<List<Bike>> = _bikes

// In Composable:
val bikes by viewModel.bikes.collectAsState()
val isLoading by viewModel.isLoading.collectAsState()
```

Benefits:
- Thread-safe
- Efficient recomposition
- Observable from Compose
- Survives configuration changes

## Network Operations

### Retrofit Configuration
- Base URL: Dynamically set (emulator vs device)
- Interceptors:
  - AuthInterceptor: Adds JWT + headers
  - HttpLoggingInterceptor: Logs requests/responses
- Timeouts: 30 seconds connect/read/write
- Gson converter for JSON

### Error Responses
Backend returns:
```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE"
}
```

Client wraps in Result and passes to UI layer.

## Testing Strategy (Future)

### Unit Tests
- ViewModel logic
- Repository data transformation
- TokenManager encryption/decryption

### Integration Tests
- API responses
- Full login flow
- Booking lifecycle

### UI Tests
- Screen navigation
- Form validation
- Error states

## Performance Considerations

1. **Lazy Loading**: Bikes loaded on demand, not at startup
2. **Caching**: Retrofit caches responses via OkHttp
3. **Coroutines**: All network calls async, UI not blocked
4. **State Optimization**: Only update when data changes
5. **Compose Recomposition**: Only recompile affected Composables

## Security

### JWT Tokens
- Stored in encrypted DataStore (system-managed encryption)
- Automatically added to all requests
- 24-hour expiration enforced by backend
- Cleared on logout

### Sensitive Data
- Passwords NOT stored locally (only JWT token)
- Google tokens stored encrypted
- No hardcoded API keys/credentials

### Network
- HTTPS ready (uses HTTP for local dev, switch in build.gradle)
- Certificate pinning ready (can implement in AuthInterceptor)

## Logging & Debugging

### Timber Logging
```kotlin
Timber.d("Debug message")
Timber.i("Info message")
Timber.e(exception, "Error message")
```

### Network Logging
OkHttp logs all requests/responses at BODY level:
```
--> POST /api/auth/login
Content-Type: application/json
{"email":"user@example.com","password":"..."}

<-- 200 OK
{"success":true,"data":{"token":"...","user":{...}}}
```

View in Android Studio Logcat:
1. Bottom panel → Logcat tab
2. Filter: `com.ebike.mobile` or `okhttp3`

## Configuration

### Build Variants
Currently: **debug** (development)

Can add:
- **release**: Optimized production build with obfuscation

### Gradle Properties
```gradle
# In gradle.properties
android.useAndroidX=true           # Use AndroidX libraries
kotlin.code.style=official          # Kotlin formatting
org.gradle.caching=true            # Gradle build cache
org.gradle.parallel=true           # Parallel builds
```

## Dependency Management

All dependencies in `app/build.gradle`:
- Keep versions aligned with Material3, Compose, AndroidX
- Check for security updates regularly
- Run `./gradlew dependencyUpdates` to check outdated

## Build Commands Reference

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Run tests
./gradlew test

# Check code quality
./gradlew lint

# View dependency tree
./gradlew dependencies

# View gradle tasks
./gradlew tasks

# Clean build
./gradlew clean
```

## Extending the App

### Adding a New Screen
1. Create `NewScreen.kt` in `ui/screens/`
2. Add route to `Navigation.kt` sealed class
3. Add composable to `NavHost`
4. Create `NewViewModel` if needed

### Adding a New API Endpoint
1. Add method to `BikeRentalApi.kt`
2. Create mapper function i repository
3. Expose via Repository method
4. Update ViewModel to use it

### Adding New Model
1. Create data class in `Models.kt`
2. Add Retrofit endpoint in `BikeRentalApi.kt`
3. Create Repository method
4. Expose in ViewModel

## Common Patterns

### Loading State
```kotlin
when {
    isLoading -> LoadingSpinner()
    errorMessage != null -> ErrorMessage(errorMessage)
    items.isEmpty() -> EmptyState()
    else -> ItemsList(items)
}
```

### Form Submission
```kotlin
Button(
    onClick = {
        if (validate(input)) {
            viewModel.submit(input)
        }
    },
    enabled = !isLoading && isValid
)
```

### Observing State
```kotlin
LaunchedEffect(result) {
    if (result?.isSuccess) navigate()
}
```

## Performance Tips

1. Avoid recomposing entire lists - use `LazyColumn`
2. Use `.distinct()` on state flows to avoid duplicates
3. Cache API responses when possible
4. Load data on demand, not at startup
5. Use `remember` for expensive computations

## Debugging Tips

### Check Backend Connection
```bash
adb shell
curl http://10.0.2.2:8080/api/bikes
```

### View App Database
Android Studio → Database Inspector (if using Room)

### Monitor Network
Android Studio → Network Profiler

### Check App Storage
Device File Explorer → `/data/data/com.ebike.mobile/`

## Future Enhancements

- Implement Room database for offline support
- Add geolocation and map integration
- Implement push notifications
- Add payment gateway integration
- Real-time bike tracking
- Ride statistics and analytics
- Social features (sharing, reviews)
- Advanced filtering and search
- Dark mode support
- Accessibility improvements

---

**Last Updated**: March 2026  
**Version**: 1.0.0  
**Maintainers**: Development Team
