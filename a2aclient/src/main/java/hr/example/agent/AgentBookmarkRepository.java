package hr.example.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing agent bookmarks.
 */
public interface AgentBookmarkRepository extends JpaRepository<AgentBookmark, Long> {
    
    List<AgentBookmark> findByActiveTrue();
    
    List<AgentBookmark> findByTag(String tag);
    
    List<AgentBookmark> findByTagAndActiveTrue(String tag);
    
    Optional<AgentBookmark> findByUrl(String url);
    
    List<AgentBookmark> findByNameContainingIgnoreCase(String name);
}

