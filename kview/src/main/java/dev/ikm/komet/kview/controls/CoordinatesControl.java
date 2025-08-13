package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.CoordinatesControlSkin;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class CoordinatesControl extends Control {

    public enum COORDINATES_TYPE {
        JOURNAL,
        CONCEPT
    }

    private final COORDINATES_TYPE coordinatesType;

    public CoordinatesControl(COORDINATES_TYPE coordinatesType) {
        this.coordinatesType = coordinatesType;

        getStyleClass().add("coordinates-control");
        getStylesheets().add(getUserAgentStylesheet());
    }

    public COORDINATES_TYPE getCoordinatesType() {
        return coordinatesType;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CoordinatesControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return CoordinatesControl.class.getResource("coordinates-control.css").toExternalForm();
    }
}
