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


import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent;
import dev.ikm.komet.kview.events.pattern.PropertyPanelEvent;
import dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent;
import dev.ikm.komet.kview.mvvm.model.PatternField;
import dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

import static dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent.ADD_FIELD;
import static dev.ikm.komet.kview.events.pattern.PatternFieldsPanelEvent.EDIT_FIELD;
import static dev.ikm.komet.kview.events.pattern.PropertyPanelEvent.CLOSE_PANEL;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_CONTINUE_ADD_FIELDS;
import static dev.ikm.komet.kview.events.pattern.ShowPatternFormInBumpOutEvent.SHOW_CONTINUE_EDIT_FIELDS;
import static dev.ikm.komet.kview.mvvm.model.DataModelHelper.fetchFieldDefinitionDataTypes;
import static dev.ikm.komet.kview.mvvm.viewmodel.DescrNameViewModel.IS_INVALID;
import static dev.ikm.komet.kview.mvvm.viewmodel.FormViewModel.VIEW_PROPERTIES;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.*;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.*;

public class PatternFieldsController {

    private static final Logger LOG = LoggerFactory.getLogger(PatternFieldsController.class);

    @InjectViewModel
    private PatternFieldsViewModel patternFieldsViewModel;

    @FXML
    private Label addEditLabel;

    @FXML
    private TextField displayNameTextField;

    @FXML
    private Button doneButton;

    @FXML
    private KLComponentControl purposeComponentControl;

    @FXML
    private KLComponentControl meaningComponentControl;

    @FXML
    private ComboBox<Integer> fieldOrderComboBox = new ComboBox<>();

    @FXML
    private ComboBox<ConceptEntity> dataTypeComboBox = new ComboBox<>();


    @FXML
    private void initialize() {
        ChangeListener fieldsValidationListener = (obs, oldValue, newValue) -> {
            patternFieldsViewModel.validate();
            patternFieldsViewModel.setPropertyValue(IS_INVALID, patternFieldsViewModel.hasErrorMsgs());
        };

        StringProperty labelProperty = patternFieldsViewModel.getProperty(ADD_EDIT_LABEL);
        addEditLabel.textProperty().bind(labelProperty);

        BooleanProperty invalidProperty = patternFieldsViewModel.getProperty(IS_INVALID);
        doneButton.disableProperty().bind(invalidProperty);

        purposeComponentControl.setOnSearchAction(e -> LOG.info("search: {}", purposeComponentControl.getSearchText()));
        purposeComponentControl.setOnAddConceptAction(e -> addComponentControl("Purpose"));
        purposeComponentControl.entityProperty().subscribe((old, entity) ->
                patternFieldsViewModel.setPropertyValue(PURPOSE_ENTITY, entity));

        meaningComponentControl.setOnSearchAction(e -> LOG.info("search: {}", meaningComponentControl.getSearchText()));
        meaningComponentControl.setOnAddConceptAction(e -> addComponentControl("Meaning"));
        meaningComponentControl.entityProperty().subscribe((old, entity) ->
                patternFieldsViewModel.setPropertyValue(MEANING_ENTITY, entity));

        IntegerProperty totalExistingfields = patternFieldsViewModel.getProperty(TOTAL_EXISTING_FIELDS);
        // This had to be changed to ObjectProperty<Integer>. Else give a runtime casting exception.
        ObjectProperty<Integer> fieldOrderProp = patternFieldsViewModel.getProperty(FIELD_ORDER);
        ObservableList<Integer> fieldOrderOptions = patternFieldsViewModel.getObservableList(FIELD_ORDER_OPTIONS);

        StringProperty displayNameProp = patternFieldsViewModel.getProperty(DISPLAY_NAME);
        ObjectProperty<ConceptEntity> dataTypeProp = patternFieldsViewModel.getProperty(DATA_TYPE);
        ObjectProperty<EntityProxy> purposeProp = patternFieldsViewModel.getProperty(PURPOSE_ENTITY);
        ObjectProperty<EntityProxy> meaningProp = patternFieldsViewModel.getProperty(MEANING_ENTITY);

        displayNameTextField.textProperty().bindBidirectional(displayNameProp);
        dataTypeComboBox.valueProperty().bindBidirectional(dataTypeProp);
        fieldOrderComboBox.setItems(fieldOrderOptions); // Set the items in fieldOrder
        fieldOrderComboBox.valueProperty().bindBidirectional(fieldOrderProp);

        displayNameProp.addListener(fieldsValidationListener);
        dataTypeProp.addListener(fieldsValidationListener);
        purposeProp.subscribe(newVal -> purposeComponentControl.setEntity(newVal));
        purposeProp.addListener(fieldsValidationListener);
        meaningProp.subscribe(newVal -> meaningComponentControl.setEntity(newVal));
        meaningProp.addListener(fieldsValidationListener);

        totalExistingfields.subscribe(newVal -> loadFieldOrderOptions(newVal.intValue()));

        loadDataTypeComboBox();
        loadFieldOrderOptions(totalExistingfields.get());
    }

    private void loadFieldOrderOptions(int totalFields){
        fieldOrderComboBox.setConverter(new IntegerStringConverter());
        // get the available dropdown options initially list will be empty.
        ObservableList<Integer> fieldOrderOptions = patternFieldsViewModel.getObservableList(FIELD_ORDER_OPTIONS);
        // Clear list
        fieldOrderOptions.clear();
        // Create a stream of integers from 1 to (total field + 1)
        IntStream.rangeClosed(1, totalFields+1)
                .boxed() // Convert int to Integer
                .forEach(fieldOrderOptions::add);
        patternFieldsViewModel.setPropertyValue(FIELD_ORDER, (totalFields+1));
    }

    ViewProperties viewProperties;

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
    }

    private void loadDataTypeComboBox(){
      /*
          ViewCalculator viewCalculator = viewProperties.calculator();
          IntIdSet dataTypeFields = viewCalculator.descendentsOf(TinkarTerm.DISPLAY_FIELDS);
            Set<ConceptEntity> allDataTypes =
                    dataTypeFields.intStream()
                            .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                            .collect(Collectors.toSet());

            IntIdSet dataTypeDynamic = viewCalculator.descendentsOf(TinkarTerm.DYNAMIC_COLUMN_DATA_TYPES);

            allDataTypes.addAll(dataTypeDynamic.intStream()
                    .mapToObj(moduleNid -> (ConceptEntity) Entity.getFast(moduleNid))
                    .collect(Collectors.toSet()));

            dataTypeComboBox.setConverter((new StringConverter<ConceptEntity>() {
                @Override
                public String toString(ConceptEntity conceptEntity) {
                    Optional<String> stringOptional= viewCalculator.getFullyQualifiedNameText(conceptEntity.nid());
                    return stringOptional.orElse("");
                }

                @Override
                public ConceptEntity fromString(String s) {
                    return null;
                }
            }));
        */
        dataTypeComboBox.setConverter((new StringConverter<>() {
            @Override
            public String toString(ConceptEntity conceptEntity) {
                ViewCalculator viewCalculator = viewProperties.calculator();
                return viewCalculator.getRegularDescriptionText(conceptEntity).get();
            }

            @Override
            public ConceptEntity fromString(String s) {
                return null;
            }
        }));

        // TODO fetchFieldDefinitionDataTypes() method call returns hardcoded values.
        //  When right backend data is available below logic can be used?
        //  Set<ConceptEntity> allDataTypes = fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.DISPLAY_FIELDS.publicId()));
        //  AND/OR?
        //  allDataTypes.addAll(fetchDescendentsOfConcept(getViewProperties(), TinkarTerm.DYNAMIC_COLUMN_DATA_TYPES.publicId())));
        //  dataTypeComboBox.getItems().addAll(fetchFieldDefinitionDataTypes().stream()...).toList());

        dataTypeComboBox.getItems().addAll(fetchFieldDefinitionDataTypes().stream().sorted((entityFacade1, entityFacade2) -> {
            ViewCalculator viewCalculator = getViewProperties().calculator();
            return viewCalculator.getRegularDescriptionText(entityFacade1).get()
                            .compareToIgnoreCase(viewCalculator.getRegularDescriptionText(entityFacade2).get());
        }).toList());
    }

    private ViewProperties getViewProperties() {
        return patternFieldsViewModel.getPropertyValue(VIEW_PROPERTIES);
    }

    @FXML
    private void onCancel(ActionEvent actionEvent) {
        //publish close env
        EvtBusFactory.getDefaultEvtBus().publish(patternFieldsViewModel.getPropertyValue(PATTERN_TOPIC),
                new PropertyPanelEvent(actionEvent.getSource(), CLOSE_PANEL));
        clearView(actionEvent);
    }

    @FXML
    public void onDone(ActionEvent actionEvent) {
        actionEvent.consume();
        collectFormData();
        // save calls validate
        patternFieldsViewModel.save();
        if (patternFieldsViewModel.hasErrorMsgs()) {
            // when there are validators, we potentially will have errors
            return; // do not proceed.
        }

        //publish form submission data
        PatternField patternField = new PatternField(
                patternFieldsViewModel.getValue(DISPLAY_NAME),
                patternFieldsViewModel.getValue(DATA_TYPE),
                patternFieldsViewModel.getValue(PURPOSE_ENTITY),
                patternFieldsViewModel.getValue(MEANING_ENTITY),
                patternFieldsViewModel.getValue(COMMENTS),
                null);

        PatternField previousPatternField = patternFieldsViewModel.getValue(PREVIOUS_PATTERN_FIELD);

        // This logic can be improvised.
        EvtType<PatternFieldsPanelEvent> eventType = EDIT_FIELD;
        int totalFields = fieldOrderComboBox.getItems().size();
        if (previousPatternField == null) {
             eventType = ADD_FIELD;
            //publish event to get to the continue adding confirmation panel
            EvtBusFactory.getDefaultEvtBus().publish(patternFieldsViewModel.getPropertyValue(PATTERN_TOPIC),
                    new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_CONTINUE_ADD_FIELDS, totalFields));
        } else {
            EvtBusFactory.getDefaultEvtBus().publish(patternFieldsViewModel.getPropertyValue(PATTERN_TOPIC),
                    new ShowPatternFormInBumpOutEvent(actionEvent.getSource(), SHOW_CONTINUE_EDIT_FIELDS, totalFields));
        }

        EvtBusFactory.getDefaultEvtBus().publish(patternFieldsViewModel.getPropertyValue(PATTERN_TOPIC),
                new PatternFieldsPanelEvent(actionEvent.getSource(), eventType, patternField, previousPatternField, patternFieldsViewModel.getValue(FIELD_ORDER)));

        clearView(actionEvent);
    }

    private void collectFormData() {
        patternFieldsViewModel.setPropertyValue(DISPLAY_NAME, displayNameTextField.getText());
        //TODO collect all the data
    }

    private void clearView(ActionEvent actionEvent) {
        purposeComponentControl.setEntity(null);
        meaningComponentControl.setEntity(null);
        patternFieldsViewModel.setPropertyValue(FIELD_ORDER, 1);
        patternFieldsViewModel.setPropertyValue(DISPLAY_NAME, "");
        patternFieldsViewModel.setPropertyValue(DATA_TYPE, null);
        patternFieldsViewModel.setPropertyValue(PREVIOUS_PATTERN_FIELD, null);
        patternFieldsViewModel.save(true);
    }

    private void addComponentControl(String title) {
        LOG.info("addComponentControl: {}", title); // TODO
    }
}
