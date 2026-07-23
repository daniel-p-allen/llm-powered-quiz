# Quiz client — Android

The Android half of the LLM-powered quiz. It fetches generated questions from the
Flask server in [`../server`](../server), stores them locally, and tracks results
per user.

For the whole system, the architecture and how to run it, see the
[root README](../README.md).

---

## What it does

- Register and sign in (multiple users on one device)
- Choose topics of interest; a quiz is generated for each
- Answer questions, with the correct answer shown immediately after submitting
- Review every past question and result in History
- Browse subscription tiers with a payment method sheet

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java |
| Build | Gradle 8.11.1, Android Gradle Plugin 8.9.1, version catalog (`gradle/libs.versions.toml`) |
| SDK | compileSdk 35, targetSdk 35, **minSdk 21** |
| UI | XML layouts with view binding |
| Networking | Retrofit 2 + OkHttp (30s connect/write, 60s read) |
| Local storage | Room — `AppDatabase` with `UserDao`, `TaskDao`, `QuizSessionDao` |
| Preferences | SharedPreferences via `UserManager` |
| Backend | Flask API at `http://10.0.2.2:5000/` |

`10.0.2.2` is the Android emulator's alias for the host machine's `localhost`, so
the server runs on your computer and the app reaches it with no configuration. On
a physical device this must be changed to the host's LAN address in
`network/ApiClient.java`.

---

## Layout

```
client/
└── app/src/main/
    ├── java/com/example/quizapp/
    │   ├── *Activity.java      Login, Signup, Home, Interests, Quiz,
    │   │                       Results, History, Tasks, Profile, Upgrade
    │   ├── QuizSession.java    a completed attempt
    │   ├── UserManager.java    accounts and current user, over SharedPreferences
    │   ├── adapter/            RecyclerView adapters (quiz, results, history, tasks, topics)
    │   ├── data/               Room database and DAOs
    │   ├── model/              QuizItem, QuizResponse, Task, TaskEntity, User
    │   └── network/            ApiClient (Retrofit) and QuizService
    ├── assets/topics.json      the 20 selectable topics
    └── res/layout/             XML layouts
```

The activity classes sit at the package root. An earlier version of this document
described an `activities/` package, which has never existed.

---

## Building

```bash
cd client
./gradlew assembleDebug
```

Needs JDK 17 or newer. The APK lands in
`app/build/outputs/apk/debug/app-debug.apk`.

To install on a running emulator:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

`make apk` from the repository root does the same build.

---

## Known gaps

Stated plainly, because they are visible to anyone who opens the source:

- **Login does not check the password.** `LoginActivity` reads the password field
  and never uses it; sign-in succeeds if the username exists in the saved list.
  There is a `// TODO: replace with real authentication` on the line. Accounts are
  local to the device and no data leaves it, so nothing is exposed — but this is
  not authentication.
- **No automated tests.** `ExampleUnitTest.java` is the Android Studio template.
  The server carries the test suite; the client is verified by building and
  running it. Adding instrumented tests is the obvious next step — the emulator
  setup already works.
- **Quiz sessions recorded before the answer-index fix keep the wrong answer.**
  Old rows in Room have `correct_index` of 0 regardless of the real answer. There
  is no migration; clearing app data resets them.
- **`incorrectAnswersJson` stores every answered question, not only the wrong
  ones.** Harmless — `HistoryActivity` re-derives correctness when rendering — but
  the column name is misleading.
- **Only the small arrow button starts a quiz** on the Tasks screen. The row and
  its title are not tappable, which is easy to miss.
- **The upgrade and payment screens are UI only.** No payment is processed.
