# AGENTS.md

## 1. Project Overview
This is a modern, offline-first expense tracker Android application with a Spanish-language UI. 
- **Current State:** Fully functional offline using local storage (Room only; no DataStore). Single Gradle module `:app`, package `com.eromn.microfintracker`. Networking IS now wired: pending (unread) transactions can be uploaded to a FastAPI backend via Retrofit (`data/remote/TransactionApi.kt`, base URL `https://iemsur.com.mx/api/`). Room remains the single source of truth; the network layer only pushes local data.
- **Future State:** Expand toward full client-server integration. The architecture MUST support this seamlessly: ViewModels/UI must depend only on `domain/repository` interfaces, never on `data/` classes directly.

## 2. Tech Stack & Strict Constraints
- **Language:** Kotlin 2.2.10 (Strict mode, Kotlin sources only; target JVM 11).
- **UI:** Jetpack Compose + Material 3 (Compose BOM 2025.12.00). Adaptive UI 1.2.0 for foldables/tablets.
- **Architecture:** MVVM with Clean Architecture principles (Repository Pattern).
- **Asynchronous:** Kotlin Coroutines and Flow (`StateFlow`/`SharedFlow`). **NO `LiveData`.**
- **Dependency Injection:** **NONE.** Use manual constructor injection. Wire dependencies through a `ViewModelProvider.Factory` (see `HistoryActivity`). Do NOT add Hilt, Dagger, or Koin.
- **Local Database:** Room 2.7.2 (using KSP for annotation processing).
- **Networking (ACTIVE):** Retrofit 3.0.0 + OkHttp 5.0.0 with kotlinx-serialization-json 1.9.0 (`org.jetbrains.kotlin.plugin.serialization` plugin enabled). `INTERNET` permission is declared in the manifest. Currently used only to upload pending transactions (`TransactionApi.createTransaction`, `POST transactions`); no server-to-client sync yet.
- **Upload flow semantics:** `Transaction.isRead == false` means "pending upload". Successful uploads mark the transaction as read; failures leave it unread for retry. Read (already uploaded) transactions can be batch-deleted locally (`deleteRead`).

## 3. Build & Verify (Crucial for CLI Agents)
- **Execution:** Run `./gradlew` directly.
- **JDK Configuration:** The project is configured to use JDK 21 via `org.gradle.java.home` in `gradle.properties`. CLI builds will work out-of-the-box without needing to set `JAVA_HOME` manually.
- **Commands:** 
  - `./gradlew :app:assembleDebug` (Debug builds install with applicationId suffix `.debug`)
  - `./gradlew :app:testDebugUnitTest` (Note: Real unit tests exist now — e.g., `TransactionUploadRepositoryImplTest` runs against a `MockWebServer` and a fake in-memory DAO; `TransactionRepositoryTest` covers local CRUD).
- **Config:** `gradle.properties` enables configuration cache, parallel builds, a 4 GB daemon heap, and pins the Gradle JDK to 21.

## 4. Architecture & Folder Structure
When adding **new** features or files, strictly follow this directory structure. 

```text
app/src/main/java/com/eromn/microfintracker/
│
├── data/
│   ├── local/         # Room Entities, DAOs, and Local Data Sources
│   ├── remote/        # Retrofit interfaces + kotlinx-serialization DTOs (e.g., TransactionApi, TransactionDto)
│   └── repository/    # Concrete implementations of Repository interfaces (e.g., TransactionUploadRepositoryImpl)
│
├── domain/            # Boundary layer: ViewModels/UI must only see these types
│   ├── model/         # Pure Kotlin data classes used by the UI (e.g., UploadResult)
│   └── repository/    # Repository INTERFACES (ViewModels only interact with these)
│
├── ui/
│   ├── components/    # Small, reusable Compose UI components (e.g., ExpensesItem, Buttons)
│   ├── screens/       # Full-screen Composables (e.g., Dashboard, History). DO NOT put screens in components/
│   └── theme/         # Material3 Theme, Colors, Typography
│
├── utils/             # Helper functions, extensions, and utilities
├── viewmodel/         # ViewModels (using StateFlow)
└── [Activity].kt      # Root Compose host activities (e.g., HistoryActivity.kt)
```

## 5. Rules for File Placement & Database
1. **Screens vs Components:** Full pages/routes go in `ui/screens/`. Reusable UI pieces go in `ui/components/`.
2. **Models:** If the UI needs different data than the database (or future API), create a pure Kotlin class in `domain/model/` and map the Entity to it in the Repository.
3. **Repositories:** Interface goes in `domain/repository/`. Implementation goes in `data/repository/`.
4. **FastAPI/Retrofit Code:** All Retrofit interfaces and DTOs MUST go in `data/remote/`. Entities are mapped to DTOs inside `data/repository/` implementations (see `TransactionUploadRepositoryImpl.toDto()`); never expose `data/` types past the domain boundary.
5. **Room Rules:** 
   - Schemas are exported to `app/schemas/com.eromn.microfintracker.data.AppDatabase/` (currently versions 1 and 2, DB at v2 with `AutoMigration(1→2)`). Any entity change requires bumping `version` in `AppDatabase.kt`, adding an `AutoMigration`/`Migration`, and committing the newly generated JSON alongside existing ones.
   - The table name is the SQL keyword `` `transaction` `` — **always backtick it** in `@Query` annotations.
   - User-facing strings belong in `res/values/strings.xml` (Spanish).

## 6. Known Deviations & Technical Debt (DO NOT FIX UNLESS EXPLICITLY ASKED)
These predate the conventions above. Do not "clean them up" during unrelated work, as they are tracked tech debt and refactoring them without context will break the app.

1. **`data/TransactionRepository.kt`:** A concrete class with no interface. (Every other repo follows the interface-in-domain + `Impl` pattern).
2. **`data/Transaction.kt`:** Holds the Room entity, the UI model, and the `Category` enum in one file. **Critical:** `Category.serverId` values have intentional gaps matching an external taxonomy. Never renumber them; unknown/null IDs fall back to `OTHERS` via `Category.fromId`.
3. **`HistoryActivity.kt`:** Dashboard stats (`username`, `monthlySpent`, `todaySpent`) are currently hardcoded placeholder values.
4. **`TransactionUploadRepositoryImpl` / `TransactionDto`:** Upload constants (mock user id, account id, currency id, timezone `"Asia/Taipei"`) are hardcoded server-side defaults; there is no auth yet.

## 7. Misc Agent Notes
- Dependency versions live in `gradle/libs.versions.toml` (Version Catalogs). Add new libraries there, not in `build.gradle.kts` directly.
- `pendientes.md` and `.aider*` files are personal/gitignored scratch notes. Do not treat them as shared project documentation or modify them unless explicitly instructed.
