package hr.example.agent;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing agent bookmarks.
 */
@Service
public class AgentBookmarkService {

    private static final Logger logger = LoggerFactory.getLogger(AgentBookmarkService.class);
    
    private final AgentBookmarkRepository repository;

    public AgentBookmarkService(AgentBookmarkRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    @Transactional
    public void initializeDefaultBookmarks() {
        // Add default bookmark for local a2aserver if not exists
        if (repository.findByUrl("http://localhost:8080").isEmpty()) {
            AgentBookmark defaultAgent = new AgentBookmark();
            defaultAgent.setName("Local Form Assistant");
            defaultAgent.setUrl("http://localhost:8080");
            defaultAgent.setDescription("Local A2A Server - Form Assistant Agent for handling contact, employee, and support forms.");
            defaultAgent.setTag("FormAgent");
            defaultAgent.setActive(true);
            repository.save(defaultAgent);
            logger.info("Created default local agent bookmark");
        }
    }

    @Transactional(readOnly = true)
    public List<AgentBookmark> getAllBookmarks() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AgentBookmark> getActiveBookmarks() {
        return repository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Optional<AgentBookmark> getBookmarkById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AgentBookmark> getBookmarkByUrl(String url) {
        return repository.findByUrl(url);
    }

    @Transactional(readOnly = true)
    public List<AgentBookmark> getBookmarksByTag(String tag) {
        return repository.findByTagAndActiveTrue(tag);
    }

    @Transactional(readOnly = true)
    public List<AgentBookmark> searchBookmarks(String query) {
        return repository.findByNameContainingIgnoreCase(query);
    }

    @Transactional
    public AgentBookmark createBookmark(String name, String url, String description, String tag) {
        AgentBookmark bookmark = new AgentBookmark();
        bookmark.setName(name);
        bookmark.setUrl(normalizeUrl(url));
        bookmark.setDescription(description);
        bookmark.setTag(tag);
        bookmark.setActive(true);
        return repository.save(bookmark);
    }

    @Transactional
    public AgentBookmark updateBookmark(Long id, String name, String url, String description, String tag, boolean active) {
        AgentBookmark bookmark = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found: " + id));
        bookmark.setName(name);
        bookmark.setUrl(normalizeUrl(url));
        bookmark.setDescription(description);
        bookmark.setTag(tag);
        bookmark.setActive(active);
        return repository.save(bookmark);
    }

    @Transactional
    public void deleteBookmark(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void updateLastConnected(Long id, String agentVersion) {
        repository.findById(id).ifPresent(bookmark -> {
            bookmark.setLastConnected(Instant.now());
            if (agentVersion != null) {
                bookmark.setAgentVersion(agentVersion);
            }
            repository.save(bookmark);
        });
    }

    @Transactional
    public void toggleActive(Long id) {
        repository.findById(id).ifPresent(bookmark -> {
            bookmark.setActive(!bookmark.isActive());
            repository.save(bookmark);
        });
    }

    private String normalizeUrl(String url) {
        if (url == null) return null;
        url = url.trim();
        // Remove trailing slash
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * Returns a formatted string of all active agents for AI context.
     */
    public String getAgentsContext() {
        List<AgentBookmark> agents = getActiveBookmarks();
        if (agents.isEmpty()) {
            return "No agent servers configured.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Available Agent Servers:\n");
        for (AgentBookmark agent : agents) {
            sb.append("- ").append(agent.getName());
            if (agent.getTag() != null) {
                sb.append(" [@").append(agent.getTag()).append("]");
            }
            sb.append(": ").append(agent.getUrl());
            if (agent.getDescription() != null) {
                sb.append("\n  Description: ").append(agent.getDescription());
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

