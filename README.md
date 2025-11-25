# A2A Monorepo - Agent-to-Agent Communication Suite

A monorepo containing two complementary applications that demonstrate the **A2A (Agent-to-Agent) protocol** for AI agent communication. Built with **Vaadin**, **Spring Boot**, and **Spring AI**.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)
![Vaadin](https://img.shields.io/badge/Vaadin-24.9-blue)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0-purple)

---

## 📦 Repository Structure

```
a2aserver/
├── a2aserver/          # A2A Server - Form Assistant Agent
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── a2aclient/          # A2A Client - Personal AI Assistant  
│   ├── src/
│   ├── pom.xml
│   └── README.md
└── README.md           # This file
```

---

## 🚀 Projects Overview

| Project | Description | Port | Role |
|---------|-------------|------|------|
| **a2aserver** | Form Assistant Agent that processes contact forms, employee registrations, and support tickets | `8080` | A2A Server (exposes `/.well-known/agent.json`) |
| **a2aclient** | Personal AI Assistant with knowledge base that can interact with A2A agents | `8081` | A2A Client (consumes agent APIs) |

### How They Work Together

```
┌─────────────────────────────────────────────────────────────────┐
│                      a2aclient (Port 8081)                       │
│              Personal AI Assistant + Knowledge Base              │
│                                                                  │
│  "Submit contact form for Ante Antić on @local agent"           │
│                              │                                   │
│                              ▼                                   │
│                    [Confirmation Dialog]                         │
│                    "Review data before sending"                  │
│                              │                                   │
│                         [Confirm]                                │
└──────────────────────────────┼───────────────────────────────────┘
                               │ JSON-RPC 2.0
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      a2aserver (Port 8080)                       │
│                    Form Assistant Agent                          │
│                                                                  │
│  /.well-known/agent.json  ←  Agent Discovery                    │
│  /a2a                     ←  Task Execution (submit-contact)    │
│                                                                  │
│                    [Process & Store Form]                        │
│                              │                                   │
│                              ▼                                   │
│                    ✅ "Contact form submitted"                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## ⚡ Quick Start

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+** (or use included `mvnw` wrapper)
- **OpenAI API Key**

### 1. Clone the Repository

```bash
git clone <repository-url>
cd a2aserver
```

### 2. Set OpenAI API Key

**Windows PowerShell:**
```powershell
$env:OPENAI_API_KEY="sk-your-api-key-here"
```

**Windows CMD:**
```cmd
set OPENAI_API_KEY=sk-your-api-key-here
```

**Linux/macOS:**
```bash
export OPENAI_API_KEY=sk-your-api-key-here
```

### 3. Build & Run

#### Option A: Development Mode (Hot Reload)

**Terminal 1 - Start a2aserver:**
```bash
cd a2aserver
mvnw spring-boot:run
```

**Terminal 2 - Start a2aclient:**
```bash
cd a2aclient
mvnw spring-boot:run
```

#### Option B: Production Build

**Build a2aserver:**
```bash
cd a2aserver
mvnw clean install -Pproduction
java -DOPENAI_API_KEY=YOUR_OPENAI_KEY -jar target\a2aserver-1.0-SNAPSHOT.jar
```

**Build a2aclient:**
```bash
cd a2aclient
mvnw clean install -Pproduction
java -DOPENAI_API_KEY=YOUR_OPENAI_KEY -jar target\a2aclient-1.0-SNAPSHOT.jar
```

### 4. Access the Applications

| Application | URL | Description |
|-------------|-----|-------------|
| **a2aserver** | http://localhost:8080 | Form Assistant with AI chat |
| **a2aclient** | http://localhost:8081 | Personal AI Assistant |
| **Agent Card** | http://localhost:8080/.well-known/agent.json | A2A capability discovery |

---

## 🎯 Usage Examples

### Using a2aclient to Submit Forms via a2aserver

1. **Start both applications** (a2aserver on 8080, a2aclient on 8081)

2. **Open a2aclient** at http://localhost:8081

3. **Try these prompts:**

   ```
   "Show me my contacts"
   → Lists contacts from your knowledge base
   
   "What agents are available?"
   → Shows bookmarked A2A agents (default: Local Form Assistant @local)
   
   "Submit contact form for Ante Antić on @local agent"
   → Shows confirmation dialog with data, then submits to a2aserver
   
   "Create a support ticket about login issues with HIGH priority"
   → Creates a support ticket via a2aserver
   ```

4. **Verify submission** at http://localhost:8080/submissions

### Using a2aserver Directly

1. **Open a2aserver** at http://localhost:8080

2. **Use the AI Assistant:**
   ```
   "Take me to the contact form"
   "Register John Smith as a developer in Engineering with salary $80,000"
   "Create a support ticket about password reset issues"
   ```

3. **Use the floating chat widget** (bottom-right corner) on any page

---

## 📁 Project Details

### a2aserver - Form Assistant Agent

An A2A-compliant server that provides:

- **AI Chat Interface** - Natural language form filling
- **Forms**: Contact, Employee Registration, Support Tickets
- **A2A Protocol Endpoints**:
  - `GET /.well-known/agent.json` - Agent capabilities
  - `POST /a2a` - JSON-RPC task execution
- **Skills**: `submit-contact`, `submit-employee`, `submit-support-ticket`, `ask-assistant`

📖 [Full documentation](a2aserver/README.md)

### a2aclient - Personal AI Assistant

A personal assistant that:

- **Knowledge Base** - Stores your contacts, projects, notes
- **Agent Bookmarks** - Manage connections to A2A servers
- **Approval Workflow** - Review data before sending to agents
- **A2A Client** - Communicates with agent servers

📖 [Full documentation](a2aclient/README.md)

---

## 🔧 Configuration

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `OPENAI_API_KEY` | ✅ | Your OpenAI API key |
| `PORT` | ❌ | Override default port |

### Default Ports

| Application | Default Port | Override |
|-------------|--------------|----------|
| a2aserver | 8080 | `java -DPORT=9080 -jar ...` |
| a2aclient | 8081 | `java -DPORT=9081 -jar ...` |

---

## 🐳 Docker

### Build Images

```bash
# a2aserver
cd a2aserver
docker build -t a2a-server:latest .

# a2aclient  
cd a2aclient
docker build -t a2a-client:latest .
```

### Run Containers

```bash
# Start a2aserver
docker run -d \
  -p 8080:8080 \
  -e OPENAI_API_KEY=sk-your-key-here \
  --name a2a-server \
  a2a-server:latest

# Start a2aclient
docker run -d \
  -p 8081:8081 \
  -e OPENAI_API_KEY=sk-your-key-here \
  --name a2a-client \
  a2a-client:latest
```

### Docker Compose

```yaml
version: '3.8'
services:
  a2aserver:
    build: ./a2aserver
    ports:
      - "8080:8080"
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    
  a2aclient:
    build: ./a2aclient
    ports:
      - "8081:8081"
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    depends_on:
      - a2aserver
```

---

## 🧪 Testing A2A Protocol

### Using cURL

```bash
# Discover agent capabilities
curl http://localhost:8080/.well-known/agent.json | jq

# Submit a contact form
curl -X POST http://localhost:8080/a2a \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tasks/send",
    "id": "test-1",
    "params": {
      "skill": "submit-contact",
      "input": {
        "firstName": "Test",
        "lastName": "User",
        "email": "test@example.com",
        "message": "Hello from cURL!"
      }
    }
  }' | jq
```

### Using Python

```python
import requests

response = requests.post(
    "http://localhost:8080/a2a",
    json={
        "jsonrpc": "2.0",
        "method": "tasks/send",
        "id": "py-001",
        "params": {
            "skill": "submit-contact",
            "input": {
                "firstName": "Python",
                "lastName": "Agent",
                "email": "python@agent.ai"
            }
        }
    }
)
print(response.json())
```

---

## 📋 Technology Stack

| Component | Technology |
|-----------|------------|
| UI Framework | Vaadin Flow 24.9 |
| Backend | Spring Boot 3.5 |
| AI Integration | Spring AI 1.0 + OpenAI |
| Database | H2 (in-memory) |
| HTTP Client | Spring WebFlux |
| Build | Maven |
| Java | OpenJDK 21 |

---

## 📄 License

See [LICENSE.md](LICENSE.md) for details.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes to either `a2aserver/` or `a2aclient/`
4. Submit a pull request

---

## 📞 Support

For issues and feature requests, please use the GitHub issue tracker.

