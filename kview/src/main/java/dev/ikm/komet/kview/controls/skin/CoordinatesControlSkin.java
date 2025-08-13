package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.CoordinatesControl;
import dev.ikm.komet.kview.controls.IconRegion;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Locale;
import java.util.ResourceBundle;

public class CoordinatesControlSkin extends SkinBase<CoordinatesControl> {

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.coordinates-control");
    private final CoordinatesControl control;
    private final CoordinatesControl.COORDINATES_TYPE coordinatesType;

    private final HBox headerBox;
    private final Accordion accordion;
    private final ScrollPane scrollPane;
    private final VBox bottomBox;
    private final Button saveButton;
    private final Button revertButton;

    public CoordinatesControlSkin(CoordinatesControl control) {
        super(control);
        this.control = control;
        coordinatesType = control.getCoordinatesType();

        StackPane closePane = new StackPane(new IconRegion("icon", "close"));
        closePane.getStyleClass().add("region");
        closePane.setOnMouseClicked(_ -> {});

        Label title = new Label(resources.getString(coordinatesType.name().toLowerCase(Locale.ROOT) + ".control.title"));
        title.getStyleClass().add("title");

        ToggleButton filterPane = new ToggleButton(null, new IconRegion("icon", "filter"));
        filterPane.getStyleClass().add("filter-button");

        headerBox = new HBox(closePane, title, filterPane);
        headerBox.getStyleClass().add("header-box");

        TitledPane stampTitledPane = new TitledPane(resources.getString("titled.pane.stamp.title"), new Accordion());
        stampTitledPane.getStyleClass().add("coordinates-titled-pane");

        TitledPane languageTitledPane = new TitledPane(resources.getString("titled.pane.language.title"), new Accordion());
        languageTitledPane.getStyleClass().add("coordinates-titled-pane");

        TitledPane otherTitledPane = new TitledPane(resources.getString("titled.pane.other.title"), new Accordion());
        otherTitledPane.getStyleClass().add("coordinates-titled-pane");

        accordion = new Accordion();
        accordion.getStyleClass().add("coordinates-accordion");
        accordion.getPanes().addAll(stampTitledPane, languageTitledPane, otherTitledPane);
        scrollPane = new ScrollPane(accordion);
        scrollPane.setFitToWidth(true);

        StackPane region = new StackPane(new IconRegion("icon", "save"));
        region.getStyleClass().add("region");

        saveButton = new Button(resources.getString("button.save"), region);
        saveButton.getStyleClass().add("save");
        saveButton.setOnAction(_ -> {});

        region = new StackPane(new IconRegion("icon", "revert"));
        region.getStyleClass().add("region");

        revertButton = new Button(resources.getString("button.revert"), region);
        revertButton.getStyleClass().add("revert");
        revertButton.setOnAction(_ -> {});

        bottomBox = new VBox(saveButton, revertButton);
        bottomBox.getStyleClass().add("bottom-box");

        getChildren().addAll(headerBox, scrollPane, bottomBox);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double spacing = 8;
        double headerHeight = headerBox.prefHeight(contentWidth);
        headerBox.resizeRelocate(contentX, contentY, contentWidth, headerHeight);
        double bottomHeight = bottomBox.prefHeight(contentWidth);
        bottomBox.resizeRelocate(contentX, contentY + contentHeight - bottomHeight, contentWidth, bottomHeight);
        scrollPane.resizeRelocate(contentX, contentY + headerHeight + spacing, contentWidth, contentHeight - headerHeight - bottomHeight - 2 * spacing);
    }
}
