# A2A Personal AI Assistant Client

A Personal AI Assistant application built with **Vaadin**, **Spring Boot**, and **Spring AI** that helps you interact with **A2A (Agent-to-Agent) servers**. The assistant has access to your personal knowledge base (contacts, projects, notes) and can submit data to A2A agent servers on your behalf, with your approval.

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
- [Knowledge Base](#knowledge-base)
- [Agent Bookmarks](#agent-bookmarks)
- [A2A Client Integration](#a2a-client-integration)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Building & Deployment](#building--deployment)
- [Usage Examples](#usage-examples)

---

## Features

### 🤖 Personal AI Assistant
- Natural language interface powered by OpenAI GPT models
- Context-aware conversation with chat history
- Access to your personal knowledge base
- Understands your contacts, projects, and notes

### 📚 Personal Knowledge Base
- **Contacts**: Store contact details (name, email, phone, company, etc.)
- **Projects**: Track your projects with status, clients, and technologies
- **Notes**: Personal notes and reminders
- Data loaded from JSON/text files for easy customization

### 🔌 A2A Agent Integration
- Bookmark and manage multiple A2A agent servers
- Tag agents for easy reference in prompts (`@local`, `@production`)
- Test agent connectivity
- Send tasks to agents (submit forms, create tickets, etc.)

### ✅ Approval Workflow
- **Review before submit**: All data is shown for approval before sending to agents
- Clear display of target agent, action type, and data fields
- Cancel or confirm submissions
- Full transparency on what data leaves your system

---

## Technology Stack

| Layer | Technology | Version | Purpose |
|-------|------------|---------|---------|
| **Frontend** | Vaadin Flow | 24.9.5 | Server-side Java UI framework |
| **Backend** | Spring Boot | 3.5.7 | Application framework |
| **AI Integration** | Spring AI | 1.0.0-M6 | LLM integration framework |
| **LLM Provider** | OpenAI | GPT-4o-mini | Language model for assistant |
| **HTTP Client** | Spring WebFlux | 3.5.x | Reactive A2A client |
| **Database** | H2 | Runtime | In-memory database (agent bookmarks) |
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

<!-- WebFlux for A2A Client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         User Interface                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  AssistantView  │  │ AgentBookmarks  │  │ KnowledgeBase   │  │
│  │  (Chat UI)      │  │  View           │  │    View         │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      AI Assistant Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │AssistantService │  │  Chat History   │  │  OpenAI Client  │  │
│  │ Command Parser  │  │  Context Mgmt   │  │  GPT-4o-mini    │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌───────────────────────┐ ┌─────────────────┐ ┌─────────────────────┐
│   Knowledge Base      │ │ Agent Bookmarks │ │  A2A Client Service │
│  ┌─────────────────┐  │ │  ┌───────────┐  │ │  ┌───────────────┐  │
│  │   Contacts      │  │ │  │  Entity   │  │ │  │  WebClient    │  │
│  │   Projects      │  │ │  │  Repo     │  │ │  │  JSON-RPC     │  │
│  │   Notes         │  │ │  │  Service  │  │ │  │  HTTP Calls   │  │
│  └─────────────────┘  │ │  └───────────┘  │ │  └───────────────┘  │
└───────────────────────┘ └─────────────────┘ └─────────────────────┘
        │                         │                       │
        ▼                         ▼                       ▼
┌───────────────────────┐ ┌─────────────────┐ ┌─────────────────────┐
│  JSON/Text Files      │ │  H2 Database    │ │  External A2A       │
│  (resources/knowledge)│ │                 │ │  Agent Servers      │
└───────────────────────┘ └─────────────────┘ └─────────────────────┘
```

---

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9+ (or use included wrapper)
- OpenAI API key
- (Optional) Running A2A server to connect to

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd a2aclient
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
   
   Open your browser at: **http://localhost:8081**
   
   > Note: The client runs on port 8081 by default to avoid conflicts with a2aserver on port 8080.

---

## Application Screens

### 1. AI Assistant (Home Page)
**Route:** `/`

The main chat interface for interacting with your personal AI assistant.

**Features:**
- Full-page chat interface with message history
- Agent selector dropdown for targeting specific agents
- Natural language understanding with knowledge base context
- **Approval workflow**: Data is shown for review before sending to agents

**Example interactions:**
- "Show me my contacts"
- "What agents are available?"
- "Submit a contact form for Ante Antić on @local agent"
- "Create a support ticket about login issues with HIGH priority"

---

### 2. Agent Servers
**Route:** `/agents`

Manage your bookmarked A2A agent servers.

**Features:**
| Feature | Description |
|---------|-------------|
| **Add Agent** | Bookmark a new A2A server with name, URL, tag, and description |
| **Edit Agent** | Update agent details |
| **Test Connection** | Verify connectivity to the agent |
| **Activate/Deactivate** | Toggle agent availability |
| **Delete Agent** | Remove bookmark |

**Agent Properties:**
| Property | Description |
|----------|-------------|
| Name | Display name for the agent |
| URL | Base URL of the A2A server (e.g., `http://localhost:8080`) |
| Tag | Short identifier for use in prompts (`@local`, `@prod`) |
| Description | What the agent does |
| Status | Active or Inactive |
| Last Connected | When the agent was last successfully contacted |

---

### 3. Knowledge Base
**Route:** `/knowledge`

Browse your personal knowledge base data.

**Sections:**
- **My Profile**: Your personal information and preferences
- **Contacts**: List of all your saved contacts
- **Projects**: Your projects with status, client, and technology info
- **Notes**: Personal notes and reminders

---

## Knowledge Base

The knowledge base is loaded from JSON and text files in `src/main/resources/knowledge/`.

### Contacts (`contacts.json`)

```json
[
  {
    "id": "ante-antic",
    "firstName": "Ante",
    "lastName": "Antić",
    "email": "ante.antic@example.com",
    "phone": "+385 91 234 5678",
    "company": "Antić Solutions d.o.o.",
    "position": "CEO",
    "address": "Ilica 123, 10000 Zagreb, Croatia",
    "notes": "Long-time business partner.",
    "tags": ["business", "partner", "croatia"]
  }
]
```

### Profile (`profile.json`)

```json
{
  "owner": {
    "firstName": "Your",
    "lastName": "Name",
    "email": "you@example.com",
    "company": "Your Company",
    "position": "Your Role"
  },
  "preferences": {
    "communicationStyle": "professional",
    "defaultGreeting": "Hi",
    "defaultSignature": "Best regards"
  }
}
```

### Projects (`projects.json`)

```json
[
  {
    "id": "project-alpha",
    "name": "Project Alpha",
    "description": "Enterprise CRM system",
    "client": "TechCorp",
    "status": "active",
    "technologies": ["Java", "Spring Boot", "Vaadin"]
  }
]
```

### Notes (`notes.txt`)

Free-form text file for personal notes, reminders, and guidelines.

---

## Agent Bookmarks

### Default Agent

On first startup, a default bookmark is created:

| Property | Value |
|----------|-------|
| Name | Local Form Assistant |
| URL | http://localhost:8080 |
| Tag | local |
| Description | Local A2A Server - Form Assistant Agent |

### Using Tags in Prompts

Reference agents by their tag using `@` prefix:

```
"Submit contact form for Ante Antić on @local agent"
"Create a support ticket on @production"
```

---

## A2A Client Integration

The application acts as a **client** to A2A agent servers, not a server itself.

### Supported Operations

| Operation | Description |
|-----------|-------------|
| **Fetch Agent Card** | Discover agent capabilities via `/.well-known/agent.json` |
| **Submit Contact** | Send contact form data to agent |
| **Submit Employee** | Send employee registration data |
| **Submit Support Ticket** | Create support tickets |
| **Ask Assistant** | Send natural language to agent's AI |

### Approval Workflow

**Before any data is sent to an agent:**

1. AI prepares the data based on your request and knowledge base
2. A confirmation dialog appears showing:
   - Target agent name
   - Action type (e.g., "Submit Contact Form")
   - All data fields that will be sent
3. You can **Cancel** or **Confirm & Submit**
4. Only after confirmation is the data actually sent

```
┌─────────────────────────────────────────────────┐
│  📋 Confirm Submission                           │
├─────────────────────────────────────────────────┤
│  🎯 Target Agent: Local Form Assistant          │
│  ⚡ Action: Submit Contact Form                  │
│                                                 │
│  📝 Data to be submitted:                       │
│  ┌─────────────────────────────────────────┐   │
│  │ First Name:    Ante                      │   │
│  │ Last Name:     Antić                     │   │
│  │ Email:         ante.antic@example.com    │   │
│  │ Phone:         +385 91 234 5678          │   │
│  │ Company:       Antić Solutions d.o.o.    │   │
│  └─────────────────────────────────────────┘   │
│                                                 │
│  ⚠️ Please review the data above before        │
│     confirming.                                 │
│                                                 │
│  [Cancel]              [✓ Confirm & Submit]     │
└─────────────────────────────────────────────────┘
```

---

## Configuration

### application.properties

```properties
# Server (different port from a2aserver)
server.port=${PORT:8081}

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
| `PORT` | ❌ | Server port (default: 8081) |

---

## Project Structure

```
src/main/
├── java/hr/example/
│   ├── Application.java                 # Spring Boot entry point (@Push enabled)
│   ├── agent/                           # Agent bookmark feature
│   │   ├── AgentBookmark.java           # JPA entity for bookmarked agents
│   │   ├── AgentBookmarkRepository.java # Data access
│   │   ├── AgentBookmarkService.java    # Business logic
│   │   ├── ui/
│   │   │   └── AgentBookmarksView.java  # Agent management UI
│   │   └── a2a/
│   │       └── A2AClientService.java    # A2A protocol client
│   ├── assistant/                       # AI Assistant feature
│   │   ├── AssistantService.java        # OpenAI integration & chat logic
│   │   ├── AssistantAction.java         # AI response action model
│   │   ├── ChatMessage.java             # Chat history model
│   │   └── ui/
│   │       └── AssistantView.java       # Main chat page with approval dialog
│   ├── knowledge/                       # Knowledge base feature
│   │   ├── KnowledgeBaseService.java    # Data loading & search
│   │   ├── KnowledgeContact.java        # Contact model
│   │   ├── KnowledgeProfile.java        # Profile model
│   │   ├── KnowledgeProject.java        # Project model
│   │   └── ui/
│   │       └── KnowledgeBaseView.java   # Knowledge browser UI
│   └── base/                            # Shared components
│       └── ui/
│           ├── MainLayout.java          # App layout with navigation
│           └── component/
│               └── ViewToolbar.java     # Reusable toolbar
└── resources/
    ├── application.properties           # App configuration
    └── knowledge/                       # Knowledge base data files
        ├── contacts.json                # Your contacts
        ├── profile.json                 # Your profile
        ├── projects.json                # Your projects
        └── notes.txt                    # Your notes
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

The production JAR will be created at `target/a2aclient-1.0-SNAPSHOT.jar`

### Run Production JAR

```bash
java -jar target/a2aclient-1.0-SNAPSHOT.jar
```

### Docker

**Build image:**
```bash
docker build -t a2a-client:latest .
```

**Run container:**
```bash
docker run -d \
  -p 8081:8081 \
  -e OPENAI_API_KEY=sk-your-key-here \
  --name a2a-client \
  a2a-client:latest
```

---

## Usage Examples

### Basic Conversation

```
You: "Show me my contacts"
AI: Lists all contacts from your knowledge base

You: "What agents are available?"
AI: Lists all active agent bookmarks with their tags
```

### Submitting to an Agent

```
You: "Submit contact form for Ante Antić on @local agent"
AI: "I found Ante Antić's information. I'll submit this to Local Form Assistant."

[Confirmation Dialog Opens]
- Shows: firstName, lastName, email, phone, company
- You click "Confirm & Submit"

AI: "✅ Agent response: Contact form submitted successfully for Ante Antić"
```

### Creating a Support Ticket

```
You: "Create a support ticket about login issues with HIGH priority on the local agent"
AI: "I'll create a support ticket on Local Form Assistant."

[Confirmation Dialog Opens]
- Shows: subject, description, reporterName, reporterEmail, priority
- You review and click "Confirm & Submit"

AI: "✅ Agent response: Support ticket created: Login issues"
```

### Using the Agent Selector

1. Select an agent from the dropdown (top-left of input area)
2. Type your message without mentioning the agent
3. The selected agent will be used automatically

---

## Comparison: a2aclient vs a2aserver

| Aspect | a2aclient | a2aserver |
|--------|-----------|-----------|
| **Role** | A2A Client | A2A Server |
| **Port** | 8081 | 8080 |
| **Purpose** | Personal AI assistant with knowledge base | Form processing agent |
| **Exposes A2A** | No (client only) | Yes (/.well-known/agent.json, /a2a) |
| **Consumes A2A** | Yes (connects to agents) | No |
| **Data Source** | JSON files (knowledge base) | H2 database (form submissions) |
| **Approval Workflow** | Yes (before sending) | No (direct execution) |

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
