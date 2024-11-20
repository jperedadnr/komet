/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.pattern;

import static dev.ikm.komet.kview.events.pattern.PatternDefinitionEvent.PATTERN_DEFINITION;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.DEFINITION_CONFIRMATION;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.IS_INVALID;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternDefinitionViewModel.MEANING_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternDefinitionViewModel.PURPOSE_ENTITY;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.STATE_MACHINE;

import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.controls.ComponentSetControl;
import dev.ikm.komet.kview.controls.ConceptControl;
import dev.ikm.komet.kview.events.pattern.PatternDefinitionEvent;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.mvvm.model.PatternDefinition;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternDefinitionViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternPropertiesViewModel;
import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.carlfx.axonic.StateMachine;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternDefinitionController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternDefinitionController.class);

    @InjectViewModel
    private PatternDefinitionViewModel patternDefinitionViewModel;

    @InjectViewModel
    private PatternPropertiesViewModel patternPropertiesViewModel;

    @FXML
    private Button doneButton;

    @FXML
    private ConceptControl purposeConceptControl;

    @FXML
    private ConceptControl meaningConceptControl;

    @FXML
    private ComponentSetControl meaningComponentSetControl;

    @FXML
    private VBox semanticOuterVBox;

    @FXML
    private void initialize() {
        ChangeListener<ConceptEntity<?>> fieldsValidationListener = (obs, oldValue, newValue) -> {
            patternDefinitionViewModel.validate();
            patternDefinitionViewModel.setPropertyValue(IS_INVALID, patternDefinitionViewModel.hasErrorMsgs());
        };

        BooleanProperty property = patternDefinitionViewModel.getProperty(IS_INVALID);
        doneButton.disableProperty().bind(property);

        purposeConceptControl.setOnSearchAction(e -> System.out.println("search: " + purposeConceptControl.getSearchText()));
        purposeConceptControl.setOnAddConceptAction(e -> {
            ConceptControl control = new ConceptControl();
            control.setTitle("Purpose");
            semanticOuterVBox.getChildren().add(semanticOuterVBox.getChildren().indexOf(purposeConceptControl), control);
        });
        purposeConceptControl.entityProperty().subscribe(entity ->
            patternDefinitionViewModel.setPropertyValue(PURPOSE_ENTITY, entity));

        meaningConceptControl.setOnSearchAction(e -> System.out.println("search: " + meaningConceptControl.getSearchText()));
        meaningConceptControl.setOnAddConceptAction(e -> {
            ConceptControl control = new ConceptControl();
            control.setTitle("Meaning");
            semanticOuterVBox.getChildren().add(semanticOuterVBox.getChildren().indexOf(meaningConceptControl), control);
        });
        meaningConceptControl.entityProperty().subscribe(entity ->
            patternDefinitionViewModel.setPropertyValue(MEANING_ENTITY, entity));

        ObjectProperty<ConceptEntity<?>> purposeProp = patternDefinitionViewModel.getProperty(PatternFieldsViewModel.PURPOSE_ENTITY);
        ObjectProperty<ConceptEntity<?>> meaningProp = patternDefinitionViewModel.getProperty(PatternFieldsViewModel.MEANING_ENTITY);
        purposeProp.addListener(fieldsValidationListener);
        meaningProp.addListener(fieldsValidationListener);
    }

    /**
     * cancel editing, close the panel
     * @param actionEvent
     */
    @FXML
    private void onCancel(ActionEvent actionEvent) {
        actionEvent.consume();
        //publish close env
        EvtBusFactory.getDefaultEvtBus().publish(patternDefinitionViewModel.getPropertyValue(PATTERN_TOPIC),
                new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        clearView();
    }

    /**
     * completing the action of adding a pattern definition
     * firing an event so that values will be saved in the viewModel
     * @param actionEvent
     */
    @FXML
    private void onDone(ActionEvent actionEvent) {
        actionEvent.consume();
        // save calls validate
        patternDefinitionViewModel.save();

        PatternDefinition patternDefinition = new PatternDefinition(
                patternDefinitionViewModel.getPropertyValue(PURPOSE_ENTITY),
                patternDefinitionViewModel.getPropertyValue(MEANING_ENTITY),
                null);

        StateMachine patternSM = patternPropertiesViewModel.getPropertyValue(STATE_MACHINE);
        patternSM.t("definitionsDone");

        // publish and event so that we can go to the definition confirmation screen
        EvtBusFactory.getDefaultEvtBus().publish(patternDefinitionViewModel.getPropertyValue(PATTERN_TOPIC),
                new PropertyPanelEvent(actionEvent.getSource(), DEFINITION_CONFIRMATION));

        // publish form submission data
        EvtBusFactory.getDefaultEvtBus().publish(patternDefinitionViewModel.getPropertyValue(PATTERN_TOPIC),
                new PatternDefinitionEvent(actionEvent.getSource(), PATTERN_DEFINITION, patternDefinition));
    }

    private void clearView() {
        purposeConceptControl.setEntity(null);
        meaningConceptControl.setEntity(null);
        patternDefinitionViewModel.save(true);
    }
}
