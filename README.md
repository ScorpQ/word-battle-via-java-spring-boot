# WordBattle 🎮

Real-time multiplayer English word guessing game. Players compete to guess English words as fast as possible — the faster you answer, the more points you earn.

---

## Features

- Real-time multiplayer gameplay (up to 4 players per room)
- WebSocket + STOMP for instant communication
- RestAPI support for creating rooms and players account as guest
- Room system with unique room codes
- 10 rounds per game
- Speed-based scoring — faster answers earn more points
- Guest player support
- Word hints powered by Spring AI

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Real-time | WebSocket + STOMP |
| Cache | Redis |
| Database | PostgreSQL |
| ORM | Spring Data JPA |
| AI | Spring AI (word hints) |
| Containerization | Docker Compose |

---

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 21
- Maven

### Setup

1. Clone the repository:
```bash
git clone https://github.com/ScorpQ/word-battle-via-java-spring-boot.git
cd word-battle-via-java-spring-boot
```

2. Create your config file:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

3. Start PostgreSQL and Redis:
```bash
docker-compose up -d
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

5. Open Swagger UI:
```
http://localhost:8081/swagger-ui/index.html
```
---

## Project Structure

```
src/main/java/com/questie/product/
├── config/         # WebSocket config
├── controller/     # REST + WebSocket controllers
├── dto/            # Response DTOs
├── entity/         # JPA entities
├── repository/     # Spring Data repositories
└── service/        # Business logic
```

---

## Roadmap

- [x] Project setup
- [x] Entity design
- [x] Room system (create / join)
- [x] WebSocket + STOMP configuration
- [ ] Game state machine
- [ ] Redis integration
- [ ] Speed-based scoring
- [ ] Spring AI word hints
- [ ] React frontend
- [ ] Docker Compose full setup
- [ ] Deploy to Railway/Render

---
