# Android Startup Template — Architecture & Usage Docs

> A reusable, production-ready Android template for building multiple apps without starting from scratch. Built on Kotlin + Jetpack Compose + clean architecture.

---

## Table of Contents

1. [Project Structure](#1-project-structure)
2. [Tech Stack](#2-tech-stack)
3. [Presentation Layer](#3-presentation-layer)
4. [UI System](#4-ui-system)
5. [Domain Layer](#5-domain-layer)
6. [Data Layer](#6-data-layer)
7. [Core Infrastructure](#7-core-infrastructure)
8. [Cross-Cutting Concerns](#8-cross-cutting-concerns)
9. [Testing](#9-testing)
10. [CI/CD Pipeline](#10-cicd-pipeline)
11. [Multi-App Strategy](#11-multi-app-strategy)
12. [Per-App Customization Checklist](#12-per-app-customization-checklist)

---

## 1. Project Structure

```
root/
├── app/                          # App shell (per-app module)
│   ├── src/main/
│   │   ├── MainActivity.kt
│   │   ├── AppNavGraph.kt
│   │   └── di/AppModule.kt
│   └── build.gradle.kts
│
├── core/
│   ├── core-ui/                  # Theme, components, base ViewModels
│   ├── core-data/                # Network, DB, DataStore, base repos
│   ├── core-domain/              # Base use case classes, models
│   └── core-common/              # Extensions, utils, sealed errors
│
├── feature/
│   ├── feature-auth/             # Login, register, forgot password
│   ├── feature-onboarding/       # Splash, welcome, permissions
│   └── feature-settings/         # Theme, account, notifications
│
├── brand-kit/                    # Per-app colors, typography, icons
├── gradle/
│   └── libs.versions.toml        # Shared version catalog
└── build.gradle.kts
```

### Module Dependency Rules

```
app  →  feature-*  →  core-domain  →  core-common
                   →  core-data    →  core-common
         brand-kit →  core-ui
```

- `feature-*` modules **never** depend on each other
- `core-data` **never** depends on any `feature-*`
- `app` is the only module allowed to depend on all features

---

## 2. Tech Stack

| Layer | Library | Version | Why |
|---|---|---|---|
| Language | Kotlin | 2.x | Null safety, coroutines, conciseness |
| UI | Jetpack Compose | BOM latest | Declarative, no XML |
| Navigation | Navigation Compose | 2.8.x | Type-safe, back stack management |
| DI | Hilt | 2.51.x | Compile-time verified, Compose support |
| Async | Coroutines + Flow | 1.8.x | Structured concurrency, reactive streams |
| Networking | Retrofit + OkHttp | 2.11.x / 4.12.x | Industry standard, interceptor support |
| Serialization | Kotlinx Serialization | 1.7.x | Kotlin-native, fast |
| Local DB | Room | 2.6.x | Type-safe SQLite, Flow support |
| Preferences | DataStore (Proto) | 1.1.x | Async, type-safe, replaces SharedPrefs |
| Image loading | Coil | 3.x | Compose-native, lightweight |
| Logging | Timber | 5.x | Structured, removable in release |
| Crash reporting | Firebase Crashlytics | latest | Free, integrates with Play Console |
| Analytics | Firebase Analytics | latest | Swappable via abstraction interface |
| Push | Firebase Cloud Messaging | latest | Standard push infrastructure |
| Testing (unit) | JUnit5 + Mockk + Turbine | latest | Kotlin-idiomatic mocking + Flow testing |
| Testing (UI) | Compose UI Test | BOM latest | Official Compose testing framework |
| Lint | Ktlint + Detekt | latest | Code style + static analysis |
| CI | GitHub Actions | — | Free for public/private repos |
| Build | Gradle (KTS) + Version Catalog | 8.x | Type-safe build scripts |

### `libs.versions.toml` (shared across all apps)

```toml
[versions]
kotlin = "2.0.0"
compose-bom = "2024.09.00"
hilt = "2.51.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
room = "2.6.1"
datastore = "1.1.1"
navigation = "2.8.0"
coroutines = "1.8.1"
coil = "3.0.0"
timber = "5.0.1"
mockk = "1.13.12"
turbine = "1.1.0"
detekt = "1.23.7"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
datastore = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }

[bundles]
compose = ["compose-ui", "compose-material3", "compose-ui-tooling-preview"]
room = ["room-runtime", "room-ktx"]
```

---

## 3. Presentation Layer

### 3.1 Onboarding — `feature-onboarding`

**What it includes:**

- `SplashScreen.kt` — branded launch screen using the SplashScreen API (no custom delay logic)
- `WelcomeScreen.kt` — paged welcome carousel (HorizontalPager), skip button, get started CTA
- `PermissionsScreen.kt` — declarative permission request UI using Accompanist or `rememberPermissionState`

**Usage:** Show on first launch only. Gate with a DataStore flag (`onboarding_complete: Boolean`).

```kotlin
// In AppNavGraph.kt
val onboardingComplete by viewModel.onboardingComplete.collectAsState()

NavHost(
    startDestination = if (onboardingComplete) Screen.Home else Screen.Onboarding
) { ... }
```

**Files to customize per app:**
- Welcome carousel text and illustrations
- Which permissions to request (camera, notifications, location)
- Brand colors (via `brand-kit`)

---

### 3.2 Auth — `feature-auth`

**What it includes:**

- `LoginScreen.kt` — email/password form with loading state, error display
- `RegisterScreen.kt` — name, email, password, confirm password
- `ForgotPasswordScreen.kt` — email input, success state
- `AuthViewModel.kt` — handles form validation, delegates to use cases

**Navigation flow:**

```
Login  ──────────────────────►  Home
  │                               ▲
  ▼                               │
Register ──► (auto login) ────────┘
  │
  ▼
ForgotPassword ──► (back to Login)
```

**Files to customize per app:**
- Social auth buttons (Google, Apple) if needed — add as optional composable slots
- Branding on the login screen header
- Terms & Privacy Policy URLs

---

### 3.3 Home Shell — `app` module

**What it includes:**

- `MainActivity.kt` — single activity, sets up Compose content
- `AppScaffold.kt` — wraps screens with bottom nav bar or nav drawer
- `BottomNavBar.kt` — pre-built with 3–5 tab slot configuration
- `AppNavGraph.kt` — central navigation graph wiring all features

```kotlin
// AppScaffold.kt
@Composable
fun AppScaffold(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        AppNavGraph(navController, Modifier.padding(padding))
    }
}
```

**Files to customize per app:**
- Number of tabs and their icons
- Top-level destinations in `AppNavGraph`

---

### 3.4 Settings — `feature-settings`

**What it includes:**

- `SettingsScreen.kt` — scrollable preference-style list
- `ThemeSection` — light / dark / system toggle (stored in DataStore)
- `AccountSection` — display name, email, profile picture, sign out
- `NotificationsSection` — FCM opt-in/opt-out toggle
- `AboutSection` — version number, privacy policy, terms link

---

## 4. UI System

All UI system code lives in `core-ui`.

### 4.1 Theme Engine

```kotlin
// core-ui/theme/AppTheme.kt
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    brandColors: BrandColors = LocalBrandColors.current,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) brandColors.darkScheme else brandColors.lightScheme
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}
```

**`BrandColors`** is the override point per app (see [Section 11](#11-multi-app-strategy)).

**Typography scale:** Define once in `AppTypography.kt` using Material3 type roles (displayLarge, headlineMedium, bodyLarge, etc.). Use `sp` units only.

---

### 4.2 Component Library

Pre-built components in `core-ui/components/`:

| Component | File | Usage |
|---|---|---|
| Primary button | `AppButton.kt` | Main CTA actions |
| Text button | `AppTextButton.kt` | Secondary actions, links |
| Input field | `AppTextField.kt` | All text inputs with error state |
| Loading overlay | `LoadingOverlay.kt` | Full-screen blocking loader |
| Error state | `ErrorScreen.kt` | Full-screen error with retry |
| Empty state | `EmptyScreen.kt` | No data placeholder |
| Snackbar host | `AppSnackbarHost.kt` | Centralized toast/snackbar |
| Bottom sheet | `AppBottomSheet.kt` | Modal bottom sheets |
| Top app bar | `AppTopBar.kt` | Back navigation, title, actions |
| Avatar | `UserAvatar.kt` | Circular image or initials fallback |
| Confirmation dialog | `ConfirmDialog.kt` | Destructive action confirmation |

---

### 4.3 Base ViewModel

All ViewModels extend `BaseViewModel` from `core-ui`:

```kotlin
// core-ui/base/BaseViewModel.kt
abstract class BaseViewModel<State, Event, Effect> : ViewModel() {

    abstract val initialState: State

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    protected fun setState(reducer: State.() -> State) {
        _state.update { it.reducer() }
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    abstract fun onEvent(event: Event)
}
```

**Usage pattern (MVI):**

```kotlin
// Every screen has:
data class LoginUiState(val email: String = "", val isLoading: Boolean = false, val error: String? = null)
sealed class LoginEvent { data class EmailChanged(val value: String) : LoginEvent(); object Submit : LoginEvent() }
sealed class LoginEffect { object NavigateHome : LoginEffect(); data class ShowError(val msg: String) : LoginEffect() }

class LoginViewModel : BaseViewModel<LoginUiState, LoginEvent, LoginEffect>() { ... }
```

---

### 4.4 Navigation

Use type-safe navigation with Navigation Compose 2.8+:

```kotlin
// core-ui/navigation/Screen.kt
@Serializable sealed class Screen {
    @Serializable object Onboarding : Screen()
    @Serializable object Login : Screen()
    @Serializable object Home : Screen()
    @Serializable data class UserDetail(val userId: String) : Screen()
}
```

Pass `NavHostController` only at the top-level `AppNavGraph`. Features expose lambda callbacks for navigation, not direct navController access.

---

## 5. Domain Layer

All domain code lives in `core-domain`. No Android framework dependencies here — pure Kotlin.

### 5.1 Base Use Case

```kotlin
// core-domain/base/UseCase.kt
abstract class UseCase<in Params, out Result> {
    abstract suspend operator fun invoke(params: Params): kotlin.Result<Result>
}

abstract class FlowUseCase<in Params, out Result> {
    abstract operator fun invoke(params: Params): Flow<Result>
}

object NoParams  // Use when a use case takes no parameters
```

---

### 5.2 Auth Use Cases

Located in `core-domain/auth/`:

| Use Case | Input | Output | Description |
|---|---|---|---|
| `LoginUseCase` | `LoginParams(email, password)` | `Result<User>` | Authenticates, persists token |
| `RegisterUseCase` | `RegisterParams(name, email, password)` | `Result<User>` | Creates account |
| `LogoutUseCase` | `NoParams` | `Result<Unit>` | Clears token + local session |
| `RefreshTokenUseCase` | `NoParams` | `Result<String>` | Called by auth interceptor |
| `GetCurrentUserUseCase` | `NoParams` | `Flow<User?>` | Emits null when logged out |
| `ForgotPasswordUseCase` | `email: String` | `Result<Unit>` | Triggers reset email |

---

### 5.3 User Profile Use Cases

Located in `core-domain/user/`:

| Use Case | Input | Output |
|---|---|---|
| `GetUserProfileUseCase` | `NoParams` | `Flow<UserProfile>` |
| `UpdateUserProfileUseCase` | `UpdateProfileParams` | `Result<UserProfile>` |
| `DeleteAccountUseCase` | `NoParams` | `Result<Unit>` |
| `UploadAvatarUseCase` | `Uri` | `Result<String>` (URL) |

---

### 5.4 Domain Models

```kotlin
// core-domain/model/User.kt
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val createdAt: Long
)

// core-domain/model/AppError.kt
sealed class AppError : Exception() {
    data class NetworkError(val code: Int?, override val message: String?) : AppError()
    data object Unauthorized : AppError()
    data object NotFound : AppError()
    data class ServerError(override val message: String?) : AppError()
    data class Unknown(override val cause: Throwable?) : AppError()
}
```

---

### 5.5 Analytics Abstraction

```kotlin
// core-domain/analytics/AnalyticsTracker.kt
interface AnalyticsTracker {
    fun trackScreen(name: String)
    fun trackEvent(name: String, params: Map<String, Any> = emptyMap())
    fun trackError(error: Throwable, context: String)
}

// core-data provides FirebaseAnalyticsTracker : AnalyticsTracker
// You can swap this for Mixpanel, Amplitude, etc. without touching feature code
```

---

## 6. Data Layer

All data code lives in `core-data`.

### 6.1 Network Client

```kotlin
// core-data/network/NetworkModule.kt  (Hilt module)
@Provides @Singleton
fun provideOkHttpClient(
    authInterceptor: AuthInterceptor,
    loggingInterceptor: HttpLoggingInterceptor
): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)   // Adds Bearer token to every request
    .addInterceptor(loggingInterceptor) // Logs in DEBUG only
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

**`AuthInterceptor`** — reads token from DataStore, attaches as `Authorization: Bearer <token>`. On 401, triggers `RefreshTokenUseCase` once, retries. On second 401, emits logout event.

**Error mapping** — a `ResponseErrorMapper` converts HTTP error codes to `AppError` sealed class.

---

### 6.2 Local Database (Room)

```kotlin
// core-data/db/AppDatabase.kt
@Database(
    entities = [UserEntity::class],   // Add entities per app
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

// core-data/db/base/BaseDao.kt
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: T)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(entities: List<T>)
    @Delete suspend fun delete(entity: T)
}
```

**Migration strategy:** Use `Room.databaseBuilder` with `addMigrations(...)`. Keep migration files in `core-data/db/migrations/`. Never use `fallbackToDestructiveMigration()` in production.

---

### 6.3 DataStore

Used for lightweight preferences — not for large data (use Room for that).

```kotlin
// core-data/datastore/AppPreferences.kt
class AppPreferences(private val dataStore: DataStore<Preferences>) {

    val authToken: Flow<String?> = dataStore.data.map { it[KEY_AUTH_TOKEN] }
    val isDarkMode: Flow<Boolean> = dataStore.data.map { it[KEY_DARK_MODE] ?: false }
    val onboardingComplete: Flow<Boolean> = dataStore.data.map { it[KEY_ONBOARDING] ?: false }
    val fcmToken: Flow<String?> = dataStore.data.map { it[KEY_FCM_TOKEN] }

    suspend fun setAuthToken(token: String) { dataStore.edit { it[KEY_AUTH_TOKEN] = token } }
    suspend fun clearAll() { dataStore.edit { it.clear() } }

    companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token")
    }
}
```

---

### 6.4 Base Repository Pattern

```kotlin
// core-data/repository/base/BaseRepository.kt
abstract class BaseRepository {
    protected suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> = try {
        Result.success(call())
    } catch (e: HttpException) {
        Result.failure(errorMapper.map(e))
    } catch (e: IOException) {
        Result.failure(AppError.NetworkError(null, e.message))
    } catch (e: Exception) {
        Result.failure(AppError.Unknown(e))
    }
}
```

---

## 7. Core Infrastructure

### 7.1 Hilt DI Setup

Module structure:

| Module | Provides | Scope |
|---|---|---|
| `AppModule` | `Context`, `CoroutineScope` | `@Singleton` |
| `NetworkModule` | `OkHttpClient`, `Retrofit`, API services | `@Singleton` |
| `DatabaseModule` | `AppDatabase`, DAOs | `@Singleton` |
| `DataStoreModule` | `AppPreferences` | `@Singleton` |
| `RepositoryModule` | All repository bindings | `@Singleton` |
| `AnalyticsModule` | `AnalyticsTracker` binding | `@Singleton` |

**Hilt entry point in `Application`:**

```kotlin
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else CrashlyticsTree())
    }
}
```

---

### 7.2 Error Handling

Central sealed class `AppError` (defined in `core-domain`) is the only error type that escapes the data layer. The UI maps it to user-facing strings:

```kotlin
// core-ui/error/ErrorMapper.kt
fun AppError.toUserMessage(context: Context): String = when (this) {
    is AppError.NetworkError -> context.getString(R.string.error_network)
    is AppError.Unauthorized -> context.getString(R.string.error_session_expired)
    is AppError.NotFound -> context.getString(R.string.error_not_found)
    is AppError.ServerError -> message ?: context.getString(R.string.error_server)
    is AppError.Unknown -> context.getString(R.string.error_unknown)
}
```

**Rule:** Never let `Exception`, `IOException`, or `HttpException` reach the ViewModel. Convert at the repository boundary.

---

### 7.3 Logging

```kotlin
// Debug builds: Timber.DebugTree() — logs to Logcat
// Release builds: CrashlyticsTree() — logs warnings/errors to Crashlytics

class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return
        FirebaseCrashlytics.getInstance().log(message)
        t?.let { FirebaseCrashlytics.getInstance().recordException(it) }
    }
}
```

**Rule:** Use `Timber.d/i/w/e()` everywhere. Never use `Log.*` directly.

---

### 7.4 Push Notifications (FCM)

```kotlin
// core-data/push/AppMessagingService.kt
@AndroidEntryPoint
class AppMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Save to DataStore + sync to your backend
        serviceScope.launch { appPreferences.setFcmToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Parse and show notification
        notificationManager.show(message.toAppNotification())
    }
}
```

**Deep link handling:** Notifications should include a `destination` key in the data payload. `AppNavGraph` reads this on launch via `intent.data` and navigates accordingly.

---

## 8. Cross-Cutting Concerns

### 8.1 Feature Flags

```kotlin
// core-common/flags/FeatureFlag.kt
enum class FeatureFlag(val defaultValue: Boolean) {
    DARK_MODE_TOGGLE(true),
    SOCIAL_LOGIN(false),
    IN_APP_PURCHASE(false),
    NEW_ONBOARDING(false),
}

// core-data/flags/FeatureFlagManager.kt
class FeatureFlagManager(
    private val remoteConfig: FirebaseRemoteConfig,  // Optional; can skip
    private val localOverrides: Map<FeatureFlag, Boolean> = emptyMap()
) {
    fun isEnabled(flag: FeatureFlag): Boolean =
        localOverrides[flag]
            ?: remoteConfig.getBoolean(flag.name)  // Falls back gracefully
            ?: flag.defaultValue
}
```

Usage in Compose:

```kotlin
if (featureFlagManager.isEnabled(FeatureFlag.SOCIAL_LOGIN)) {
    SocialLoginSection()
}
```

---

### 8.2 Runtime Permission Utils

```kotlin
// core-ui/permission/PermissionHandler.kt
@Composable
fun rememberPermissionHandler(
    permission: String,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    onPermanentlyDenied: () -> Unit
): ManagedActivityResultLauncher<String, Boolean> { ... }
```

**`PermissionsScreen`** (in `feature-onboarding`) uses this to batch-request permissions on first launch with explanatory UI before each system dialog.

---

### 8.3 In-App Update + Rating

```kotlin
// core-common/update/InAppUpdateManager.kt
class InAppUpdateManager(private val context: Context) {
    fun checkForUpdate(activity: Activity) {
        AppUpdateManagerFactory.create(context)
            .appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    // Trigger flexible update flow
                }
            }
    }
}

// Rating prompt — trigger after user completes a key action (not on first launch)
// core-common/rating/RatingManager.kt
class RatingManager(private val context: Context) {
    fun requestReview(activity: Activity) {
        ReviewManagerFactory.create(context)
            .requestReviewFlow()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) activity.launchReviewFlow(task.result)
            }
    }
}
```

**Rule:** Show rating prompt after 3+ app opens AND at least one meaningful action completed. Never show on launch.

---

### 8.4 In-App Purchases (Billing)

```kotlin
// core-data/billing/BillingRepository.kt
interface BillingRepository {
    val purchases: Flow<List<Purchase>>
    suspend fun queryProducts(productIds: List<String>): List<ProductDetails>
    suspend fun launchPurchase(activity: Activity, product: ProductDetails): BillingResult
    suspend fun restorePurchases(): List<Purchase>
}
```

Use `BillingClient` from `com.android.billingclient:billing-ktx`. Wrap everything in `BillingRepository` so features never call `BillingClient` directly. Verify purchases server-side — never trust client-side only.

---

## 9. Testing

### 9.1 Unit Tests

Base class for all ViewModel tests:

```kotlin
// core-test/base/BaseViewModelTest.kt
abstract class BaseViewModelTest {
    @get:Rule val coroutineRule = MainCoroutineRule()  // Replaces Dispatchers.Main with TestDispatcher

    protected fun <T> Flow<T>.test(block: suspend TurbineContext<T>.() -> Unit) =
        runTest { this@test.test(block) }
}
```

Example ViewModel test:

```kotlin
class LoginViewModelTest : BaseViewModelTest() {

    private val loginUseCase = mockk<LoginUseCase>()
    private val viewModel by lazy { LoginViewModel(loginUseCase) }

    @Test
    fun `submit with valid credentials emits NavigateHome effect`() = runTest {
        coEvery { loginUseCase(any()) } returns Result.success(fakeUser)

        viewModel.effect.test {
            viewModel.onEvent(LoginEvent.Submit)
            assertThat(awaitItem()).isEqualTo(LoginEffect.NavigateHome)
        }
    }
}
```

**Fake data builders** live in `core-test/fakes/`:

```kotlin
object FakeUser {
    fun build(id: String = "1", email: String = "test@test.com") = User(id, email, "Test User", null, 0L)
}
```

---

### 9.2 UI Tests (Compose)

```kotlin
// core-test/compose/ComposeTestBase.kt
abstract class ComposeTestBase {
    @get:Rule val composeRule = createComposeRule()
    @get:Rule val hiltRule = HiltAndroidRule(this)  // If using Hilt
}

class LoginScreenTest : ComposeTestBase() {
    @Test
    fun `login button is disabled when email is empty`() {
        composeRule.setContent { LoginScreen(state = LoginUiState(), onEvent = {}) }
        composeRule.onNodeWithTag("login_button").assertIsNotEnabled()
    }
}
```

---

## 10. CI/CD Pipeline

File: `.github/workflows/android.yml`

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/libs.versions.toml') }}

      - name: Run Ktlint
        run: ./gradlew ktlintCheck

      - name: Run Detekt
        run: ./gradlew detekt

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: '**/build/reports/tests/'

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Build release AAB
        run: ./gradlew bundleRelease

      - name: Sign AAB
        uses: r0adkll/sign-android-release@v1
        with:
          releaseDirectory: app/build/outputs/bundle/release
          signingKeyBase64: ${{ secrets.SIGNING_KEY }}
          alias: ${{ secrets.KEY_ALIAS }}
          keyStorePassword: ${{ secrets.KEY_STORE_PASSWORD }}

      - name: Upload to Play Store (internal track)
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.SERVICE_ACCOUNT_JSON }}
          packageName: com.yourcompany.appname
          releaseFiles: app/build/outputs/bundle/release/*.aab
          track: internal
```

**Required GitHub Secrets:**
- `SIGNING_KEY` — base64-encoded keystore file
- `KEY_ALIAS` — keystore alias
- `KEY_STORE_PASSWORD` — keystore password
- `SERVICE_ACCOUNT_JSON` — Google Play service account JSON

---

## 11. Multi-App Strategy

### How to create a new app from the template

**Step 1:** Copy the template repo or use it as a GitHub template.

**Step 2:** Create a new `:app` module with a unique `applicationId`:
```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        applicationId = "com.yourcompany.newapp"
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

**Step 3:** Override brand colors and typography in `brand-kit/`:
```kotlin
// brand-kit/src/main/BrandColors.kt
val MyNewAppBrandColors = BrandColors(
    lightScheme = lightColorScheme(
        primary = Color(0xFF1A73E8),
        secondary = Color(0xFF34A853),
        // ...
    ),
    darkScheme = darkColorScheme(...)
)
```

**Step 4:** Replace app icons in `app/src/main/res/mipmap-*/`

**Step 5:** Configure `google-services.json` for the new Firebase project.

**Step 6:** Enable only the features you need in `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":feature-auth"))        // always include
    implementation(project(":feature-onboarding"))  // always include
    implementation(project(":feature-settings"))    // always include
    implementation(project(":feature-your-thing"))  // app-specific
}
```

---

### What changes per app vs what is shared

| Component | Shared (template) | Per-app |
|---|---|---|
| Auth screens | UI structure | Backend URL, OAuth config |
| Onboarding | Flow logic | Text, images, which permissions |
| Theme engine | The engine itself | Color values, font choice |
| Component library | All components | — |
| Base ViewModels | — (always shared) | — |
| Navigation | Nav graph scaffolding | Destinations added |
| Network client | Setup, interceptors | Base URL, API service interfaces |
| Room DB | Base DAOs, migrations helper | Entity definitions |
| Error handling | All of it | Custom error messages |
| CI pipeline | Entire workflow | Package name, signing config |
| Feature flags | Manager + enum | Which flags are defined |

---

## 12. Per-App Customization Checklist

Use this checklist each time you start a new app from the template:

**Identity**
- [ ] Update `applicationId` in `app/build.gradle.kts`
- [ ] Update `app_name` in `strings.xml`
- [ ] Replace all icons in `mipmap-*/` and `drawable/`
- [ ] Set brand colors in `brand-kit/BrandColors.kt`
- [ ] Set brand typography (font family) in `brand-kit/BrandTypography.kt`

**Backend**
- [ ] Set `BASE_URL` in `gradle.properties` (or per build flavor)
- [ ] Add app-specific API service interfaces in `core-data/network/`
- [ ] Configure new Firebase project and add `google-services.json`
- [ ] Update FCM notification channel name and icon

**Features**
- [ ] Customize onboarding carousel content
- [ ] Add only the `feature-*` modules this app needs
- [ ] Define app-specific `FeatureFlag` entries
- [ ] Configure which permissions to request in onboarding

**Release**
- [ ] Add keystore and secrets to GitHub repository secrets
- [ ] Set Play Store package name in CI workflow
- [ ] Configure ProGuard / R8 rules for any added libraries
- [ ] Set up Firebase Crashlytics and Analytics in the Firebase console

---

*Template maintained by Somnath Jha — update `libs.versions.toml` regularly to stay on latest stable dependencies.*