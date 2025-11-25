package hr.example.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;

    public SupportTicketService(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public SupportTicket createTicket(String subject, String description, String reporterName,
                                       String reporterEmail, SupportTicket.Priority priority,
                                       SupportTicket.Category category) {
        SupportTicket ticket = new SupportTicket(subject, description, reporterName, reporterEmail);
        ticket.setPriority(priority);
        ticket.setCategory(category);
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicket> list(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }
}

