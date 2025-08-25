package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.LangFilterTitledPane;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.IconRegion;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.util.Objects;
import java.util.ResourceBundle;

public class LangFilterTitledPaneSkin extends TitledPaneSkin {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");
    private static final PseudoClass MODIFIED_TITLED_PANE = PseudoClass.getPseudoClass("modified");
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass TALLER_TITLE_AREA = PseudoClass.getPseudoClass("taller");

    private final Region arrow;
    private final VBox titleBox;
    private final LangGridPane selectedOptionPane;
    private final LangContentBox contentBox;
    private final LangFilterTitledPane control;
    private Subscription subscription;
    private ScrollPane scrollPane;
    private FilterOptions.LanguageCoordinates currentLangCoordinates;

    public LangFilterTitledPaneSkin(LangFilterTitledPane control) {
        super(control);
        this.control = control;

        Label titleLabel = new Label(control.getText(), new IconRegion("circle"));
        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("title-label");

        selectedOptionPane = new LangGridPane();
        selectedOptionPane.getStyleClass().add("grid-option");

        titleBox = new VBox(titleLabel, selectedOptionPane);
        titleBox.getStyleClass().add("title-box");
        control.setGraphic(titleBox);

        Region titleRegion = (Region) control.lookup(".title");
        arrow = (Region) titleRegion.lookup(".arrow-button");

        arrow.translateXProperty().bind(titleRegion.widthProperty().subtract(arrow.widthProperty().add(arrow.layoutXProperty())));
        arrow.translateYProperty().bind(titleBox.layoutYProperty().subtract(arrow.layoutYProperty()));
        titleBox.translateXProperty().bind(arrow.widthProperty().multiply(-1));

        contentBox = new LangContentBox();
        control.setContent(contentBox);

        titleRegion.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!control.isExpanded()) {
//                boolean active = contentBox.isComboBoxActive(e);
//                control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, active);
//                e.consume();
            }
        });

        if (control.getParent() instanceof Accordion accordion) {
            Parent parent = accordion.getParent();
            while (!(parent instanceof ScrollPane)) {
                parent = parent.getParent();
            }

            scrollPane = (ScrollPane) parent;
        }

        setupAccordion();
    }

    private void setupAccordion() {
        if (subscription != null) {
            subscription.unsubscribe();
        }

        currentLangCoordinates = control.getLangCoordinates();
        selectedOptionPane.setLangOption(currentLangCoordinates);
        contentBox.setLangOption(currentLangCoordinates);

        subscription = Subscription.EMPTY;
//        subscription = selectedOption.boundsInParentProperty().subscribe(b ->
//                pseudoClassStateChanged(TALLER_TITLE_AREA, b.getHeight() > 30));

//        subscription = subscription.and(selectedOption.textProperty().subscribe(text -> {
//            List<String> defaultOptions = currentOption.defaultOptions();
//            if (defaultOptions.isEmpty()) {
//                defaultOptions.add(currentOption.availableOptions().getFirst());
//            }
//            pseudoClassStateChanged(MODIFIED_TITLED_PANE, !text.isEmpty() && !text.equals(String.join(", ", defaultOptions)));
//        }));

        if (control.getParent() instanceof Accordion accordion) {
            subscription = subscription.and(accordion.expandedPaneProperty().subscribe(pane -> {
                if (!(pane instanceof LangFilterTitledPane)) {
                    control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
                }
            }));
        }

        subscription = subscription.and(control.heightProperty().subscribe(_ -> {
            if (control.isExpanded()) {
                scrollPane.setVvalue(scrollPane.getVmax()); // TODO: make sure this titlePane is visible
            }
        }));

//        subscription = subscription.and(comboBox.showingProperty().subscribe((_, showing) -> {
//            if (showing && !contentBox.getChildren().isEmpty() && !control.isExpanded()) {
//                comboBox.hide();
//                control.setExpanded(true);
//            }
//        }));

//        subscription = subscription.and(comboBox.getSelectionModel().selectedIndexProperty().subscribe((_, value) -> {
//            if (comboBox.isShowing()) {
//                // reset
//                currentOption = new FilterOptions().getTime();
//            }

//            if (value.intValue() == 0) {
//                contentBox.getChildren().clear();
//                if (comboBox.isShowing()) {
//                    control.setExpanded(false);
//                    control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
//                }
//            } else {
//                if (value.intValue() == 1) {
//                    createSpecificDatePane(currentOption);
//                }
//                if (comboBox.isShowing()) {
//                    control.setExpanded(true);
//                    control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
//                }
//            }
//            selectedOption.setText(getOptionText(currentOption));
//        }));

        // confirm changes
        subscription = subscription.and(control.expandedProperty().subscribe((_, expanded) -> {
            if (!expanded) {
                control.setLangCoordinates(currentLangCoordinates.copy());
                pseudoClassStateChanged(MODIFIED_TITLED_PANE,
                        !Objects.equals(currentLangCoordinates, control.getDefaultLangCoordinates()));
            }
        }));

        subscription = subscription.and(control.langCoordinatesProperty().subscribe((_, _) -> setupAccordion()));

//        List<String> selectedOptions = control.getOption().getOptionSet().selectedOptions();
//        List<String> excludedOptions = control.getOption().getOptionSet().excludedOptions();

    }

    @Override
    public void dispose() {
        arrow.translateXProperty().unbind();
        arrow.translateYProperty().unbind();
        titleBox.translateXProperty().unbind();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        super.dispose();
    }

    private String getOptionText(FilterOptions.Option option) {
        if (option == null) {
            return null;
        }
        return "";
    }

    private static class LangGridPane extends GridPane {

        private final Label langOption;
        private final Label dialectOption;
        private final Label patternOption;
        private final Label descriptionOption;

        public LangGridPane() {
            Label langLabel = new Label(resources.getString("language.option.title"));
            langLabel.getStyleClass().add("title-label");
            add(langLabel, 0, 0);

            Region line1 = new Region();
            line1.getStyleClass().add("line");
            add(line1, 0, 1, 2, 1);

            Label dialectLabel = new Label(resources.getString("dialect.option.title"));
            dialectLabel.getStyleClass().add("title-label");
            add(dialectLabel, 0, 2);

            Region line2 = new Region();
            line2.getStyleClass().add("line");
            add(line2, 0, 3, 2, 1);

            Label patternLabel = new Label(resources.getString("pattern.option.title"));
            patternLabel.getStyleClass().add("title-label");
            add(patternLabel, 0, 4);

            Region line3 = new Region();
            line3.getStyleClass().add("line");
            add(line3, 0, 5, 2, 1);

            Label descriptionLabel = new Label(resources.getString("description.option.title"));
            descriptionLabel.getStyleClass().add("title-label");
            add(descriptionLabel, 0, 6);

            Region line4 = new Region();
            line4.getStyleClass().add("line");
            add(line4, 0, 7, 2, 1);

            langOption = new Label();
            langOption.getStyleClass().add("option-label");
            add(langOption, 1, 0);

            dialectOption = new Label();
            dialectOption.getStyleClass().add("option-label");
            add(dialectOption, 1, 2);

            patternOption = new Label();
            patternOption.getStyleClass().add("option-label");
            add(patternOption, 1, 4);

            descriptionOption = new Label();
            descriptionOption.getStyleClass().add("option-label");
            add(descriptionOption, 1, 6);

            ColumnConstraints columnConstraints1 = new ColumnConstraints();
            columnConstraints1.setMinWidth(80);
            columnConstraints1.setPrefWidth(80);
            columnConstraints1.setMaxWidth(80);
            getColumnConstraints().add(columnConstraints1);
            ColumnConstraints columnConstraints2 = new ColumnConstraints();
            columnConstraints2.setFillWidth(true);
            getColumnConstraints().add(columnConstraints2);

            RowConstraints rowConstraints1 = new RowConstraints();
            rowConstraints1.setMinHeight(20);
            rowConstraints1.setVgrow(Priority.SOMETIMES);
            RowConstraints rowConstraints2 = new RowConstraints();
            rowConstraints2.setPrefHeight(9);
            rowConstraints2.setVgrow(Priority.NEVER);
            RowConstraints rowConstraints3 = new RowConstraints();
            rowConstraints3.setMinHeight(20);
            rowConstraints3.setVgrow(Priority.SOMETIMES);
            RowConstraints rowConstraints4 = new RowConstraints();
            rowConstraints4.setPrefHeight(9);
            rowConstraints4.setVgrow(Priority.NEVER);
            RowConstraints rowConstraints5 = new RowConstraints();
            rowConstraints5.setMinHeight(20);
            rowConstraints5.setVgrow(Priority.SOMETIMES);
            RowConstraints rowConstraints6 = new RowConstraints();
            rowConstraints6.setPrefHeight(9);
            rowConstraints6.setVgrow(Priority.NEVER);
            RowConstraints rowConstraints7 = new RowConstraints();
            rowConstraints7.setMinHeight(20);
            rowConstraints7.setVgrow(Priority.SOMETIMES);
            RowConstraints rowConstraints8 = new RowConstraints();
            rowConstraints8.setPrefHeight(9);
            rowConstraints8.setVgrow(Priority.NEVER);

            getRowConstraints().addAll(
                    rowConstraints1, rowConstraints2,
                    rowConstraints3, rowConstraints4,
                    rowConstraints5, rowConstraints6,
                    rowConstraints7, rowConstraints8
            );

        }

        // langOptionProperty
        private final ObjectProperty<FilterOptions.LanguageCoordinates> langOptionProperty = new SimpleObjectProperty<>(this, "langOption") {
            @Override
            protected void invalidated() {
                FilterOptions.LanguageCoordinates languageOptions = get();
                if (languageOptions != null) {
                    langOption.setText(languageOptions.getLanguage().selectedOptions().isEmpty() ? "" : languageOptions.getLanguage().selectedOptions().getFirst());
                    dialectOption.setText(String.join(", ", languageOptions.getDialect().selectedOptions()));
                    patternOption.setText(languageOptions.getPattern().selectedOptions().isEmpty() ? "" : languageOptions.getPattern().selectedOptions().getFirst());
                    descriptionOption.setText(String.join(", ", languageOptions.getDescriptionType().selectedOptions()));
                }
            }
        };
        public final ObjectProperty<FilterOptions.LanguageCoordinates> langOptionProperty() {
            return langOptionProperty;
        }
        public final FilterOptions.LanguageCoordinates getLangOption() {
            return langOptionProperty.get();
        }
        public final void setLangOption(FilterOptions.LanguageCoordinates value) {
            langOptionProperty.set(value);
        }

    }

    private static class LangContentBox extends VBox {

        private final ComboBox<String> comboBox;

        private final VBox dialectBox;

        public LangContentBox() {
            Label langLabel = new Label(resources.getString("language.option.title"));
            langLabel.getStyleClass().add("title-label");

            comboBox = new ComboBox<>();
            comboBox.setCellFactory(_ -> new ListCell<>() {
                private final Label label;
                private final HBox box;
                {
                    label = new Label();
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    StackPane region = new StackPane(new IconRegion("icon", "check"));
                    region.getStyleClass().add("region");
                    box = new HBox(label, spacer, region);
                    box.getStyleClass().add("box");
                    setText(null);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        label.setText(item);
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                    }
                }
            });
            comboBox.getStyleClass().add("lang-combo-box");

            Region line1 = new Region();
            line1.getStyleClass().add("line");

            dialectBox = new VBox();
            dialectBox.getStyleClass().add("option-toggle-box");

            Label dialectLabel = new Label(resources.getString("dialect.option.title"));
            dialectLabel.getStyleClass().add("title-label");

            Region line2 = new Region();
            line2.getStyleClass().add("line");

            Label patternLabel = new Label(resources.getString("pattern.option.title"));
            patternLabel.getStyleClass().add("title-label");

            Region line3 = new Region();
            line3.getStyleClass().add("line");

            Label descriptionLabel = new Label(resources.getString("description.option.title"));
            descriptionLabel.getStyleClass().add("title-label");

            getChildren().addAll(
                    langLabel, comboBox, line1,
                    dialectLabel, dialectBox, line2,
                    patternLabel, line3, descriptionLabel);
            getStyleClass().add("content-box");
        }

        // langOptionProperty
        private final ObjectProperty<FilterOptions.LanguageCoordinates> langOptionProperty = new SimpleObjectProperty<>(this, "langOption") {
            @Override
            protected void invalidated() {
                FilterOptions.LanguageCoordinates languageOptions = get();
                if (languageOptions != null) {
                    comboBox.setItems(FXCollections.observableArrayList(languageOptions.getLanguage().availableOptions()));
                    comboBox.setValue(languageOptions.getLanguage().selectedOptions().isEmpty() ?
                            null : languageOptions.getLanguage().selectedOptions().getFirst());

                    dialectBox.getChildren().clear();
                    dialectBox.getChildren().addAll(
                            languageOptions.getDialect().availableOptions().stream()
                                    .map(o -> {
                                        ToggleButton tb = new ToggleButton(o, new IconRegion("check"));
                                        tb.getStyleClass().add("option-toggle");
                                        return tb;
                                    })
                                    .toList());

//                    dialectOption.setText(String.join(", ", languageOptions.getDialect().getSelectedOptions()));
//                    patternOption.setText(languageOptions.getPattern().getSelectedOptions().isEmpty() ? "" : languageOptions.getPattern().getSelectedOptions().getFirst());
//                    descriptionOption.setText(String.join(", ", languageOptions.getDescriptionType().getSelectedOptions()));
                }
            }
        };
        public final ObjectProperty<FilterOptions.LanguageCoordinates> langOptionProperty() {
            return langOptionProperty;
        }
        public final FilterOptions.LanguageCoordinates getLangOption() {
            return langOptionProperty.get();
        }
        public final void setLangOption(FilterOptions.LanguageCoordinates value) {
            langOptionProperty.set(value);
        }

        boolean isComboBoxActive(MouseEvent e) {
            return !comboBox.isVisible() || comboBox.localToScene(comboBox.getBoundsInLocal()).contains(e.getSceneX(), e.getSceneY());
        }
    }

}
