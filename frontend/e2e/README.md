# RedMath Bank — Frontend system tests (Cucumber + Selenium)

End-to-end tests for authentication, route guards, chatbot MCP tools, and RAG policy answers.

## Prerequisites

1. **PostgreSQL** running with the BankApp schema migrated
2. **Backend** (`bank-app`) on `http://localhost:8081`
3. **MCP service** (`redmath-bank-mcp`) on `http://localhost:8082` — required for chat tool scenarios
4. **Frontend** Vite dev server on `http://localhost:5173`
5. **Google Chrome** installed (Selenium Manager downloads the matching ChromeDriver automatically)
6. **Gemini API key** configured for the backend AI chat endpoints

## Setup

```bash
cd frontend
npm install

# IMPORTANT: copy the example file — npm scripts read e2e/.env, NOT .env.example
cp e2e/.env.example e2e/.env
```

Edit **`e2e/.env`** (never commit this file) and set:

- `E2E_ADMIN_EMAIL` / `E2E_ADMIN_PASSWORD` — your working admin login (used only by test helpers to approve new users)
- Leave URLs as-is if frontend is on `:5173` and backend on `:8081`

Do **not** put real passwords in `e2e/.env.example` — that file may be committed to git.

## Run tests

```bash
# All scenarios (auth + chatbot)
npm run test:e2e

# Auth-only smoke suite (skips @ai chat scenarios)
npm run test:e2e:smoke

# Chat / AI scenarios only (slow — needs Gemini + MCP)
npm run test:e2e:chat
```

Run with a visible browser:

```bash
E2E_HEADLESS=false npm run test:e2e:smoke
```

## Scenario coverage

| Feature file | Scenarios |
|---|---|
| `authentication.feature` | Signup → approve → login → dashboard, invalid login, profile/logout, route guards, pending user chat block |
| `chatbot.feature` | `get_account_summary`, `get_recent_transactions`, RAG overdraft policy, combined tool+RAG, chat history persistence |

## Notes

- New signups are `PENDING` until an admin approves them. Tests use the admin REST API to approve users before dashboard/chat flows.
- Chat scenarios are tagged `@ai` and excluded from `test:e2e:smoke`. Run them separately with `npm run test:e2e:chat`.
- Set `E2E_CHAT_TIMEOUT_MS` (wait for AI reply) and `E2E_STEP_TIMEOUT_MS` (Cucumber step limit). Step timeout must be >= chat timeout.
- Chat assertions use flexible text matching because LLM responses are non-deterministic.
- HTML report: `frontend/e2e/reports/cucumber-report.html`
