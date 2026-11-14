# Improvement Backlog

Record of findings from a code and stack review. Its purpose is to define what the follow-up tasks should do. **Nothing in this document has been implemented yet.**

Constraints from `AGENTS.md` still apply to every task below (no DI framework, Room only, ViewModels depend on `domain/repository` interfaces, backtick the `` `transaction` `` table, Spanish strings in `strings.xml`, never renumber `Category.serverId`). Items in `AGENTS.md` section 6 (known debt) are only to be touched when a task here says so explicitly.

Suggested status values per item: `TODO`, `IN PROGRESS`, `DONE`, `DROPPED`.

---

## 1. Stack recommendations

The current stack (Kotlin, Compose + Material 3, MVVM, Coroutines/Flow, Room, Retrofit/OkHttp/kotlinx-serialization) is sound. These are the changes a from-scratch project would make. All are optional and several conflict with `AGENTS.md`, so they need an explicit decision first.

| ID | Area | Recommendation | Current | Priority |
|---|---|---|---|---|
| S1 | Background work | Use **WorkManager** for uploads/sync (network constraint, retry with backoff, survives process death). | Upload only runs when triggered from the foreground. | High |
| S2 | Preferences/config | Add **DataStore** for settings, auth tokens, last-sync state, currency and timezone. | Nothing. Constants are hardcoded. | High (needed for auth) |
| S3 | Navigation | **Navigation Compose** with a single `MainActivity`. | One Activity per screen (`HistoryActivity`). | Medium |
| S4 | DI | **Hilt** (or Koin). | Manual factories. Frameworks are banned by `AGENTS.md`. | Low, needs a decision to lift the ban |
| S5 | Modularization | Split into `core:data`, `core:domain`, `feature:*` plus convention plugins once there are about 5 or more features. | Single `:app` module. | Low |
| S6 | Sync model | Explicit `syncStatus`, client-generated UUID plus server id, `updatedAt`, and tombstones for deletes. | `isRead` doubles as "pending upload". | High before server-to-client sync |
| S7 | Testing and quality | Add Turbine and MockK, Compose UI tests, ktlint or detekt, and GitHub Actions CI. | MockWebServer plus fake DAO tests only. | Medium |

Notes
- S1 and S6 should be designed together.
- S2 is a prerequisite for removing the hardcoded user, account, currency and timezone in `TransactionUploadRepositoryImpl`.
- If S4 is adopted, update `AGENTS.md` sections 1 and 2 in the same change.

---

## 2. Correctness and data integrity

| ID | Finding | Location | Suggested task | Priority |
|---|---|---|---|---|
| C1 | Money is stored as `Double`, which gives floating-point drift. Existing `// TODO: Change to BigDecimal`. | `data/Transaction.kt` | Store integer minor units (centavos) as `Long`. Requires a Room version bump, a migration that converts existing values, a new schema JSON, and DTO and UI formatting changes. Confirm the server contract first. | High |
| C2 | A retry can duplicate uploads. The POST happens, then the row is marked read, and a crash or DB failure between the two re-sends it. | `data/repository/TransactionUploadRepositoryImpl.kt` | Add a client-generated UUID or idempotency key sent in the DTO so the server can dedupe. Needs a server change plus an entity and migration change. | High |
| C3 | Undo-delete reinserts with `id = 0`, so the row gets a new id and a new list position. | `HistoryViewModel.restoreTransaction` | Reinsert with the original id (use `OnConflictStrategy.REPLACE` or a dedicated DAO method), and add a test. | Medium |
| C4 | Upload errors are swallowed. The loop only counts failures. | `TransactionUploadRepositoryImpl.uploadPendingTransactions` | Log the exception. Distinguish permanent errors (4xx) from retryable ones (network, 5xx) and surface that in `UploadResult`. | Medium |
| C5 | The upload sends a hardcoded `Asia/Taipei` timezone while the UI formats dates in the device zone. | `TransactionUploadRepositoryImpl`, `DateUtils` | Resolve with S2, or at minimum send the device zone id. This is known debt (`AGENTS.md` 6.4), so touch it only deliberately. | Medium |
| C6 | Uploads are sequential, one request per row. | `TransactionUploadRepositoryImpl` | Add a batch endpoint, or send with bounded concurrency. Needs a server decision. | Low |

---

## 3. ViewModel and state management

| ID | Finding | Location | Suggested task |
|---|---|---|---|
| V1 | `HistoryViewModel` exposes about 11 separate `StateFlow`s, and `HistoryActivity` collects each by hand. States can be inconsistent between emissions. | `viewmodel/HistoryViewModel.kt`, `HistoryActivity.kt` | Introduce one `HistoryUiState` data class (editing, category picker, upload and delete state) exposed as a single `StateFlow`. |
| V2 | One-shot events (`saveFailed`, `uploadResult`, `deleteReadResult`) are modelled as state and cleared manually. They can replay after rotation. | same | Move them to a `Channel` or `SharedFlow` of events. Remove the `clear*` functions. |
| V3 | `launch(Dispatchers.IO)` is hardcoded everywhere, and Room suspend functions already switch threads. | `HistoryViewModel` | Drop it where unneeded. Where needed, inject a `CoroutineDispatcher` through the factory so tests can control it. |
| V4 | Possibly unused members: `logTransaction`, `markAsRead`, `markAsUnread`, `allTransactions` (public), `TransactionRepository.deleteById`, `TransactionDao.deleteById`. | `HistoryViewModel`, `data/` | Verify with a search that nothing uses them, then remove. Keep the DAO method if tests use it. |
| V5 | `startAdding`, `startEditing` and `clearEditing` repeat the same reset block. | `HistoryViewModel` | Extract one private reset function. |
| V6 | Hardcoded English toast `"Transaction restored!"`. The UI is Spanish. | `HistoryActivity.kt` | Move it to `res/values/strings.xml` (Spanish). |
| V7 | Dashboard stats are hardcoded placeholders (`username`, `monthlySpent`, `todaySpent`). | `HistoryActivity.kt` | Known debt (`AGENTS.md` 6.3). Only do this if explicitly requested. It should compute spending from Room through a repository. |

---

## 4. Performance

| ID | Finding | Location | Suggested task |
|---|---|---|---|
| P1 | `groupedTransactions` regroups the whole table on every emission. | `HistoryViewModel` | Short term: see P2. If the table is expected to grow into the thousands, use Paging 3 or a limited window. |
| P2 | `groupedTransactions` is a cold `Flow`, so it re-queries after configuration changes. | `HistoryViewModel` | Expose it with `stateIn(viewModelScope, WhileSubscribed(5_000), emptyMap())`, as the category flows already do. |
| P3 | `DateTimeFormatter` instances are built on every call. | `utils/DateUtils.kt` | Hoist them into constants. They are immutable and thread-safe. |
| P4 | No Room indexes. `getAll` sorts by `timestamp DESC, id DESC` and `getUnread` filters on `is_read`. | `data/Transaction.kt`, `TransactionDao.kt` | Add indexes on `timestamp` and `is_read`. This is an entity change, so bump the DB version, add a migration, and commit the schema JSON. |
| P5 | `Category.fromId` and the category list may do repeated linear work. | `data/Transaction.kt` | Verify. If so, cache an id-to-category map. Never renumber `serverId`. |
| P6 | `Transaction` may be treated as unstable by Compose, causing extra recompositions. | `ui/` | Check the Compose compiler reports. Add `@Immutable`, or map to a UI model in `domain/model/`. |

---

## 5. Build and housekeeping

| ID | Finding | Suggested task |
|---|---|---|
| B1 | Release builds may not use R8 or resource shrinking. | Enable minification and shrinking for release, with keep rules for the kotlinx-serialization DTOs. Verify with a release build. |
| B2 | OkHttp timeouts and retry policy are defaults. | Set explicit connect, read and write timeouts in `TransactionApi.defaultClient()`. |
| B3 | `AppDatabase` still has misleading template comments ("Migration is not covered in this basic example"). | Clean up the comments only. |
| B4 | There are no ViewModel tests or Compose UI tests, though the ViewModel holds the most logic. | Add `HistoryViewModel` tests (fake repositories plus a test dispatcher). They depend on V3. |

---

## 6. Suggested order of work

1. **Quick, low-risk:** P2, P3, V5, V6, B3.
2. **State refactor:** V1 and V2, then V3 and B4.
3. **Data integrity (needs server confirmation):** C2, then C1, then C3 and C4.
4. **Schema performance:** P4, together with any other schema change (C1, C2) so there is only one migration.
5. **Architecture:** S1 with S6, then S2 (which also resolves C5), then S3.
6. **Optional or later:** S4, S5, C6, P1.

## 7. Rules for every task derived from this document

- Change one numbered group at a time and keep commits focused.
- Any entity change needs: a `version` bump in `AppDatabase.kt`, an `AutoMigration` or `Migration`, and the new schema JSON under `app/schemas/com.eromn.microfintracker.data.AppDatabase/`. Batch schema changes (C1, C2, P4) into as few versions as possible.
- Run `./gradlew :app:testDebugUnitTest` and `./gradlew :app:assembleDebug` before finishing.
- Add or update unit tests for any behavior change. Keep UI strings in Spanish in `strings.xml`.
- Update `AGENTS.md` when a task changes its stated conventions (for example S4) or removes an entry from its known-debt list.
