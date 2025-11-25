package hr.example.submissions;

import hr.example.contact.Contact;
import hr.example.contact.ContactRepository;
import hr.example.employee.Employee;
import hr.example.employee.EmployeeRepository;
import hr.example.support.SupportTicket;
import hr.example.support.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service that aggregates all form submissions from different sources.
 */
@Service
public class SubmissionsService {

    private final ContactRepository contactRepository;
    private final EmployeeRepository employeeRepository;
    private final SupportTicketRepository supportTicketRepository;

    public SubmissionsService(ContactRepository contactRepository,
                               EmployeeRepository employeeRepository,
                               SupportTicketRepository supportTicketRepository) {
        this.contactRepository = contactRepository;
        this.employeeRepository = employeeRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    @Transactional(readOnly = true)
    public List<SubmissionDTO> getAllSubmissions(String formTypeFilter, LocalDate fromDate, LocalDate toDate, String searchText) {
        List<SubmissionDTO> allSubmissions = new ArrayList<>();

        // Get contacts
        if (formTypeFilter == null || formTypeFilter.isEmpty() || "CONTACT".equals(formTypeFilter)) {
            contactRepository.findAll().stream()
                    .map(this::toSubmissionDTO)
                    .forEach(allSubmissions::add);
        }

        // Get employees
        if (formTypeFilter == null || formTypeFilter.isEmpty() || "EMPLOYEE".equals(formTypeFilter)) {
            employeeRepository.findAll().stream()
                    .map(this::toSubmissionDTO)
                    .forEach(allSubmissions::add);
        }

        // Get support tickets
        if (formTypeFilter == null || formTypeFilter.isEmpty() || "SUPPORT".equals(formTypeFilter)) {
            supportTicketRepository.findAll().stream()
                    .map(this::toSubmissionDTO)
                    .forEach(allSubmissions::add);
        }

        // Apply date filters
        Stream<SubmissionDTO> filtered = allSubmissions.stream();
        
        if (fromDate != null) {
            Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            filtered = filtered.filter(s -> s.createdAt().isAfter(fromInstant) || s.createdAt().equals(fromInstant));
        }
        
        if (toDate != null) {
            Instant toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            filtered = filtered.filter(s -> s.createdAt().isBefore(toInstant));
        }

        // Apply text search
        if (searchText != null && !searchText.isBlank()) {
            String search = searchText.toLowerCase();
            filtered = filtered.filter(s -> 
                    s.title().toLowerCase().contains(search) ||
                    s.description().toLowerCase().contains(search) ||
                    s.details().values().stream().anyMatch(v -> v.toLowerCase().contains(search))
            );
        }

        // Sort by creation date (newest first)
        return filtered
                .sorted(Comparator.comparing(SubmissionDTO::createdAt).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countByFormType(String formType) {
        return switch (formType) {
            case "CONTACT" -> contactRepository.count();
            case "EMPLOYEE" -> employeeRepository.count();
            case "SUPPORT" -> supportTicketRepository.count();
            default -> 0;
        };
    }

    private SubmissionDTO toSubmissionDTO(Contact contact) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("First Name", contact.getFirstName());
        details.put("Last Name", contact.getLastName());
        details.put("Email", contact.getEmail());
        if (contact.getPhone() != null) details.put("Phone", contact.getPhone());
        if (contact.getCompany() != null) details.put("Company", contact.getCompany());

        return new SubmissionDTO(
                contact.getId(),
                "CONTACT",
                contact.getFirstName() + " " + contact.getLastName(),
                contact.getMessage() != null ? contact.getMessage() : "No message",
                contact.getCreatedAt(),
                details
        );
    }

    private SubmissionDTO toSubmissionDTO(Employee employee) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("First Name", employee.getFirstName());
        details.put("Last Name", employee.getLastName());
        details.put("Email", employee.getEmail());
        if (employee.getDepartment() != null) details.put("Department", employee.getDepartment());
        if (employee.getPosition() != null) details.put("Position", employee.getPosition());
        if (employee.getHireDate() != null) details.put("Hire Date", employee.getHireDate().toString());
        if (employee.getSalary() != null) details.put("Salary", "$" + String.format("%.2f", employee.getSalary()));

        return new SubmissionDTO(
                employee.getId(),
                "EMPLOYEE",
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getPosition() != null ? employee.getPosition() + " in " + employee.getDepartment() : "New Employee",
                employee.getCreatedAt(),
                details
        );
    }

    private SubmissionDTO toSubmissionDTO(SupportTicket ticket) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Reporter", ticket.getReporterName());
        details.put("Email", ticket.getReporterEmail());
        if (ticket.getPriority() != null) details.put("Priority", ticket.getPriority().name());
        if (ticket.getCategory() != null) details.put("Category", ticket.getCategory().name().replace("_", " "));

        return new SubmissionDTO(
                ticket.getId(),
                "SUPPORT",
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCreatedAt(),
                details
        );
    }
}

