# Project Context & AI Agent Guidelines

## 1. Project Overview
This is a modern, offline-first Android application written in Kotlin. 
**Current State:** Fully functional offline using local storage (Room only; no DataStore).
**Future State:** Will integrate with a FastAPI backend. The architecture MUST support a seamless transition to client-server without breaking the UI/ViewModel layers.

## 2. Tech Stack & Dependencies
- **Language:** Kotlin 2.2.10 (Strict mode, no Java. Kotlin source only; target JVM 11).
- **UI:** Jetpack Compose (Material 3). Compose BOM 2025.12.00. 
- **Architecture:** MVVM with Clean Architecture principles (Repository Pattern).
- **Asynchronous:** Kotlin Coroutines and Flow (`StateFlow`/`SharedFlow`). NO `LiveData`.
- **Dependency Injection:** **NONE.** Use manual constructor injection. Do NOT use Hilt, Dagger, or Koin.
- **Local Database:** Room 2.7.2 (using KSP for annotation processing).
- **Adaptive UI:** Material 3 Adaptive 1.2.0 (for foldables/tablets).

## 3. Architecture & Folder Structure

The project uses a **layer-based architecture**. When adding new features or files, strictly follow this directory structure. 

*Note: If you are modifying existing files, keep them in their current location. For all NEW files, use the structure below.*

```text
app/src/main/java/com/eromn/microfintracker/
│
├── data/
│   ├── local/         # Room Entities, DAOs, and Local Data Sources
│   ├── remote/        # (Future) FastAPI DTOs, Network Interfaces, Remote Data Sources
│   └── repository/    # Concrete implementations of Repository interfaces
│
├── domain/            # (Crucial for future FastAPI integration)
│   ├── model/         # Pure Kotlin data classes used by the UI (Domain Models)
│   └── repository/    # Repository INTERFACES (ViewModels only interact with these)
│
├── ui/
│   ├── components/    # Small, reusable Compose UI components (e.g., ExpensesItem, Buttons)
│   ├── screens/       # Full-screen Composables (e.g., Dashboard, History). DO NOT put screens in components/
│   └── theme/         # Material3 Theme, Colors, Typography
│
├── utils/             # Helper functions, extensions, and utilities (e.g., DateUtils)
│
├── viewmodel/         # ViewModels (using StateFlow)
│
└── [Activity].kt      # Root Compose host activities (e.g., HistoryActivity.kt)
```

### Rules for File Placement:
1. **Screens vs Components:** If a file is a full page/route, it goes in `ui/screens/`. If it is a reusable piece of UI (like a list item or a custom button), it goes in `ui/components/`.
2. **Models:** If a Room Entity is exactly the same as the UI model, it can stay in `data/local/`. However, if the UI needs different data than the database (or future API), create a pure Kotlin class in `domain/model/` and map the Entity to it in the Repository.
3. **Repositories:** The interface goes in `domain/repository/`. The implementation goes in `data/repository/`.
4. **Future FastAPI Code:** All Retrofit/Ktor interfaces, DTOs (Data Transfer Objects), and remote data source implementations MUST go in `data/remote/`.

## Known Exceptions (technical debt)
These files currently **deviate from the conventions above**. They work, so do NOT move them during routine work; instead plan to reconcile them in a dedicated iteration.

1. **`app/src/main/java/com/eromn/microfintracker/data/TransactionRepository.kt`** — a concrete `class` in `data/` with no interface. Every other repository follows the pattern (interface in `domain/repository/`, impl in `data/repository/`). **Recommendation:** extract a `TransactionRepository` interface into `domain/repository/`, rename the concrete one to `TransactionRepositoryImpl` in `data/repository/`, and inject the interface into the ViewModel.

2. **`Transaction.kt` (in `data/`, not `data/local/`)** — acts as both the Room Entity and the UI/domain model, and it currently lacks a `domain/model/` counterpart. **Recommendation:** once FastAPI lands, introduce a pure Kotlin `Transaction` domain model in `domain/model/`, map it to/from the Room entity in the repository, and keep API DTOs in `data/remote/`.

3. **`ui/components/HistoryActivity2.kt`** — a full screen placed in `ui/components/` instead of `ui/screens/`. **Recommendation:** move it to `ui/screens/` when next touched.
