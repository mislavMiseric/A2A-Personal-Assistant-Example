package hr.example.submissions.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import hr.example.base.ui.component.ViewToolbar;
import hr.example.submissions.SubmissionDTO;
import hr.example.submissions.SubmissionsService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

@Route("submissions")
@PageTitle("Form Submissions")
@Menu(order = 10, icon = "vaadin:records", title = "Submissions")
public class SubmissionsView extends Main {

    private final SubmissionsService submissionsService;
    private final Grid<SubmissionDTO> grid;
    
    private final ComboBox<String> formTypeFilter;
    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final TextField searchField;

    public SubmissionsView(SubmissionsService submissionsService) {
        this.submissionsService = submissionsService;

        // Filters
        formTypeFilter = new ComboBox<>("Form Type");
        formTypeFilter.setItems("", "CONTACT", "EMPLOYEE", "SUPPORT");
        formTypeFilter.setItemLabelGenerator(item -> {
            if (item == null || item.isEmpty()) return "All Forms";
            return switch (item) {
                case "CONTACT" -> "Contact Forms";
                case "EMPLOYEE" -> "Employee Registrations";
                case "SUPPORT" -> "Support Tickets";
                default -> item;
            };
        });
        formTypeFilter.setValue("");
        formTypeFilter.addValueChangeListener(e -> refreshGrid());

        fromDate = new DatePicker("From Date");
        fromDate.addValueChangeListener(e -> refreshGrid());

        toDate = new DatePicker("To Date");
        toDate.addValueChangeListener(e -> refreshGrid());

        searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshGrid());

        Button clearFilters = new Button("Clear", VaadinIcon.CLOSE_SMALL.create(), e -> {
            formTypeFilter.clear();
            fromDate.clear();
            toDate.clear();
            searchField.clear();
        });
        clearFilters.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout filters = new HorizontalLayout(formTypeFilter, fromDate, toDate, searchField, clearFilters);
        filters.setAlignItems(FlexComponent.Alignment.END);
        filters.getStyle().set("flex-wrap", "wrap");
        filters.addClassName(LumoUtility.Gap.MEDIUM);

        // Grid
        grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withZone(ZoneId.systemDefault());

        grid.addColumn(new ComponentRenderer<>(submission -> {
            Span badge = new Span(getFormTypeLabel(submission.formType()));
            badge.getElement().getThemeList().add("badge " + getFormTypeBadgeColor(submission.formType()));
            return badge;
        })).setHeader("Type").setWidth("130px").setFlexGrow(0);

        grid.addColumn(SubmissionDTO::title)
                .setHeader("Title")
                .setFlexGrow(1);

        grid.addColumn(submission -> {
            String desc = submission.description();
            return desc.length() > 60 ? desc.substring(0, 60) + "..." : desc;
        }).setHeader("Description").setFlexGrow(2);

        grid.addColumn(submission -> formatter.format(submission.createdAt()))
                .setHeader("Submitted")
                .setWidth("180px")
                .setFlexGrow(0);

        grid.addColumn(new ComponentRenderer<>(submission -> {
            Button viewBtn = new Button(VaadinIcon.EYE.create(), e -> showDetailsDialog(submission));
            viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            viewBtn.getElement().setAttribute("title", "View Details");
            return viewBtn;
        })).setHeader("").setWidth("80px").setFlexGrow(0);

        grid.setSizeFull();

        // Stats cards
        HorizontalLayout statsLayout = createStatsLayout();

        // Layout
        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM);

        add(new ViewToolbar("Form Submissions"));
        add(statsLayout);
        add(filters);
        add(grid);

        grid.getStyle().set("flex-grow", "1");

        refreshGrid();
    }

    private HorizontalLayout createStatsLayout() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.addClassName(LumoUtility.Gap.MEDIUM);
        stats.getStyle().set("flex-wrap", "wrap");

        stats.add(createStatCard("Contact Forms", submissionsService.countByFormType("CONTACT"), VaadinIcon.ENVELOPE, "primary"));
        stats.add(createStatCard("Employees", submissionsService.countByFormType("EMPLOYEE"), VaadinIcon.USER, "success"));
        stats.add(createStatCard("Support Tickets", submissionsService.countByFormType("SUPPORT"), VaadinIcon.TICKET, "error"));

        return stats;
    }

    private Div createStatCard(String title, long count, VaadinIcon icon, String theme) {
        Div card = new Div();
        card.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE);
        card.getStyle()
                .set("background", "var(--lumo-" + theme + "-color-10pct)")
                .set("border", "1px solid var(--lumo-" + theme + "-color-50pct)")
                .set("min-width", "180px")
                .set("flex", "1");

        var iconElement = icon.create();
        iconElement.getStyle().set("color", "var(--lumo-" + theme + "-color)");
        iconElement.setSize("24px");

        Span countSpan = new Span(String.valueOf(count));
        countSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD);
        countSpan.getStyle().set("color", "var(--lumo-" + theme + "-color)");

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        HorizontalLayout header = new HorizontalLayout(iconElement, countSpan);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName(LumoUtility.Gap.SMALL);

        VerticalLayout content = new VerticalLayout(header, titleSpan);
        content.setPadding(false);
        content.setSpacing(false);

        card.add(content);
        return card;
    }

    private void refreshGrid() {
        grid.setItems(submissionsService.getAllSubmissions(
                formTypeFilter.getValue(),
                fromDate.getValue(),
                toDate.getValue(),
                searchField.getValue()
        ));
    }

    private String getFormTypeLabel(String formType) {
        return switch (formType) {
            case "CONTACT" -> "Contact";
            case "EMPLOYEE" -> "Employee";
            case "SUPPORT" -> "Support";
            default -> formType;
        };
    }

    private String getFormTypeBadgeColor(String formType) {
        return switch (formType) {
            case "CONTACT" -> "primary";
            case "EMPLOYEE" -> "success";
            case "SUPPORT" -> "error";
            default -> "";
        };
    }

    private void showDetailsDialog(SubmissionDTO submission) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(submission.title());
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // Form type badge
        Span badge = new Span(getFormTypeLabel(submission.formType()));
        badge.getElement().getThemeList().add("badge " + getFormTypeBadgeColor(submission.formType()));

        // Details
        DescriptionList dl = new DescriptionList();
        dl.getStyle().set("margin", "0");

        for (Map.Entry<String, String> entry : submission.details().entrySet()) {
            Div term = new Div();
            term.setText(entry.getKey());
            term.addClassName(LumoUtility.FontWeight.SEMIBOLD);
            
            Div desc = new Div();
            desc.setText(entry.getValue());
            
            dl.add(term, desc);
        }

        // Description section
        if (submission.description() != null && !submission.description().equals("No message")) {
            H4 descTitle = new H4("Description");
            descTitle.addClassName(LumoUtility.Margin.Top.MEDIUM);
            
            Paragraph descPara = new Paragraph(submission.description());
            descPara.getStyle()
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("white-space", "pre-wrap");
            
            content.add(badge, dl, descTitle, descPara);
        } else {
            content.add(badge, dl);
        }

        // Timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
                .withZone(ZoneId.systemDefault());
        Span timestamp = new Span("Submitted: " + formatter.format(submission.createdAt()));
        timestamp.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        content.add(timestamp);

        dialog.add(content);

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeBtn);

        dialog.open();
    }
}

