# AI TOOL GUIDANCE

This file provides guidance when working with code in this repository.

## Technology Stack

This is a Personal AI Assistant application built with:
- Java 21
- Spring Boot 3.5
- Spring AI with OpenAI
- Vaadin 24 for UI
- Spring Data JPA with H2 database
- Spring WebFlux for A2A client communication
- Maven build system

## Development Commands

### Running the Application
```bash
./mvnw                           # Start in development mode (default goal: spring-boot:run)
./mvnw spring-boot:run           # Explicit development mode
```

The application will be available at http://localhost:8081 (different port from a2aserver)

### Building for Production
```bash
./mvnw -Pproduction package      # Build production JAR
docker build -t a2aclient:latest .  # Build Docker image
```

### Environment Variables
- `OPENAI_API_KEY`: Required - Your OpenAI API key for AI assistant functionality
- `PORT`: Optional - Override the default port (8081)

## Architecture

This project follows a **feature-based package structure**.

### Package Structure

- **`hr.example.assistant`**: Personal AI Assistant feature
  - `AssistantService.java`: AI-powered assistant using Spring AI and OpenAI
  - `AssistantAction.java`: Action records for assistant responses
  - `ChatMessage.java`: Chat message record for conversation history
  - `ui.AssistantView.java`: Main chat UI for interacting with the assistant

- **`hr.example.agent`**: Agent server management feature
  - `AgentBookmark.java`: JPA entity for bookmarked A2A servers
  - `AgentBookmarkRepository.java`: Repository for agent bookmarks
  - `AgentBookmarkService.java`: Service for managing agent bookmarks
  - `ui.AgentBookmarksView.java`: UI for managing agent server bookmarks
  - `a2a.A2AClientService.java`: Client for communicating with A2A agent servers

- **`hr.example.knowledge`**: Knowledge base feature
  - `KnowledgeBaseService.java`: Service for loading and managing personal data
  - `KnowledgeContact.java`: Contact data model
  - `KnowledgeProfile.java`: User profile data model
  - `KnowledgeProject.java`: Project data model
  - `ui.KnowledgeBaseView.java`: UI for browsing knowledge base

- **`hr.example.base`**: Reusable components and base classes
  - `base.ui.MainLayout`: AppLayout with drawer navigation

### Key Features

1. **Personal AI Assistant**: Chat interface powered by OpenAI that understands your personal data
2. **Knowledge Base**: Load contacts, projects, and notes from JSON/text files
3. **Agent Bookmarks**: Save and manage connections to A2A agent servers
4. **A2A Protocol Client**: Send tasks to agent servers (submit forms, etc.)

### Knowledge Base Data

Knowledge base data is loaded from `src/main/resources/knowledge/`:
- `contacts.json`: Personal contacts with details
- `profile.json`: User profile and preferences
- `projects.json`: Project information
- `notes.txt`: Personal notes and quick references

### A2A Protocol Integration

The application can communicate with A2A-compatible agent servers:
- Fetch agent capabilities via `/.well-known/agent.json`
- Send tasks via JSON-RPC at `/a2a` endpoint
- Supported skills: `submit-contact`, `submit-employee`, `submit-support-ticket`, `ask-assistant`

## Usage Examples

### Using the Assistant

1. "Show me my contacts" - Lists contacts from knowledge base
2. "What agents are available?" - Lists bookmarked agent servers
3. "Submit a contact form for Ante Antić on @local agent" - Uses contact data to submit to agent
4. "Create a support ticket about login issues with HIGH priority" - Creates support ticket via agent

### Agent Tagging

Users can reference agents in prompts using:
- Agent name: "on Local Form Assistant"
- Tag: "@local"
- Direct selection from dropdown

## Database

- H2 in-memory database for development
- Stores agent bookmarks with connection history
- JPA entities use `@GeneratedValue(strategy = GenerationType.SEQUENCE)`
