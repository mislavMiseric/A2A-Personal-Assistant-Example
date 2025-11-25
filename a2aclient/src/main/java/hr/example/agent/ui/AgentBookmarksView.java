package hr.example.agent.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import hr.example.agent.AgentBookmark;
import hr.example.agent.AgentBookmarkService;
import hr.example.agent.a2a.A2AClientService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * View for managing agent server bookmarks.
 */
@Route("agents")
@PageTitle("Agent Servers")
@Menu(order = 1, icon = "vaadin:server", title = "Agents")
public class AgentBookmarksView extends VerticalLayout {

    private final AgentBookmarkService bookmarkService;
    private final A2AClientService a2aClientService;
    private final Grid<AgentBookmark> grid;

    public AgentBookmarksView(AgentBookmarkService bookmarkService, A2AClientService a2aClientService) {
        this.bookmarkService = bookmarkService;
        this.a2aClientService = a2aClientService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        // Header
        add(createHeader());
        
        // Grid
        grid = createGrid();
        add(grid);
        setFlexGrow(1, grid);
        
        refreshGrid();
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("Agent Servers");
        title.getStyle().set("margin", "0");
        
        Button addButton = new Button("Add Agent", VaadinIcon.PLUS.create(), e -> openAddDialog());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> refreshGrid());
        
        HorizontalLayout header = new HorizontalLayout(title, refreshButton, addButton);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);
        
        return header;
    }

    private Grid<AgentBookmark> createGrid() {
        Grid<AgentBookmark> grid = new Grid<>(AgentBookmark.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        
        grid.addColumn(new ComponentRenderer<>(agent -> {
            VerticalLayout layout = new VerticalLayout();
            layout.setSpacing(false);
            layout.setPadding(false);
            
            Span name = new Span(agent.getName());
            name.getStyle().set("font-weight", "bold");
            
            Span tag = new Span(agent.getTag() != null ? "@" + agent.getTag() : "");
            tag.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-primary-color)")
                    .set("background", "var(--lumo-primary-color-10pct)")
                    .set("padding", "2px 8px")
                    .set("border-radius", "12px")
                    .set("margin-left", "8px");
            
            HorizontalLayout nameRow = new HorizontalLayout(name);
            if (agent.getTag() != null) {
                nameRow.add(tag);
            }
            nameRow.setAlignItems(FlexComponent.Alignment.CENTER);
            nameRow.setSpacing(false);
            
            layout.add(nameRow);
            
            if (agent.getDescription() != null) {
                Span desc = new Span(agent.getDescription());
                desc.getStyle()
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("color", "var(--lumo-secondary-text-color)");
                layout.add(desc);
            }
            
            return layout;
        })).setHeader("Agent").setAutoWidth(true).setFlexGrow(2);
        
        grid.addColumn(AgentBookmark::getUrl).setHeader("URL").setAutoWidth(true).setFlexGrow(1);
        
        grid.addColumn(new ComponentRenderer<>(agent -> {
            Span status = new Span(agent.isActive() ? "Active" : "Inactive");
            status.getStyle()
                    .set("padding", "4px 12px")
                    .set("border-radius", "16px")
                    .set("font-size", "var(--lumo-font-size-s)");
            if (agent.isActive()) {
                status.getStyle()
                        .set("background", "var(--lumo-success-color-10pct)")
                        .set("color", "var(--lumo-success-text-color)");
            } else {
                status.getStyle()
                        .set("background", "var(--lumo-contrast-10pct)")
                        .set("color", "var(--lumo-secondary-text-color)");
            }
            return status;
        })).setHeader("Status").setAutoWidth(true);
        
        grid.addColumn(new ComponentRenderer<>(agent -> {
            if (agent.getLastConnected() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        .withZone(ZoneId.systemDefault());
                return new Span(formatter.format(agent.getLastConnected()));
            }
            return new Span("-");
        })).setHeader("Last Connected").setAutoWidth(true);
        
        grid.addColumn(new ComponentRenderer<>(agent -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            
            Button testBtn = new Button(VaadinIcon.PLUG.create(), e -> testConnection(agent));
            testBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            testBtn.setTooltipText("Test Connection");
            
            Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(agent));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editBtn.setTooltipText("Edit");
            
            Button toggleBtn = new Button(
                    agent.isActive() ? VaadinIcon.PAUSE.create() : VaadinIcon.PLAY.create(),
                    e -> toggleActive(agent)
            );
            toggleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            toggleBtn.setTooltipText(agent.isActive() ? "Deactivate" : "Activate");
            
            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(agent));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.setTooltipText("Delete");
            
            actions.add(testBtn, editBtn, toggleBtn, deleteBtn);
            return actions;
        })).setHeader("Actions").setAutoWidth(true);
        
        return grid;
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Agent Server");
        dialog.setWidth("500px");
        
        TextField nameField = new TextField("Name");
        nameField.setRequired(true);
        nameField.setWidthFull();
        
        TextField urlField = new TextField("URL");
        urlField.setRequired(true);
        urlField.setPlaceholder("http://localhost:8080");
        urlField.setWidthFull();
        
        TextField tagField = new TextField("Tag");
        tagField.setPlaceholder("e.g., local, production");
        tagField.setHelperText("Use @tag in prompts to reference this agent");
        tagField.setWidthFull();
        
        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();
        
        FormLayout form = new FormLayout(nameField, urlField, tagField, descriptionField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        
        dialog.add(form);
        
        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        Button saveBtn = new Button("Save", e -> {
            if (nameField.getValue().isBlank() || urlField.getValue().isBlank()) {
                Notification.show("Name and URL are required", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            bookmarkService.createBookmark(
                    nameField.getValue().trim(),
                    urlField.getValue().trim(),
                    descriptionField.getValue().trim(),
                    tagField.getValue().isBlank() ? null : tagField.getValue().trim()
            );
            
            refreshGrid();
            dialog.close();
            Notification.show("Agent added successfully", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openEditDialog(AgentBookmark agent) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Agent Server");
        dialog.setWidth("500px");
        
        TextField nameField = new TextField("Name");
        nameField.setValue(agent.getName());
        nameField.setRequired(true);
        nameField.setWidthFull();
        
        TextField urlField = new TextField("URL");
        urlField.setValue(agent.getUrl());
        urlField.setRequired(true);
        urlField.setWidthFull();
        
        TextField tagField = new TextField("Tag");
        tagField.setValue(agent.getTag() != null ? agent.getTag() : "");
        tagField.setHelperText("Use @tag in prompts to reference this agent");
        tagField.setWidthFull();
        
        TextArea descriptionField = new TextArea("Description");
        descriptionField.setValue(agent.getDescription() != null ? agent.getDescription() : "");
        descriptionField.setWidthFull();
        
        FormLayout form = new FormLayout(nameField, urlField, tagField, descriptionField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        
        dialog.add(form);
        
        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        Button saveBtn = new Button("Save", e -> {
            if (nameField.getValue().isBlank() || urlField.getValue().isBlank()) {
                Notification.show("Name and URL are required", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            bookmarkService.updateBookmark(
                    agent.getId(),
                    nameField.getValue().trim(),
                    urlField.getValue().trim(),
                    descriptionField.getValue().trim(),
                    tagField.getValue().isBlank() ? null : tagField.getValue().trim(),
                    agent.isActive()
            );
            
            refreshGrid();
            dialog.close();
            Notification.show("Agent updated successfully", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void testConnection(AgentBookmark agent) {
        Notification.show("Testing connection to " + agent.getName() + "...", 2000, Notification.Position.BOTTOM_CENTER);
        
        a2aClientService.testConnection(agent.getUrl())
                .subscribe(success -> {
                    getUI().ifPresent(ui -> ui.access(() -> {
                        if (success) {
                            bookmarkService.updateLastConnected(agent.getId(), null);
                            refreshGrid();
                            Notification.show("✅ Connection successful!", 3000, Notification.Position.BOTTOM_CENTER)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        } else {
                            Notification.show("❌ Connection failed", 3000, Notification.Position.BOTTOM_CENTER)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    }));
                });
    }

    private void toggleActive(AgentBookmark agent) {
        bookmarkService.toggleActive(agent.getId());
        refreshGrid();
        String status = agent.isActive() ? "deactivated" : "activated";
        Notification.show("Agent " + status, 2000, Notification.Position.BOTTOM_CENTER);
    }

    private void confirmDelete(AgentBookmark agent) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Agent");
        dialog.setText("Are you sure you want to delete '" + agent.getName() + "'?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            bookmarkService.deleteBookmark(agent.getId());
            refreshGrid();
            Notification.show("Agent deleted", 2000, Notification.Position.BOTTOM_CENTER);
        });
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(bookmarkService.getAllBookmarks());
    }
}

