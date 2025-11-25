# A2A Form Assistant Server

An AI-powered virtual assistant application built with **Vaadin**, **Spring Boot**, and **Spring AI** that enables intelligent form navigation, population, and submission. The application exposes an **A2A (Agent-to-Agent) protocol** server, allowing other AI agents to interact with forms programmatically.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)
![Vaadin](https://img.shields.io/badge/Vaadin-24.9-blue)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0-purple)

---

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture Overview](#architecture-overview)
- [Getting Started](#getting-started)
- [Application Screens](#application-screens)
- [Available Forms](#available-forms)
- [A2A Protocol](#a2a-protocol)
  - [Agent Discovery](#agent-discovery)
  - [Task Execution](#task-execution)
  - [Available Skills](#available-skills)
  - [Request/Response Examples](#requestresponse-examples)
- [API Reference](#api-reference)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Building & Deployment](#building--deployment)

---

## Features

### 🤖 AI Virtual Assistant
- Natural language interface powered by OpenAI GPT models
- Context-aware conversation with chat history
- Understands user intent and extracts form data from natural language
- Available on every page via floating chat widget

### 📝 Smart Form Management
- Multiple form types: Contact, Employee Registration, Support Tickets
- AI-assisted form population based on conversation
- Automatic form submission capability
- URL-based form pre-population via query parameters

### 🔌 A2A Protocol Server
- Full A2A (Agent-to-Agent) protocol implementation
- JSON-RPC 2.0 based communication
- Agent Card for capability discovery
- Task-based form submission for external agents

### 📊 Submissions Dashboard
- Unified view of all form submissions
- Advanced filtering by form type, date range, and search text
- Statistics cards showing submission counts
- Detailed view dialog for each submission

---

## Technology Stack

| Layer | Technology | Version | Purpose |
|-------|------------|---------|---------|
| **Frontend** | Vaadin Flow | 24.9.5 | Server-side Java UI framework |
| **Backend** | Spring Boot | 3.5.7 | Application framework |
| **AI Integration** | Spring AI | 1.0.0-M6 | LLM integration framework |
| **LLM Provider** | OpenAI | GPT-4o-mini | Language model for assistant |
| **Database** | H2 | Runtime | In-memory database (dev) |
| **ORM** | Spring Data JPA | 3.5.x | Data persistence |
| **Build Tool** | Maven | 3.9+ | Dependency management |
| **Java** | OpenJDK | 21 | Runtime environment |

### Key Dependencies

```xml
<!-- Vaadin -->
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-spring-boot-starter</artifactId>
</dependency>

<!-- Spring AI with OpenAI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        A2A Client (External Agent)               │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ JSON-RPC 2.0
┌─────────────────────────────────────────────────────────────────┐
│                         A2A Protocol Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  Agent Card     │  │  A2A Controller │  │  A2A Service    │  │
│  │  /.well-known/  │  │  /a2a           │  │  Task Executor  │  │
│  │  agent.json     │  │                 │  │                 │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      AI Assistant Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ AssistantService│  │  Chat History   │  │  OpenAI Client  │  │
│  │ Command Parser  │  │  Context Mgmt   │  │  GPT-4o-mini    │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Business Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │ ContactService  │  │ EmployeeService │  │ SupportService  │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Data Layer (JPA)                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │    Contact      │  │    Employee     │  │  SupportTicket  │  │
│  │    Entity       │  │    Entity       │  │    Entity       │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                     H2 In-Memory Database                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9+ (or use included wrapper)
- OpenAI API key

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd a2aserver
   ```

2. **Set OpenAI API Key**
   
   Windows PowerShell:
   ```powershell
   $env:OPENAI_API_KEY="sk-your-api-key-here"
   ```
   
   Windows CMD:
   ```cmd
   set OPENAI_API_KEY=sk-your-api-key-here
   ```
   
   Linux/macOS:
   ```bash
   export OPENAI_API_KEY=sk-your-api-key-here
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Or on Windows:
   ```cmd
   mvnw.cmd spring-boot:run
   ```

4. **Access the application**
   
   Open your browser at: http://localhost:8080

---

## Application Screens

### 1. AI Assistant (Home Page)
**Route:** `/`

The main chat interface for interacting with the AI assistant.

**Features:**
- Full-page chat interface
- Natural language understanding
- Conversation history with context
- Quick navigation to any form
- Form data extraction from conversation

**Example interactions:**
- "Take me to the contact form"
- "I want to register John Smith as a developer in Engineering with salary $80,000"
- "Create a support ticket about login issues, my name is Jane and email is jane@test.com"

---

### 2. Contact Form
**Route:** `/contact`

A form for submitting contact inquiries.

**Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| First Name | Text | ✅ | Contact's first name |
| Last Name | Text | ✅ | Contact's last name |
| Email | Email | ✅ | Email address |
| Phone | Text | ❌ | Phone number |
| Company | Text | ❌ | Company name |
| Message | TextArea | ❌ | Message content (max 2000 chars) |

---

### 3. Employee Registration Form
**Route:** `/employee`

A form for registering new employees in the system.

**Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| First Name | Text | ✅ | Employee's first name |
| Last Name | Text | ✅ | Employee's last name |
| Email | Email | ✅ | Work email address |
| Department | Select | ❌ | Engineering, Sales, Marketing, HR, Finance, Operations |
| Position | Text | ❌ | Job title |
| Hire Date | Date | ❌ | Start date (YYYY-MM-DD) |
| Salary | Number | ❌ | Annual salary |

---

### 4. Support Ticket Form
**Route:** `/support`

A form for creating support tickets and bug reports.

**Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| Subject | Text | ✅ | Ticket subject/title |
| Description | TextArea | ✅ | Detailed description (max 4000 chars) |
| Reporter Name | Text | ✅ | Your name |
| Reporter Email | Email | ✅ | Your email address |
| Priority | Select | ❌ | LOW, MEDIUM, HIGH, CRITICAL |
| Category | Select | ❌ | TECHNICAL, BILLING, GENERAL, FEATURE_REQUEST, BUG_REPORT |

---

### 5. Submissions Dashboard
**Route:** `/submissions`

A unified view of all form submissions across all form types.

**Features:**
- **Statistics Cards:** Display count for each form type (Contact, Employee, Support)
- **Filters:**
  - Form type dropdown
  - Date range pickers (From/To)
  - Text search across all fields
  - Clear filters button
- **Data Grid:** Sortable table with:
  - Type badge (color-coded)
  - Title
  - Description preview
  - Submission date
  - View details button
- **Detail Dialog:** Full information view for each submission

---

### 6. Floating Assistant (Global)

Available on **every page** as a floating chat button in the bottom-right corner.

**Features:**
- Collapsible chat panel
- Page context awareness
- Same AI capabilities as main assistant
- Maintains separate conversation history
- Can populate forms on the current page

---

## A2A Protocol

The A2A (Agent-to-Agent) protocol enables external AI agents to interact with this server programmatically. The implementation follows JSON-RPC 2.0 specification.

### Agent Discovery

External agents can discover this server's capabilities by fetching the Agent Card:

```
GET /.well-known/agent.json
```

**Response:**
```json
{
  "name": "Form Assistant Agent",
  "description": "An AI-powered agent that can navigate to forms, populate them with data, and submit them.",
  "url": "http://localhost:8080",
  "version": "1.0.0",
  "skills": [...],
  "capabilities": {
    "streaming": false,
    "pushNotifications": false,
    "stateTransitionHistory": false
  },
  "authentication": {
    "schemes": ["none"]
  }
}
```

---

### Task Execution

All task operations use the main A2A endpoint:

```
POST /a2a
Content-Type: application/json
```

#### Supported Methods

| Method | Description |
|--------|-------------|
| `tasks/send` | Create and execute a new task |
| `tasks/get` | Get status of an existing task |
| `tasks/cancel` | Cancel a running task |

---

### Available Skills

| Skill ID | Name | Description |
|----------|------|-------------|
| `navigate-form` | Navigate to Form | Get navigation info for a form |
| `submit-contact` | Submit Contact Form | Create a contact submission |
| `submit-employee` | Submit Employee Registration | Register a new employee |
| `submit-support-ticket` | Submit Support Ticket | Create a support ticket |
| `ask-assistant` | Ask AI Assistant | Send natural language to AI |

---

### Request/Response Examples

#### Submit Contact Form

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "tasks/send",
  "id": "req-001",
  "params": {
    "skill": "submit-contact",
    "input": {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phone": "+1-555-123-4567",
      "company": "Acme Inc.",
      "message": "I would like to learn more about your services."
    }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "req-001",
  "result": {
    "id": "task-uuid-here",
    "status": "completed",
    "result": {
      "role": "agent",
      "parts": [
        {
          "type": "text",
          "text": "{success=true, contactId=1, message=Contact form submitted successfully for John Doe}"
        }
      ]
    },
    "artifacts": [
      {
        "name": "result",
        "mimeType": "application/json",
        "data": {
          "success": true,
          "contactId": 1,
          "message": "Contact form submitted successfully for John Doe"
        }
      }
    ]
  }
}
```

---

#### Submit Employee Registration

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "tasks/send",
  "id": "req-002",
  "params": {
    "skill": "submit-employee",
    "input": {
      "firstName": "Alice",
      "lastName": "Johnson",
      "email": "alice.johnson@company.com",
      "department": "Engineering",
      "position": "Senior Software Engineer",
      "hireDate": "2025-02-01",
      "salary": 95000
    }
  }
}
```

---

#### Submit Support Ticket

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "tasks/send",
  "id": "req-003",
  "params": {
    "skill": "submit-support-ticket",
    "input": {
      "subject": "Unable to reset password",
      "description": "User receives 'invalid token' error when clicking the password reset link in email. Issue started after the latest deployment.",
      "reporterName": "Support Bot",
      "reporterEmail": "support-bot@system.ai",
      "priority": "HIGH",
      "category": "TECHNICAL"
    }
  }
}
```

---

#### Natural Language via AI Assistant

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "tasks/send",
  "id": "req-004",
  "params": {
    "skill": "ask-assistant",
    "input": {
      "message": "Register a new employee named Bob Wilson in the Sales department as Account Manager with a salary of $70,000 starting next Monday"
    }
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "req-004",
  "result": {
    "id": "task-uuid",
    "status": "completed",
    "result": {
      "role": "agent",
      "parts": [
        {
          "type": "text",
          "text": "{action=submit, formId=employee, formData={firstName=Bob, lastName=Wilson, ...}, message=I'll register Bob Wilson...}"
        }
      ]
    },
    "artifacts": [
      {
        "name": "result",
        "mimeType": "application/json",
        "data": {
          "action": "submit",
          "formId": "employee",
          "formData": {
            "firstName": "Bob",
            "lastName": "Wilson",
            "department": "Sales",
            "position": "Account Manager",
            "salary": 70000
          },
          "message": "I'll register Bob Wilson as an Account Manager in the Sales department."
        }
      }
    ]
  }
}
```

---

#### Get Task Status

**Request:**
```json
{
  "jsonrpc": "2.0",
  "method": "tasks/get",
  "id": "req-005",
  "params": {
    "id": "task-uuid-here"
  }
}
```

---

#### Error Response Example

```json
{
  "jsonrpc": "2.0",
  "id": "req-006",
  "error": {
    "code": -32602,
    "message": "firstName is required",
    "data": null
  }
}
```

**Error Codes:**
| Code | Meaning |
|------|---------|
| -32700 | Parse error |
| -32600 | Invalid request |
| -32601 | Method not found |
| -32602 | Invalid params |
| -32603 | Internal error |
| -32001 | Task not found |
| -32002 | Task failed |

---

## API Reference

### REST Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/.well-known/agent.json` | Agent Card (capability discovery) |
| POST | `/a2a` | A2A JSON-RPC endpoint |

### Vaadin Routes

| Route | View | Description |
|-------|------|-------------|
| `/` | AssistantView | AI chat interface |
| `/contact` | ContactFormView | Contact form |
| `/employee` | EmployeeFormView | Employee registration |
| `/support` | SupportTicketFormView | Support ticket form |
| `/submissions` | SubmissionsView | Submissions dashboard |

---

## Configuration

### application.properties

```properties
# Server
server.port=${PORT:8080}

# Vaadin
vaadin.launch-browser=true
vaadin.push.enabled=true

# OpenAI Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.7

# Database
spring.jpa.hibernate.ddl-auto=update
```

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `OPENAI_API_KEY` | ✅ | Your OpenAI API key |
| `PORT` | ❌ | Server port (default: 8080) |

---

## Project Structure

```
src/main/java/hr/example/
├── Application.java                 # Spring Boot entry point (@Push enabled)
├── a2a/                             # A2A Protocol implementation
│   ├── A2AController.java           # REST endpoints for A2A
│   ├── A2AService.java              # Task execution logic
│   └── model/
│       ├── AgentCard.java           # Agent capability descriptor
│       ├── A2ARequest.java          # JSON-RPC request model
│       ├── A2AResponse.java         # JSON-RPC response model
│       └── A2ATask.java             # Task state model
├── assistant/                       # AI Assistant feature
│   ├── AssistantService.java        # OpenAI integration & chat logic
│   ├── ChatMessage.java             # Chat history model
│   ├── FormInfo.java                # Form metadata registry
│   ├── NavigationAction.java        # AI response action model
│   └── ui/
│       ├── AssistantView.java       # Main chat page
│       └── FloatingAssistant.java   # Global floating chat widget
├── base/                            # Shared components
│   └── ui/
│       ├── MainLayout.java          # App layout with navigation
│       └── component/
│           └── ViewToolbar.java     # Reusable toolbar component
├── contact/                         # Contact form feature
│   ├── Contact.java                 # JPA entity
│   ├── ContactRepository.java       # Data access
│   ├── ContactService.java          # Business logic
│   └── ui/
│       └── ContactFormView.java     # Form UI
├── employee/                        # Employee form feature
│   ├── Employee.java
│   ├── EmployeeRepository.java
│   ├── EmployeeService.java
│   └── ui/
│       └── EmployeeFormView.java
├── support/                         # Support ticket feature
│   ├── SupportTicket.java
│   ├── SupportTicketRepository.java
│   ├── SupportTicketService.java
│   └── ui/
│       └── SupportTicketFormView.java
└── submissions/                     # Submissions dashboard feature
    ├── SubmissionDTO.java           # Unified submission model
    ├── SubmissionsService.java      # Aggregation service
    └── ui/
        └── SubmissionsView.java     # Dashboard UI
```

---

## Building & Deployment

### Development

```bash
./mvnw spring-boot:run
```

### Production Build

```bash
./mvnw clean package -Pproduction
```

The production JAR will be created at `target/a2aserver-1.0-SNAPSHOT.jar`

### Run Production JAR

```bash
java -jar target/a2aserver-1.0-SNAPSHOT.jar
```

### Docker

**Build image:**
```bash
docker build -t a2a-form-assistant:latest .
```

**Run container:**
```bash
docker run -d \
  -p 8080:8080 \
  -e OPENAI_API_KEY=sk-your-key-here \
  --name a2a-server \
  a2a-form-assistant:latest
```

---

## Testing A2A Integration

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

# Submit employee registration
response = requests.post(
    "http://localhost:8080/a2a",
    json={
        "jsonrpc": "2.0",
        "method": "tasks/send",
        "id": "py-001",
        "params": {
            "skill": "submit-employee",
            "input": {
                "firstName": "Python",
                "lastName": "Agent",
                "email": "python@agent.ai",
                "department": "Engineering",
                "position": "AI Developer"
            }
        }
    }
)
print(response.json())
```

---

## License

See [LICENSE.md](LICENSE.md) for details.

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

## Support

For issues and feature requests, please use the GitHub issue tracker.
