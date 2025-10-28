# Meet-Fit

Meet-Fit is an Android application that helps fitness enthusiasts find training partners, manage meetups, and share their activity preferences. The app combines Firebase-backed authentication, rich user profiles, and event planning tools to streamline coordinating workouts with people nearby.

## Features
- **Firebase authentication** with email sign-up, login, and credential updates, all exposed through a single `MainActivity` controller.【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L82-L148】【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L228-L283】
- **Profile onboarding** flow that collects contact details, fitness level, preferred activities, and profile photos, storing everything in Firebase Realtime Database.【F:app/src/main/java/com/example/meet_fit/fragmetns/userinfo.java†L90-L177】【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L284-L360】
- **Social discovery feed** backed by a `RecyclerView` that surfaces other users' profiles while filtering out the current account.【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L361-L425】【F:app/src/main/java/com/example/meet_fit/adapters/RecyclerAdapter.java†L35-L118】
- **Event creation & management** with time, location, and activity details saved per user under an `events` collection in Firebase.【F:app/src/main/java/com/example/meet_fit/fragmetns/addEvent.java†L70-L169】【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L495-L560】
- **Location-aware suggestions** that rely on fused location services, Google Geocoding, and Places Autocomplete APIs to populate city selections and show "My Location" helpers.【F:app/src/main/java/com/example/meet_fit/fragmetns/addEvent.java†L260-L368】【F:app/src/main/java/com/example/meet_fit/fragmetns/userinfo.java†L255-L368】
- **Media handling utilities** for cropping and encoding avatar images as Base64 strings for storage and Glide-backed display.【F:app/src/main/java/com/example/meet_fit/models/dataAdapter.java†L58-L142】【F:app/src/main/java/com/example/meet_fit/adapters/RecyclerAdapter.java†L64-L113】

## Tech Stack
- **Language:** Java (Android)
- **Architecture:** Single-activity, multi-fragment navigation using the Jetpack Navigation Component.【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L106-L211】
- **Backend:** Firebase Authentication and Firebase Realtime Database for user and event persistence.【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L232-L360】【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L495-L560】
- **Location Services:** Google Play Services (FusedLocationProviderClient), Google Maps Geocoding API, and Places Autocomplete API.【F:app/src/main/java/com/example/meet_fit/fragmetns/addEvent.java†L205-L368】
- **Image Loading:** Glide for rendering profile photos and event imagery.【F:app/src/main/java/com/example/meet_fit/adapters/RecyclerAdapter.java†L64-L113】【F:app/build.gradle.kts†L33-L36】
- **Networking:** OkHttp client for REST calls to Google APIs.【F:app/src/main/java/com/example/meet_fit/fragmetns/addEvent.java†L324-L368】【F:app/build.gradle.kts†L33-L36】

## Project Structure
```
app/
  src/main/java/com/example/meet_fit/
    activities/         # Activity hosts navigation graph and Firebase gateways
    fragmetns/          # UI flows for auth, onboarding, profile, events, and discovery
    adapters/           # RecyclerView adapters and view holders
    models/             # Data classes and helpers (Info, Event, User, dataAdapter)
  src/main/res/         # Layout XMLs, navigation graph, drawable resources
  google-services.json  # Firebase configuration placeholder
build.gradle.kts        # Root Gradle configuration
app/build.gradle.kts    # Android application module configuration
```

## Getting Started
### Prerequisites
- Android Studio Jellyfish or newer with Android SDK 34 installed.
- JDK 17 (bundled with recent Android Studio releases).
- A Firebase project configured for Email/Password authentication and Realtime Database.
- Google Cloud project with Geocoding and Places APIs enabled.

### Clone & Open
```bash
git clone https://github.com/your-org/meet-fit.git
cd meet-fit
```
1. Open the project in **Android Studio** (`File` → `Open...`).
2. Let Gradle sync complete and resolve dependencies.

### Firebase Setup
1. In the Firebase console, enable **Email/Password** under Authentication.
2. Create a **Realtime Database** in production mode.
3. Replace the bundled `app/google-services.json` with the config downloaded from your Firebase project.
4. Update database rules to allow authenticated read/write access.

The `MainActivity` helper methods expect the following structure under the `users` node:
```
users/
  <uid>/
    user/    # Auth credentials metadata (email, name)
    info/    # Profile details collected in the onboarding flow
    events/  # Events created by the user
```
【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L232-L360】【F:app/src/main/java/com/example/meet_fit/activities/MainActivity.java†L495-L560】

### Google API Keys
API keys are currently hard-coded in `fragmetns/addEvent.java` and `fragmetns/userinfo.java` for Geocoding and Places lookups. Replace the placeholder strings (`AIza...`) with credentials from your Google Cloud project before shipping builds.【F:app/src/main/java/com/example/meet_fit/fragmetns/addEvent.java†L321-L345】【F:app/src/main/java/com/example/meet_fit/fragmetns/userinfo.java†L318-L343】

For better security, move these keys into secure storage such as the Android NDK, encrypted preferences, or remote config before production release.

### Run the App
1. Connect an Android device or start an emulator (Android 8.0/Oreo API 26 or later).
2. Use **Run › Run 'app'** from Android Studio to install and launch.
3. Register a new account, complete your profile, and explore nearby fitness partners.

## Testing
Execute the Gradle unit tests from the command line:
```bash
./gradlew test
```
Instrumented tests are located under `app/src/androidTest`; run them from Android Studio's **Run Tests** configuration for on-device verification.

## Contributing
1. Fork the repository and create a feature branch.
2. Follow the existing Java code style and add tests when introducing new logic.
3. Submit a pull request describing the changes, screenshots (for UI tweaks), and testing evidence.

## Security & Privacy Notes
- Never commit production API keys or Firebase configuration with unrestricted permissions.
- Review Firebase rules to ensure only authenticated users can read/write sensitive data.
- Consider implementing moderation or reporting tools before public release.

## License
Specify your project's license here (e.g., MIT, Apache 2.0). If no license is chosen yet, add one before distributing builds.
