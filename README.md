# MyQuizApp – Multi‑User Android Client for LLM‑Powered Quiz Platform

**Author:** Daniel Allen  
 
**License:** MIT

---

## Overview

**MyQuizApp** is a multi‑user Android application that connects to an intelligent quiz backend powered by the **Hugging Face Router API** using **Gemma 3‑27B**.  
The app communicates through a **Flask REST API** hosted server that handles prompt generation and evaluation, enabling learners to engage with adaptive, AI‑generated quiz questions.

The purpose of this project is to demonstrate full‑stack integration between a mobile front‑end and an LLM‑based backend, showcasing end‑to‑end knowledge of Android development, HTTP networking, and AI model orchestration.

---

## Features

- User registration and profile management
- Topic‑based quiz selection and adaptive question generation
- Quiz history and results tracking via Room database
- Server‑side LLM integration (Flask + Gemma 3‑27B)
- Clean XML‑based UI and modular activity design
- Navigation and data binding across multiple activities
- Integration‑ready with future subscription or leaderboard systems

---

## Tech Stack

| Layer | Technology |
|-------|-------------|
| Language | Java (Android) |
| Framework | Android SDK + AndroidX |
| UI | XML layouts with Data Binding |
| Networking | Retrofit 2 + OkHttp |
| Local Storage | Room Database (DAO pattern) |
| Dependency Management | Gradle 7.x |
| Backend Interface | Flask API at `http://10.0.2.2:5000/` (default Android emulator bridge) |
| LLM Provider | Hugging Face Router (OpenAI‑compatible API) – Gemma 3‑27B model |
| Version Control | Git + GitHub |

---

## API Endpoints (Backend Integration)

| Method | Endpoint | Description |
|--------|-----------|-------------|
| `GET` | `/getQuiz?topic=<topic>` | Retrieves a dynamically generated question from the LLM backend. |
| `GET` | `/test` | Health check endpoint for verifying connectivity. |

The backend repository (Flask server) handles all calls to Hugging Face, manages the API token securely, and returns processed responses to this Android client.

---

## Key Learnings and Showcase Areas

- Implementation of Retrofit service interfaces and network clients.  
- Integration of Flask endpoints with Android emulator using `10.0.2.2`.  
- Clean separation between UI, network, and persistence layers.  
- Use of Room DAO classes for session, task, and quiz data.  
- Secure handling of environment variables via `.env` files and `.gitignore` rules.  
- Understanding of Hugging Face LLM API structure and JSON payloads.  
- Full development lifecycle: Gradle config → Android client → Flask backend → LLM response.  

---

## Folder Structure (Simplified)

```
MyQuizApp/
 ├── app/
 │   ├── src/
 │   │   ├── main/java/com/example/quizapp/
 │   │   │   ├── activities/        # Home, Login, Quiz, Results, etc.
 │   │   │   ├── adapter/           # RecyclerView adapters
 │   │   │   ├── data/              # Room database entities + DAO
 │   │   │   ├── model/             # POJOs for QuizItem, User, etc.
 │   │   │   └── network/           # Retrofit client and API service
 │   │   └── res/layout/            # XML UI layouts
 │   ├── build.gradle
 ├── gradle.properties
 └── settings.gradle
```

---

## Security and Environment Practices

- `.env` and `google‑services.json` excluded from Git via `.gitignore`.  
- No hardcoded API keys or Hugging Face tokens.  
- Environment variables managed on backend only.  
- Local Room database stores user progress only – no sensitive information.  

---

## License

MIT License © 2025 Daniel Allen

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
