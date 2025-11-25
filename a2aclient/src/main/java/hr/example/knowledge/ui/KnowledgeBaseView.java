package hr.example.knowledge.ui;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import hr.example.knowledge.KnowledgeBaseService;
import hr.example.knowledge.KnowledgeContact;
import hr.example.knowledge.KnowledgeProfile;
import hr.example.knowledge.KnowledgeProject;

/**
 * View for browsing the knowledge base data.
 */
@Route("knowledge")
@PageTitle("Knowledge Base")
@Menu(order = 2, icon = "vaadin:book", title = "Knowledge Base")
public class KnowledgeBaseView extends VerticalLayout {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseView(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(createHeader());
        
        Accordion accordion = new Accordion();
        accordion.setWidthFull();
        
        // Profile section
        KnowledgeProfile profile = knowledgeBaseService.getProfile();
        if (profile != null) {
            accordion.add(createProfilePanel(profile));
        }
        
        // Contacts section
        accordion.add(createContactsPanel());
        
        // Projects section
        accordion.add(createProjectsPanel());
        
        // Notes section
        String notes = knowledgeBaseService.getNotes();
        if (notes != null && !notes.isBlank()) {
            accordion.add(createNotesPanel(notes));
        }
        
        add(accordion);
    }

    private Div createHeader() {
        H2 title = new H2("📚 Knowledge Base");
        title.getStyle().set("margin", "0");
        
        Paragraph description = new Paragraph("Your personal data that the AI assistant can use when interacting with agents.");
        description.addClassName(LumoUtility.TextColor.SECONDARY);
        
        Div header = new Div(title, description);
        header.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "var(--lumo-space-m)");
        
        return header;
    }

    private AccordionPanel createProfilePanel(KnowledgeProfile profile) {
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(false);
        content.setPadding(true);
        
        KnowledgeProfile.Owner owner = profile.owner();
        
        content.add(createInfoRow("Name", owner.fullName()));
        content.add(createInfoRow("Email", owner.email()));
        content.add(createInfoRow("Phone", owner.phone()));
        content.add(createInfoRow("Company", owner.company()));
        content.add(createInfoRow("Position", owner.position()));
        content.add(createInfoRow("Location", owner.location()));
        content.add(createInfoRow("Bio", owner.bio()));
        
        if (profile.preferences() != null) {
            content.add(new Hr());
            H4 prefTitle = new H4("Preferences");
            prefTitle.getStyle().set("margin-bottom", "8px");
            content.add(prefTitle);
            
            content.add(createInfoRow("Communication Style", profile.preferences().communicationStyle()));
            content.add(createInfoRow("Default Greeting", profile.preferences().defaultGreeting()));
            content.add(createInfoRow("Default Signature", profile.preferences().defaultSignature()));
        }
        
        return new AccordionPanel("👤 My Profile", content);
    }

    private AccordionPanel createContactsPanel() {
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        
        for (KnowledgeContact contact : knowledgeBaseService.getAllContacts()) {
            content.add(createContactCard(contact));
        }
        
        return new AccordionPanel("📇 Contacts (" + knowledgeBaseService.getAllContacts().size() + ")", content);
    }

    private Div createContactCard(KnowledgeContact contact) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "var(--lumo-space-s)");
        
        H4 name = new H4(contact.fullName());
        name.getStyle().set("margin", "0 0 8px 0");
        
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);
        
        details.add(createSmallInfoRow("📧", contact.email()));
        if (contact.phone() != null) details.add(createSmallInfoRow("📱", contact.phone()));
        if (contact.company() != null) details.add(createSmallInfoRow("🏢", contact.company() + (contact.position() != null ? " - " + contact.position() : "")));
        if (contact.address() != null) details.add(createSmallInfoRow("📍", contact.address()));
        if (contact.notes() != null) {
            Paragraph notes = new Paragraph("💬 " + contact.notes());
            notes.addClassName(LumoUtility.TextColor.SECONDARY);
            notes.getStyle().set("font-size", "var(--lumo-font-size-s)").set("margin", "4px 0");
            details.add(notes);
        }
        
        if (contact.tags() != null && !contact.tags().isEmpty()) {
            Div tagsDiv = new Div();
            tagsDiv.getStyle().set("margin-top", "8px");
            for (String tag : contact.tags()) {
                Span tagSpan = new Span(tag);
                tagSpan.getStyle()
                        .set("background", "var(--lumo-primary-color-10pct)")
                        .set("color", "var(--lumo-primary-color)")
                        .set("padding", "2px 8px")
                        .set("border-radius", "12px")
                        .set("font-size", "var(--lumo-font-size-xs)")
                        .set("margin-right", "4px");
                tagsDiv.add(tagSpan);
            }
            details.add(tagsDiv);
        }
        
        card.add(name, details);
        return card;
    }

    private AccordionPanel createProjectsPanel() {
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        
        for (KnowledgeProject project : knowledgeBaseService.getAllProjects()) {
            content.add(createProjectCard(project));
        }
        
        return new AccordionPanel("📁 Projects (" + knowledgeBaseService.getAllProjects().size() + ")", content);
    }

    private Div createProjectCard(KnowledgeProject project) {
        Div card = new Div();
        card.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "var(--lumo-space-s)");
        
        Div header = new Div();
        header.getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px");
        
        H4 name = new H4(project.name());
        name.getStyle().set("margin", "0");
        
        Span status = new Span(project.status().toUpperCase());
        status.getStyle()
                .set("padding", "2px 8px")
                .set("border-radius", "12px")
                .set("font-size", "var(--lumo-font-size-xs)");
        
        switch (project.status()) {
            case "active" -> status.getStyle()
                    .set("background", "var(--lumo-success-color-10pct)")
                    .set("color", "var(--lumo-success-text-color)");
            case "completed" -> status.getStyle()
                    .set("background", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-text-color)");
            default -> status.getStyle()
                    .set("background", "var(--lumo-contrast-10pct)")
                    .set("color", "var(--lumo-secondary-text-color)");
        }
        
        header.add(name, status);
        
        Paragraph desc = new Paragraph(project.description());
        desc.addClassName(LumoUtility.TextColor.SECONDARY);
        desc.getStyle().set("margin", "8px 0");
        
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);
        
        details.add(createSmallInfoRow("🏢", "Client: " + project.client()));
        details.add(createSmallInfoRow("📅", project.startDate() + " → " + project.expectedEndDate()));
        if (project.budget() != null) details.add(createSmallInfoRow("💰", project.budget()));
        
        if (project.technologies() != null && !project.technologies().isEmpty()) {
            Div techDiv = new Div();
            techDiv.getStyle().set("margin-top", "8px");
            for (String tech : project.technologies()) {
                Span techSpan = new Span(tech);
                techSpan.getStyle()
                        .set("background", "var(--lumo-contrast-10pct)")
                        .set("padding", "2px 8px")
                        .set("border-radius", "12px")
                        .set("font-size", "var(--lumo-font-size-xs)")
                        .set("margin-right", "4px");
                techDiv.add(techSpan);
            }
            details.add(techDiv);
        }
        
        card.add(header, desc, details);
        return card;
    }

    private AccordionPanel createNotesPanel(String notes) {
        Pre notesContent = new Pre(notes);
        notesContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("font-family", "var(--lumo-font-family)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin", "0");
        
        VerticalLayout content = new VerticalLayout(notesContent);
        content.setPadding(true);
        
        return new AccordionPanel("📝 Notes", content);
    }

    private Div createInfoRow(String label, String value) {
        Div row = new Div();
        row.getStyle().set("margin-bottom", "8px");
        
        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle().set("font-weight", "bold").set("color", "var(--lumo-secondary-text-color)");
        
        Span valueSpan = new Span(value != null ? value : "-");
        
        row.add(labelSpan, valueSpan);
        return row;
    }

    private Paragraph createSmallInfoRow(String icon, String text) {
        Paragraph p = new Paragraph(icon + " " + text);
        p.getStyle()
                .set("margin", "2px 0")
                .set("font-size", "var(--lumo-font-size-s)");
        return p;
    }
}

