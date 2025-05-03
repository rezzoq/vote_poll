# vote-poll

> Anonymous token-based voting system. The admin creates a poll and distributes one-time UUID tokens to participants. Each token can be used exactly once — no accounts, no tracking.

---

## How It Works

```
Admin (authenticated)          Participant (anonymous)
        │                               │
POST /createPoll                        │
  question + userCount                  │
        │                               │
        ▼                               │
  N unique UUID tokens generated        │
  stored in DB, linked to poll          │
        │                               │
  Admin distributes tokens ─────────────▶  GET /vote/{token}
  (link, QR, chat, etc.)                       │
                                               │ token valid?
                                               │    yes → show poll
                                               ▼
                                    POST /submitVote
                                      token + voteYes
                                               │
                                      token marked used
                                      yes/no count updated
                                               │
                                               ▼
                                        GET /results
                                    (public, no auth needed)
```

**Key properties:**
- One vote per token — the token is marked `used=true` after voting, double-voting is impossible
- Votes are anonymous — the `Vote` entity stores only `voteYes` + `token`, no user identity
- Admin area protected by Spring Security (ROLE_ADMIN), participants need no account

---

## Tech Stack

| Layer | Technologies |
|---|---|
| Backend | Java 17, Spring Boot 3, Maven |
| Security | Spring Security 6, BCrypt, role-based access |
| Persistence | Spring Data JPA, PostgreSQL (prod) / H2 (dev) |
| Templates | Thymeleaf |
| Tests | JUnit 5, Mockito, Spring Security Test |
| Containerisation | Docker, Docker Compose |

---

## Quick Start

### With Docker

```bash
git clone https://github.com/rezzoq/vote-poll.git
cd vote-poll
docker compose up --build
```

App starts at `http://localhost:8080`

Default admin credentials (override via env):
- username: `admin`
- password: `admin123`

### Local (H2 in-memory, no Docker needed)

```bash
mvn spring-boot:run
```

H2 console available at `http://localhost:8080/h2-console`

---

## API / Pages

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/` | ADMIN | Dashboard — create poll, see tokens |
| `POST` | `/createPoll` | ADMIN | Create a new poll, generate tokens |
| `GET` | `/vote/{token}` | None | Voting page for a token holder |
| `POST` | `/submitVote` | None | Submit a vote |
| `GET` | `/results` | None | Live results |

---

## Configuration

All sensitive values are read from environment variables:

| Variable | Default | Description |
|---|---|---|
| `ADMIN_USERNAME` | `admin` | Admin login |
| `ADMIN_PASSWORD` | `admin123` | Admin password |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `votepolldb` | Database name |
| `DB_USER` | `postgres` | DB username |
| `DB_PASSWORD` | `postgres` | DB password |

Copy `.env.example` to `.env` and fill in production values before deploying.

---

## Project Structure

```
vote-poll/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── pom.xml
└── src/
    ├── main/java/vote/
    │   ├── VotingApplication.java
    │   ├── controller/
    │   │   └── VotingController.java
    │   ├── model/
    │   │   ├── Poll.java          # question, yes/no counts, active flag
    │   │   ├── UserToken.java     # UUID token, used flag, poll reference
    │   │   └── Vote.java          # vote record (anonymous)
    │   ├── repository/
    │   │   ├── PollRepository.java
    │   │   ├── UserTokenRepository.java
    │   │   └── VoteRepository.java
    │   ├── security/
    │   │   └── SecurityConfig.java   # Spring Security, BCrypt, ADMIN role
    │   └── service/
    │       └── VotingService.java    # business logic
    ├── main/resources/
    │   ├── application.properties          # dev (H2)
    │   ├── application-prod.properties     # prod (PostgreSQL)
    │   └── templates/
    │       ├── index.html    # admin dashboard
    │       ├── poll.html     # voting page
    │       └── results.html  # live results
    └── test/java/vote/service/
        └── VotingServiceTest.java    # 8 unit tests with Mockito
```

---

## Key Design Decisions

**Why UUID tokens instead of user accounts?**
Eliminates the need for registration while still enforcing one-vote-per-person. The organiser controls access by distributing tokens through whatever channel they prefer (email, QR code, Telegram). No personal data is collected.

**Why `token.used = true` instead of deleting the token?**
Keeps an audit trail — the admin can see how many tokens were distributed vs how many were actually used, without knowing who voted for what.

**Why H2 for dev and PostgreSQL for prod?**
H2 in-memory requires zero setup for local development and tests. Spring profiles switch the datasource transparently — the same JPA entities and repositories work on both.
