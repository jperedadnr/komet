package dev.ikm.komet.kview.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

public class DetailsToolbar extends HBox {

    private final Button coordinateButton;
    private final Button saveButton;
    private final Button shareButton;
    private final Button favoriteButton;
    private final ToggleButton reasonerToggleButton;
    private final Button listViewButton;
    private final ToggleButton timelineToggleButton;
    private final ToggleButton propertiesToggleButton;
    private final Button closeButton;

    public DetailsToolbar() {
        coordinateButton = new Button(null, new IconRegion("icon", "coordinate"));

        saveButton = new Button(null, new IconRegion("icon", "save"));
        saveButton.visibleProperty().bind(saveButtonVisibleProperty());
        saveButton.managedProperty().bind(saveButtonVisibleProperty());
        saveButton.disableProperty().bind(saveButtonDisabledProperty());

        shareButton = new Button(null, new IconRegion("icon", "share-concept"));
        favoriteButton = new Button(null, new IconRegion("icon", "favorite-concept"));

        reasonerToggleButton = new ToggleButton(null, new IconRegion("icon", "reasoner"));

        listViewButton = new Button(null, new IconRegion("icon", "kview-listview"));
        timelineToggleButton = new ToggleButton(null, new IconRegion("icon", "timeline-icon"));
        Region span = new Region();
        HBox.setHgrow(span, Priority.ALWAYS);

        propertiesToggleButton = new ToggleButton("PROPERTIES", new SwitchButton());
        propertiesToggleButton.getStyleClass().add("toggle-switch-button");

        closeButton = new Button(null, new IconRegion("icon", "close-window"));

        getChildren().addAll(coordinateButton, new Divider(),
                saveButton, shareButton, favoriteButton, reasonerToggleButton, new Divider(),
                listViewButton, timelineToggleButton, span,
                propertiesToggleButton, new Divider(),
                closeButton);

        getStyleClass().addAll("details-toolbar", "concept-header-control", "draggable-region");
    }

    // coordinateButtonEventHandlerProperty
    private final ObjectProperty<EventHandler<ActionEvent>> coordinateButtonEventHandlerProperty = new SimpleObjectProperty<>(this, "coordinateButtonEventHandler") {
        @Override
        protected void invalidated() {
            coordinateButton.setOnAction(get());
        }
    };
    public final ObjectProperty<EventHandler<ActionEvent>> coordinateButtonEventHandlerProperty() {
        return coordinateButtonEventHandlerProperty;
    }
    public final EventHandler<ActionEvent> getCoordinateButtonEventHandler() {
        return coordinateButtonEventHandlerProperty.get();
    }
    public final void setCoordinateButtonEventHandler(EventHandler<ActionEvent> actionEventHandler) {
        coordinateButton.setOnAction(actionEventHandler);
    }

    // saveButtonVisibleProperty
    private final BooleanProperty saveButtonVisibleProperty = new SimpleBooleanProperty(this, "saveButtonVisible");
    public final BooleanProperty saveButtonVisibleProperty() {
        return saveButtonVisibleProperty;
    }
    public final boolean isSaveButtonVisible() {
        return saveButtonVisibleProperty.get();
    }
    public final void setSaveButtonVisible(boolean value) {
        saveButtonVisibleProperty.set(value);
    }

    // saveButtonDisabledProperty
    private final BooleanProperty saveButtonDisabledProperty = new SimpleBooleanProperty(this, "saveButtonDisabled");
    public final BooleanProperty saveButtonDisabledProperty() {
        return saveButtonDisabledProperty;
    }
    public final boolean isSaveButtonDisabled() {
        return saveButtonDisabledProperty.get();
    }
    public final void setSaveButtonDisabled(boolean value) {
        saveButtonDisabledProperty.set(value);
    }

    // saveButtonEventHandlerProperty
    private final ObjectProperty<EventHandler<ActionEvent>> saveButtonEventHandlerProperty = new SimpleObjectProperty<>(this, "saveButtonEventHandler") {
        @Override
        protected void invalidated() {
            saveButton.setOnAction(get());
        }
    };
    public final ObjectProperty<EventHandler<ActionEvent>> saveButtonEventHandlerProperty() {
        return saveButtonEventHandlerProperty;
    }
    public final EventHandler<ActionEvent> getSaveButtonEventHandler() {
        return saveButtonEventHandlerProperty.get();
    }
    public final void setSaveButtonEventHandler(EventHandler<ActionEvent> value) {
        saveButtonEventHandlerProperty.set(value);
    }

    // shareButtonEventHandlerProperty
    private final ObjectProperty<EventHandler<ActionEvent>> shareButtonEventHandlerProperty = new SimpleObjectProperty<>(this, "shareButtonEventHandler") {
        @Override
        protected void invalidated() {
            shareButton.setOnAction(get());
        }
    };
    public final ObjectProperty<EventHandler<ActionEvent>> shareButtonEventHandlerProperty() {
        return shareButtonEventHandlerProperty;
    }
    public final EventHandler<ActionEvent> getShareButtonEventHandler() {
        return shareButtonEventHandlerProperty.get();
    }
    public final void setShareButtonEventHandler(EventHandler<ActionEvent> value) {
        shareButtonEventHandlerProperty.set(value);
    }

    // favoriteButtonEventHandlerProperty
    private final ObjectProperty<EventHandler<ActionEvent>> favoriteButtonEventHandlerProperty = new SimpleObjectProperty<>(this, "favoriteButtonEventHandler") {
        @Override
        protected void invalidated() {
            favoriteButton.setOnAction(get());
        }
    };
    public final ObjectProperty<EventHandler<ActionEvent>> favoriteButtonEventHandlerProperty() {
        return favoriteButtonEventHandlerProperty;
    }
    public final EventHandler<ActionEvent> getFavoriteButtonEventHandler() {
        return favoriteButtonEventHandlerProperty.get();
    }
    public final void setFavoriteButtonEventHandler(EventHandler<ActionEvent> value) {
        favoriteButtonEventHandlerProperty.set(value);
    }

    // reasonerToggleButtonEventHandlerProperty
    private final ObjectProperty<EventHandler<ActionEvent>> reasonerToggleButtonEventHandlerProperty = new SimpleObjectProperty<>(this, "reasonerToggleButtonEventHandler") {
        @Override
        protected void invalidated() {
            reasonerToggleButton.setOnAction(get());
        }
    };
    public final ObjectProperty<EventHandler<ActionEvent>> reasonerToggleButtonEventHandlerProperty() {
        return reasonerToggleButtonEventHandlerProperty;
    }
    public final EventHandler<ActionEvent> getReasonerToggleButtonEventHandler() {
        return reasonerToggleButtonEventHandlerProperty.get();
    }
    public final void setReasonerToggleButtonEventHandler(EventHandler<ActionEvent> value) {
        reasonerToggleButtonEventHandlerProperty.set(value);
    }

    public final ContextMenu getReasonerToggleButtonContextMenu() {
        if (reasonerToggleButton.getContextMenu() == null) {
            ContextMenu contextMenu = new ContextMenu();
            reasonerToggleButton.setContextMenu(contextMenu);
        }
        return reasonerToggleButton.getContextMenu();
    }

    // listViewButtonEventHandlerProperty
    private final ObjectProperty<EventHandler<ActionEvent>> listViewButtonEventHandlerProperty = new SimpleObjectProperty<>(this, "listViewButtonEventHandler") {
        @Override
        protected void invalidated() {
            listViewButton.setOnAction(get());
        }
    };
    public final ObjectProperty<EventHandler<ActionEvent>> listViewButtonEventHandlerProperty() {
        return listViewButtonEventHandlerProperty;
    }
    public final EventHandler<ActionEvent> getListViewButtonEventHandler() {
        return listViewButtonEventHandlerProperty.get();
    }
    public final void setListViewButtonEventHandler(EventHandler<ActionEvent> value) {
        listViewButtonEventHandlerProperty.set(value);
    }

    // timelineToggleButtonEventHandlerProperty
    private final ObjectProperty<EventHandler<ActionEvent>> timelineToggleButtonEventHandlerProperty = new SimpleObjectProperty<>(this, "timelineToggleButtonEventHandler") {
        @Override
        protected void invalidated() {
            timelineToggleButton.setOnAction(get());
        }
    };
    public final ObjectProperty<EventHandler<ActionEvent>> timelineToggleButtonEventHandlerProperty() {
        return timelineToggleButtonEventHandlerProperty;
    }
    public final EventHandler<ActionEvent> getTimelineToggleButtonEventHandler() {
        return timelineToggleButtonEventHandlerProperty.get();
    }
    public final void setTimelineToggleButtonEventHandler(EventHandler<ActionEvent> value) {
        timelineToggleButtonEventHandlerProperty.set(value);
    }

    // propertiesToggleButtonEventHandlerProperty
    private final ObjectProperty<EventHandler<ActionEvent>> propertiesToggleButtonEventHandlerProperty = new SimpleObjectProperty<>(this, "propertiesToggleButtonEventHandler") {
        @Override
        protected void invalidated() {
            propertiesToggleButton.setOnAction(get());
        }
    };
    public final ObjectProperty<EventHandler<ActionEvent>> propertiesToggleButtonEventHandlerProperty() {
        return propertiesToggleButtonEventHandlerProperty;
    }
    public final EventHandler<ActionEvent> getPropertiesToggleButtonEventHandler() {
        return propertiesToggleButtonEventHandlerProperty.get();
    }
    public final void setPropertiesToggleButtonEventHandler(EventHandler<ActionEvent> value) {
        propertiesToggleButtonEventHandlerProperty.set(value);
    }

    // propertiesToggleButtonSelectedProperty
    public final BooleanProperty propertiesToggleButtonSelectedProperty() {
       return propertiesToggleButton.selectedProperty();
    }
    public final boolean isPropertiesToggleButtonSelected() {
       return propertiesToggleButton.isSelected();
    }
    public final void setPropertiesToggleButtonSelected(boolean value) {
        propertiesToggleButton.setSelected(value);
    }

    // closeButtonEventHandlerProperty
    private final ObjectProperty<EventHandler<ActionEvent>> closeButtonEventHandlerProperty = new SimpleObjectProperty<>(this, "closeButtonEventHandler") {
        @Override
        protected void invalidated() {
            closeButton.setOnAction(get());
        }
    };
    public final ObjectProperty<EventHandler<ActionEvent>> closeButtonEventHandlerProperty() {
        return closeButtonEventHandlerProperty;
    }
    public final EventHandler<ActionEvent> getCloseButtonEventHandler() {
        return closeButtonEventHandlerProperty.get();
    }
    public final void setCloseButtonEventHandler(EventHandler<ActionEvent> value) {
        closeButtonEventHandlerProperty.set(value);
    }

    private static class IconRegion extends Region {

        public IconRegion(String... styleClasses) {
            getStyleClass().addAll(styleClasses);
        }
    }

    private static class Divider extends Region {

        public Divider() {
            getStyleClass().add("vertical-divider");
        }
    }

    private static class SwitchButton extends Group {

        public SwitchButton() {
            getStyleClass().add("switch-button");
            Rectangle rectangle = new Rectangle(32, 20);
            rectangle.getStyleClass().add("toggle-switch-body");
            Ellipse ellipse = new Ellipse(10, 10, 8, 8);
            ellipse.getStyleClass().add("property-toggle-switch");
            getChildren().addAll(rectangle, ellipse);
        }
    }

}
