package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.LangFilterTitledPaneSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

public class LangFilterTitledPane extends FilterTitledPane {

    public LangFilterTitledPane() {
        getStyleClass().add("lang-filter-titled-pane");
    }

    // optionsProperty
    private final ObjectProperty<LanguageOptions> optionsProperty = new SimpleObjectProperty<>(this, "options", new LanguageOptions());
    public final ObjectProperty<LanguageOptions> optionsProperty() {
       return optionsProperty;
    }
    public final LanguageOptions getOptions() {
       return optionsProperty.get();
    }
    public final void setOptions(LanguageOptions value) {
        optionsProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new LangFilterTitledPaneSkin(this);
    }
}
