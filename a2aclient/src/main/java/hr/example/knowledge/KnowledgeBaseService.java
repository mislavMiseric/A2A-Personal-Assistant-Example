package hr.example.knowledge;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for loading and managing the personal knowledge base.
 * Loads data from JSON and text files in the resources/knowledge directory.
 */
@Service
public class KnowledgeBaseService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseService.class);
    
    private final ObjectMapper objectMapper;
    
    private List<KnowledgeContact> contacts = new ArrayList<>();
    private KnowledgeProfile profile;
    private List<KnowledgeProject> projects = new ArrayList<>();
    private String notes = "";

    public KnowledgeBaseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadKnowledgeBase() {
        loadContacts();
        loadProfile();
        loadProjects();
        loadNotes();
        logger.info("Knowledge base loaded: {} contacts, {} projects", contacts.size(), projects.size());
    }

    private void loadContacts() {
        try {
            ClassPathResource resource = new ClassPathResource("knowledge/contacts.json");
            contacts = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            logger.warn("Could not load contacts.json: {}", e.getMessage());
        }
    }

    private void loadProfile() {
        try {
            ClassPathResource resource = new ClassPathResource("knowledge/profile.json");
            profile = objectMapper.readValue(resource.getInputStream(), KnowledgeProfile.class);
        } catch (IOException e) {
            logger.warn("Could not load profile.json: {}", e.getMessage());
        }
    }

    private void loadProjects() {
        try {
            ClassPathResource resource = new ClassPathResource("knowledge/projects.json");
            projects = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            logger.warn("Could not load projects.json: {}", e.getMessage());
        }
    }

    private void loadNotes() {
        try {
            ClassPathResource resource = new ClassPathResource("knowledge/notes.txt");
            notes = resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Could not load notes.txt: {}", e.getMessage());
        }
    }

    public List<KnowledgeContact> getAllContacts() {
        return new ArrayList<>(contacts);
    }

    public Optional<KnowledgeContact> findContactById(String id) {
        return contacts.stream()
                .filter(c -> c.id().equals(id))
                .findFirst();
    }

    public Optional<KnowledgeContact> findContactByName(String name) {
        String searchName = name.toLowerCase();
        return contacts.stream()
                .filter(c -> c.fullName().toLowerCase().contains(searchName) ||
                             c.firstName().toLowerCase().contains(searchName) ||
                             c.lastName().toLowerCase().contains(searchName))
                .findFirst();
    }

    public List<KnowledgeContact> searchContacts(String query) {
        String searchQuery = query.toLowerCase();
        return contacts.stream()
                .filter(c -> c.fullName().toLowerCase().contains(searchQuery) ||
                             (c.company() != null && c.company().toLowerCase().contains(searchQuery)) ||
                             (c.email() != null && c.email().toLowerCase().contains(searchQuery)) ||
                             (c.tags() != null && c.tags().stream().anyMatch(t -> t.toLowerCase().contains(searchQuery))))
                .toList();
    }

    public KnowledgeProfile getProfile() {
        return profile;
    }

    public List<KnowledgeProject> getAllProjects() {
        return new ArrayList<>(projects);
    }

    public Optional<KnowledgeProject> findProjectById(String id) {
        return projects.stream()
                .filter(p -> p.id().equals(id))
                .findFirst();
    }

    public List<KnowledgeProject> getActiveProjects() {
        return projects.stream()
                .filter(p -> "active".equals(p.status()))
                .toList();
    }

    public String getNotes() {
        return notes;
    }

    /**
     * Generates a complete context string for the AI assistant.
     * This provides all knowledge base data formatted for AI consumption.
     */
    public String getFullContext() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== PERSONAL KNOWLEDGE BASE ===\n\n");
        
        if (profile != null) {
            sb.append(profile.toContextString());
            sb.append("\n");
        }
        
        sb.append("=== CONTACTS ===\n\n");
        for (KnowledgeContact contact : contacts) {
            sb.append(contact.toContextString());
            sb.append("\n");
        }
        
        sb.append("=== PROJECTS ===\n\n");
        for (KnowledgeProject project : projects) {
            sb.append(project.toContextString());
            sb.append("\n");
        }
        
        sb.append("=== NOTES ===\n\n");
        sb.append(notes);
        
        return sb.toString();
    }

    /**
     * Gets context about a specific contact for use in agent interactions.
     */
    public String getContactContext(String contactId) {
        return findContactById(contactId)
                .map(KnowledgeContact::toContextString)
                .orElse("Contact not found: " + contactId);
    }
}

