# BankApp

BankApp is an enterprise-grade, microservice-based digital banking platform engineered for secure financial transactions, real-time threat prevention, and intelligent user assistance. Constructed with Java 25, Spring Boot 3, and React 18, the system is load-balanced across two Microsoft Azure Virtual Machines and rigorously adheres to Twelve-Factor App methodology, Clean Code principles, and Effective Java standards.

---

## System Architecture

```
                                 Client Requests
                                        │
                                        ▼
                           ┌─────────────────────────┐
                           │      Nginx Gateway      │
                           │     (Load Balancer)     │
                           └────────────┬────────────┘
                                        │
             ┌──────────────────────────┴──────────────────────────┐
             ▼                                                     ▼
┌─────────────────────────┐                           ┌─────────────────────────┐
│     Azure Node 01       │                           │     Azure Node 02       │
│  ┌───────────────────┐  │                           │  ┌───────────────────┐  │
│  │ Core Bank Service │  │   Active-Active Cluster   │  │ Core Bank Service │  │
│  └─────────┬─────────┘  │ ◄───────────────────────► │  └─────────┬─────────┘  │
│            │            │                           │            │            │
│  ┌─────────┴─────────┐  │                           │  ┌─────────┴─────────┐  │
│  │ Anomaly Detection │  │                           │  │ Anomaly Detection │  │
│  └─────────┬─────────┘  │                           │  └─────────┬─────────┘  │
│            │            │                           │            │            │
│  ┌─────────┴─────────┐  │                           │  ┌─────────┴─────────┐  │
│  │    MCP Service    │  │                           │  │    MCP Service    │  │
│  └───────────────────┘  │                           │  └───────────────────┘  │
└────────────┬────────────┘                           └────────────┬────────────┘
             │                                                     │
             └──────────────────────────┬──────────────────────────┘
                                        │
                                        ▼
                           ┌─────────────────────────┐
                           │   PostgreSQL Database   │
                           └─────────────────────────┘

```

---

## Key Capabilities & AI Intelligence

* **Stateless Asymmetric Authentication:** Multi-tier authentication utilizing asymmetric RSA (PS256) signature verification. Access tokens are validated statelessly across nodes via shared key pairs.
* **AI-Powered Anomaly Detection:** Real-time analysis inspecting transaction velocities, behavioral baseline deviations, and interaction heuristics to intercept high-risk or unauthorized transfers.
* **Model Context Protocol (MCP) Service:** A dedicated Model Context Protocol backend providing bounded, secure execution interfaces for Large Language Models to assist users with balance inquiries, transaction analysis, and account management.
* **Production Load Balancing:** Active-active deployment distributed across dual Azure VMs managed via Nginx using connections-aware routing strategies for zero-downtime tolerance.

---

## Technology Stack

### Backend Infrastructure

* **Core Framework:** Java 25, Spring Boot 3, Spring Security, Spring Data JPA
* **Database:** PostgreSQL
* **Security & Auth:** OAuth2, JWT (RS256/PS256)

### Frontend Infrastructure

* **Core Framework:** React 18, TypeScript, Vite
* **Styling & State:** Tailwind CSS, Axios

### Infrastructure & Operations

* **Cloud Platform:** Microsoft Azure (Dual Virtual Machines)
* **Reverse Proxy / Load Balancer:** Nginx
* **Containerization:** Docker, Docker Compose

---

## Code Quality & Verification Suite

BankApp enforces strict static analysis, unit, integration, mutation, and end-to-end testing standards within its automated build process.

| Scope | Tool | Purpose |
| --- | --- | --- |
| **Java Code Analysis** | Checkstyle | Enforces strict Java style and layout rules |
| **Static Code Analysis** | PMD | Identifies flawed design patterns, dead code, and performance anti-patterns |
| **Bug Detection** | SpotBugs | Analyzes compiled bytecode to discover security vulnerabilities and logic errors |
| **Code Coverage** | JaCoCo | Measures unit and integration test coverage thresholds |
| **Mutation Testing** | Pitest | Evaluates test suite quality by injecting synthetic fault mutations into bytecodes |
| **Frontend Linting** | ESLint | Ensures Code quality and adherence to JavaScript/TypeScript patterns |
| **Frontend Unit Tests** | Vitest | Executes component-level tests and state transitions |
| **End-to-End Testing** | Playwright | Runs browser automated workflows for full functional validation |

---

## Local Development Setup

### Prerequisites

* JDK 25
* Node.js (v20+) & npm
* Docker Engine & Docker Compose
* OpenSSL

### 1. Key Generation

Generate the RSA keypair required for JWT operations:

```bash
mkdir -p keys
openssl genpkey -algorithm RSA -out keys/private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in keys/private_key.pem -out keys/public_key.pem

```

### 2. Environment Configuration

Create a `.env` file in the project root:

```env
PG_URL=jdbc:postgresql://localhost:5432/bankdb
PG_USERNAME=postgres
PG_PASSWORD=postgres
JWT_PUBLIC_KEY=classpath:keys/public_key.pem
JWT_PRIVATE_KEY=classpath:keys/private_key.pem
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

```

### 3. Build & Run via Docker Compose

```bash
# Build and run the entire environment
docker compose up --build -d

```

---

## Quality Verification Execution

Run full quality verification suites across backend and frontend codebases:

```bash
# Backend Quality Checks (Checkstyle, PMD, SpotBugs, JaCoCo, Pitest)
./mvnw clean verify -P quality-checks

# Frontend Quality Checks (ESLint, Vitest)
cd frontend && npm run test && npm run lint

# End-to-End Browser Testing
npm run test:e2e

```

---

## Architectural Principles & Standards

1. **Twelve-Factor Application Compliance:** Explicit dependency declarations, strict environment variable configuration isolation, stateless process models, and disposable container design.
2. **Clean Code & Effective Java:** Strict adherence to immutability, static factory methods, encapsulation boundaries, resource management, and precise exception handling mechanics.
3. **Defense in Depth:** Zero clear-text credentials stored in version control, read-only volume mounts for security assets, and least-privilege security roles.
