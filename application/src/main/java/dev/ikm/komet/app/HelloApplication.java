package dev.ikm.komet.app;

import dev.ikm.komet.framework.concurrent.TaskWrapper;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.framework.window.WindowSettings;
//import dev.ikm.komet.kview.controls.CoordinatesControl;
//import dev.ikm.komet.kview.controls.ConceptNavigatorUtils;
import dev.ikm.komet.kview.controls.ConceptNavigatorUtils;
import dev.ikm.komet.kview.controls.FilterOptions;
import dev.ikm.komet.kview.controls.FilterOptionsPopup;
import dev.ikm.komet.kview.controls.FilterTitledPane;
import dev.ikm.komet.kview.controls.LangFilterTitledPane;
import dev.ikm.komet.kview.fxutils.FXUtils;
import dev.ikm.komet.kview.mvvm.view.BasicController;
import dev.ikm.komet.kview.tasks.FilterMenuTask;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.komet.navigator.graph.ViewNavigator;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.service.DataUriOption;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.TinkExecutor;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static dev.ikm.komet.app.AppState.RUNNING;
import static dev.ikm.komet.app.AppState.SELECT_USER;
import static dev.ikm.komet.app.AppState.STARTING;
import static dev.ikm.komet.kview.controls.FilterOptions.OPTION_ITEM.MODULE;
import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;

public class HelloApplication extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(HelloApplication.class);


    @Override
    public void start(Stage stage) throws IOException {
        FilterOptionsPopup filterOptionsPopup = new FilterOptionsPopup(FilterOptionsPopup.FILTER_TYPE.SEARCH);

        filterOptionsPopup.filterOptionsProperty().subscribe((_, options) -> {
            System.out.println("New options: " + options + "\n-------");
        });

        Button showFilter = new Button("Show Filter");
        Button showCoordinates = new Button("Show Coordinates");
        HBox top = new HBox(20, showFilter, showCoordinates);
        top.setPadding(new Insets(10, 10, 10, 10));

        FilterTitledPane statusFilterTitledPane = new FilterTitledPane();
        statusFilterTitledPane.setTitle("STATUS");
        statusFilterTitledPane.setExpanded(false);

        FilterTitledPane moduleFilterTitledPane = new FilterTitledPane();
        moduleFilterTitledPane.setTitle("MODULE");
        moduleFilterTitledPane.setExpanded(false);

        LangFilterTitledPane langFilterTitledPane = new LangFilterTitledPane();
        langFilterTitledPane.setTitle("PRIMARY");
        filterOptionsPopup.navigatorProperty().subscribe((_, navigator) -> {
            if (navigator == null || navigator.getRootNids() == null || navigator.getRootNids().length == 0) {
                return;
            }
            int rootNid = navigator.getRootNids()[0];
            FilterOptions options = new FilterOptions();
            FilterOptions.Option language = options.getLanguageCoordinates(0).getLanguage();
            List<String> languageList = ConceptNavigatorUtils.getDescendentsList(navigator, rootNid, FilterOptions.OPTION_ITEM.LANGUAGE.getPath());
            language.availableOptions().addAll(languageList.stream().map(l -> l.substring(0, l.indexOf(" "))).toList());
            language.selectedOptions().add(language.availableOptions().getFirst());
//            langFilterTitledPane.setOptions(options);
        });

        Accordion langAccordion = new Accordion(langFilterTitledPane);

        TitledPane languageTitledPane = new TitledPane("LANGUAGE COORDINATES", langAccordion);
        languageTitledPane.getStyleClass().add("lang-titled-pane");

//        Accordion accordion = new Accordion();
//        accordion.getPanes().addAll(filterTitledPane, languageTitledPane);
        VBox accordion = new VBox(statusFilterTitledPane, languageTitledPane);
        accordion.getStyleClass().add("accordion");
        statusFilterTitledPane.expandedProperty().subscribe((_, expanded) -> {
            if (expanded) {
                accordion.getChildren().stream()
                        .filter(FilterTitledPane.class::isInstance)
                        .map(FilterTitledPane.class::cast)
                        .filter(tp -> tp != statusFilterTitledPane)
                        .forEach(tp -> tp.setExpanded(false));
            }
        });
        moduleFilterTitledPane.expandedProperty().subscribe((_, expanded) -> {
            if (expanded) {
                accordion.getChildren().stream()
                        .filter(FilterTitledPane.class::isInstance)
                        .map(FilterTitledPane.class::cast)
                        .filter(tp -> tp != moduleFilterTitledPane)
                        .forEach(tp -> tp.setExpanded(false));
            }
        });
//        accordion.getStyleClass().addAll("coordinates-accordion");

        ScrollPane scrollPane = new ScrollPane(accordion);
        scrollPane.setFitToWidth(true);

        BorderPane root = new BorderPane(scrollPane);
        root.getStyleClass().add("filter-options-popup");
        root.setTop(top);

        showFilter.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (filterOptionsPopup.isShowing()) {
                    e.consume();
                    filterOptionsPopup.hide();
                } else {
                    Bounds bounds = root.localToScreen(root.getLayoutBounds());
                    filterOptionsPopup.show(root.getScene().getWindow(), bounds.getMaxX(), bounds.getMinY());
                }
            }
        });
        root.heightProperty().subscribe(h -> filterOptionsPopup.setStyle("-popup-pref-height: " + h));
        showCoordinates.setOnAction(e -> {
//            CoordinatesControl coordinatesControl = new CoordinatesControl(CoordinatesControl.COORDINATES_TYPE.JOURNAL);
//            VBox center = new VBox(coordinatesControl);
//            VBox.setVgrow(coordinatesControl, Priority.ALWAYS);
//            root.setCenter(center);
        });
        Scene scene = new Scene(root, 320, 1000);
        scene.getStylesheets().add(FilterOptions.class.getResource("filter-options-popup.css").toExternalForm());
        scene.getStylesheets().add(BasicController.class.getResource("kview.css").toExternalForm());
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        filterOptionsPopup.navigatorProperty().subscribe((_, n) -> {
            int rootNid = n.getRootNids()[0];
            List<String> descendentsList = ConceptNavigatorUtils.getDescendentsList(n, rootNid, MODULE.getPath());
            FilterOptions filterOptions = new FilterOptions();
            filterOptions.getMainCoordinates().getModule().availableOptions().addAll(descendentsList);
            filterOptions.getMainCoordinates().getModule().selectedOptions().addAll(descendentsList);
            moduleFilterTitledPane.setOption(filterOptions.getMainCoordinates().getModule());
            accordion.getChildren().add(1, moduleFilterTitledPane);
        });
        LoadDataset.open(viewProperties -> {
            TinkExecutor.threadPool().execute(TaskWrapper.make(new FilterMenuTask(viewProperties),
                    (FilterOptions fo) ->
                            FXUtils.runOnFxThread(() -> {
                                filterOptionsPopup.setInitialFilterOptions(fo);
                            })
            ));
        }, filterOptionsPopup::setNavigator);
    }

    public static void main(String[] args) {
        launch();
    }

    private static class LoadDataset {

        private static final SimpleObjectProperty<AppState> state = new SimpleObjectProperty<>(STARTING);

        private LoadDataset() {}

        static void open(Consumer<ViewProperties> propertiesConsumer, Consumer<Navigator> navigatorConsumer) {
            state.subscribe(s -> {
                if (SELECT_USER == s || RUNNING == s) {
                    KometPreferences windowPreferences = KometPreferencesImpl.getConfigurationRootPreferences().node(MAIN_KOMET_WINDOW);
                    ObservableViewNoOverride view = new WindowSettings(windowPreferences).getView();
                    ViewProperties viewProperties = view.makeOverridableViewProperties();
                    propertiesConsumer.accept(viewProperties);
                    Navigator navigator = new ViewNavigator(viewProperties.nodeView());
                    navigatorConsumer.accept(navigator);
                }
            });

            // Load dataset
            PrimitiveData.getControllerOptions().stream()
                    .filter(dsc -> "Open SpinedArrayStore".equals(dsc.controllerName()))
                    .findFirst()
                    .ifPresent(controller -> {
                        PrimitiveData.setController(controller);
                        List<DataUriOption> list = controller.providerOptions();
                        list.stream()
                            .filter(l -> "tinkar-example-data-1.2.0+1.1.5-reasoned-sa".equals(l.toFile().getName()))
                            .findFirst()
                            .ifPresent(p -> {
                                controller.setDataUriOption(p);
                                LoadDataSourceTask task = new LoadDataSourceTask(state);
                                TinkExecutor.threadPool().submit(task);
                            });
                    });

        }
    }
}