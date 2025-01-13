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
package dev.ikm.komet.app;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.storage.StorageService;
import dev.ikm.komet.app.util.FileUtils;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.ScreenInfo;
import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.framework.events.Subscriber;
import dev.ikm.komet.framework.graphics.LoadFonts;
import dev.ikm.komet.framework.preferences.PrefX;
import dev.ikm.komet.framework.window.WindowSettings;
import dev.ikm.komet.kview.events.CreateJournalEvent;
import dev.ikm.komet.kview.events.JournalTileEvent;
import dev.ikm.komet.kview.mvvm.view.journal.JournalController;
import dev.ikm.komet.kview.mvvm.view.journal.JournalViewFactory;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageController;
import dev.ikm.komet.kview.mvvm.view.landingpage.LandingPageViewFactory;
import dev.ikm.komet.navigator.graph.GraphNavigatorNodeFactory;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.komet.preferences.Preferences;
import dev.ikm.komet.search.SearchNodeFactory;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.common.service.TinkExecutor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static dev.ikm.komet.app.AppState.LOADING_DATA_SOURCE;
import static dev.ikm.komet.app.AppState.SHUTDOWN;
import static dev.ikm.komet.app.AppState.STARTING;
import static dev.ikm.komet.app.util.CssFile.KOMET_CSS;
import static dev.ikm.komet.app.util.CssFile.KVIEW_CSS;
import static dev.ikm.komet.app.util.CssUtils.addStylesheets;
import static dev.ikm.komet.kview.events.EventTopics.JOURNAL_TOPIC;
import static dev.ikm.komet.kview.events.JournalTileEvent.UPDATE_JOURNAL_TILE;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNAL_NAMES;
import static dev.ikm.komet.preferences.JournalWindowPreferences.JOURNAL_WINDOW;
import static dev.ikm.komet.preferences.JournalWindowPreferences.MAIN_KOMET_WINDOW;
import static dev.ikm.komet.preferences.JournalWindowSettings.CAN_DELETE;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_AUTHOR;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_HEIGHT;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_LAST_EDIT;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_TITLE;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_WIDTH;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_XPOS;
import static dev.ikm.komet.preferences.JournalWindowSettings.JOURNAL_YPOS;

/**
 * JavaFX App
 */
public class MobileApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MobileApp.class);
    public static final SimpleObjectProperty<AppState> state = new SimpleObjectProperty<>(STARTING);
    private static Stage primaryStage;

    private static Stage landingPageWindow;
    private final List<JournalController> journalControllersList = new ArrayList<>();

    private EvtBus kViewEventBus;

    public static void main(String[] args) {
        System.setProperty("com.gluonhq.attach.debug", "true");
        System.setProperty("charm-desktop-form", "tablet");
        System.setProperty("apple.laf.useScreenMenuBar", "false");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Komet");
        // https://stackoverflow.com/questions/42598097/using-javafx-application-stop-method-over-shutdownhook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Starting shutdown hook");
            PrimitiveData.save();
            PrimitiveData.stop();
            LOG.info("Finished shutdown hook");
        }));
        launch();
    }

    @Override
    public void init() throws Exception {
        LOG.info("Starting Komet");
        LoadFonts.load();
        String dataset = "assets/tinkar-starter-data-1.0.6-pb.zip";
        LOG.info("Loading dataset {}", MobileApp.class.getResource("/" + dataset));
        FileUtils fileUtils = new FileUtils(dataset);
        if (fileUtils.checkFileInResources(dataset)) {
            File datasetFile = fileUtils.getFileFromAssets(dataset);
            LOG.info("Loading tinkar-starter-data: {}", datasetFile.getAbsolutePath());
        }
        StorageService.create().flatMap(StorageService::getPrivateStorage).ifPresent(p -> {
            LOG.info("Got private path: {}", p);
            ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, p.toPath().toFile());
        });

        // get the instance of the event bus
        kViewEventBus = EvtBusFactory.getInstance(EvtBus.class);
        Subscriber<CreateJournalEvent> detailsSubscriber = evt -> {

            String journalName = evt.getWindowSettingsObjectMap().getValue(JOURNAL_TITLE);
            // Inspects the existing journal windows to see if it is already open
            // So that we do not open duplicate journal windows
            journalControllersList.stream()
                    .filter(journalController -> journalController.getTitle().equals(journalName))
                    .findFirst()
                    .ifPresentOrElse(
                            journalController -> journalController.windowToFront(), /* Window already launched now make window to the front (so user sees window) */
                            () -> launchJournalViewWindow(evt.getWindowSettingsObjectMap()) /* launch new Journal view window */
                    );
        };
        // subscribe to the topic
        kViewEventBus.subscribe(JOURNAL_TOPIC, CreateJournalEvent.class, detailsSubscriber);
    }

    private Dimension2D dimension2D;

    @Override
    public void start(Stage stage) {
        dimension2D = DisplayService.create().map(DisplayService::getDefaultDimensions).orElse(new Dimension2D(1200, 800));

        try {
            MobileApp.primaryStage = stage;
            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
                LOG.info("SYSTEM ERROR! ", e);
                e.printStackTrace();
                AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
            });

            FXMLLoader sourceLoader = new FXMLLoader(getClass().getResource("SelectDataSource.fxml"));
            BorderPane sourceRoot = sourceLoader.load();
            SelectDataSourceController selectDataSourceController = sourceLoader.getController();
            selectDataSourceController.getCancelButton().setOnAction(actionEvent -> {
                // Platform.exit()
            });
            Scene sourceScene = new Scene(sourceRoot, dimension2D.getWidth(), dimension2D.getHeight());
            addStylesheets(sourceScene, KOMET_CSS, KVIEW_CSS);

            stage.setScene(sourceScene);
            stage.setTitle("KOMET Startup");

            stage.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                ScreenInfo.mouseIsPressed(true);
                ScreenInfo.mouseWasDragged(false);
            });
            stage.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
                ScreenInfo.mouseIsPressed(false);
                ScreenInfo.mouseIsDragging(false);
            });
            stage.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
                ScreenInfo.mouseIsDragging(true);
                ScreenInfo.mouseWasDragged(true);
            });

            // Ensure app is shutdown gracefully. Once state changes it calls appStateChangeListener.
            stage.setOnCloseRequest(windowEvent -> {
                state.set(SHUTDOWN);
            });
            stage.show();
            state.set(AppState.SELECT_DATA_SOURCE);
            state.addListener(this::appStateChangeListener);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            Platform.exit();
        }
    }

    private void launchLandingPage() {
        if (landingPageWindow != null) {
            MobileApp.primaryStage = landingPageWindow;
            landingPageWindow.show();
            landingPageWindow.toFront();
            return;
        }
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node("main-komet-window");
        WindowSettings windowSettings = new WindowSettings(windowPreferences);

        Stage kViewStage = primaryStage;
        try {
            FXMLLoader landingPageLoader = LandingPageViewFactory.createFXMLLoader();
            BorderPane landingPageBorderPane = landingPageLoader.load();
            LandingPageController landingPageController = landingPageLoader.getController();
            Scene sourceScene = new Scene(landingPageBorderPane, dimension2D.getWidth(), dimension2D.getHeight());
            kViewStage.setScene(sourceScene);
            kViewStage.setTitle("Landing Page");

            kViewStage.setOnCloseRequest(windowEvent -> {
                // call shutdown method on the view
                state.set(SHUTDOWN);
                landingPageController.cleanup();
                landingPageWindow.close();
                landingPageWindow = null;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        landingPageWindow = kViewStage;
    }

    /**
     * When a user selects the menu option View/New Journal a new Stage Window is launched.
     * This method will load a navigation panel to be a publisher and windows will be connected (subscribed) to the activity stream.
     * @param journalWindowSettings if present will give the size and positioning of the journal window
     */
    private void launchJournalViewWindow(PrefX journalWindowSettings) {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences windowPreferences = appPreferences.node(MAIN_KOMET_WINDOW);

        WindowSettings windowSettings = new WindowSettings(windowPreferences);

        Stage journalStageWindow = new Stage();
        FXMLLoader journalLoader = JournalViewFactory.createFXMLLoader();
        JournalController journalController;
        try {
            BorderPane journalBorderPane = journalLoader.load();
            journalController = journalLoader.getController();
            Scene sourceScene = new Scene(journalBorderPane, dimension2D.getWidth(), dimension2D.getHeight());
            addStylesheets(sourceScene, KOMET_CSS, KVIEW_CSS);

            journalStageWindow.setScene(sourceScene);

            String journalName;
            if (journalWindowSettings != null) {
                // load journal specific window settings
                journalName = journalWindowSettings.getValue(JOURNAL_TITLE);
                journalStageWindow.setTitle(journalName);
                if (journalWindowSettings.getValue(JOURNAL_HEIGHT) != null) {
//                    journalStageWindow.setHeight(journalWindowSettings.getValue(JOURNAL_HEIGHT));
//                    journalStageWindow.setWidth(journalWindowSettings.getValue(JOURNAL_WIDTH));
//                    journalStageWindow.setX(journalWindowSettings.getValue(JOURNAL_XPOS));
//                    journalStageWindow.setY(journalWindowSettings.getValue(JOURNAL_YPOS));
//                    journalController.recreateConceptWindows(journalWindowSettings);
                }
            }

            journalStageWindow.setOnCloseRequest(windowEvent -> {
                saveJournalWindowsToPreferences();
                // call shutdown method on the view
                journalController.shutdown();
                journalControllersList.remove(journalController);
                // enable Delete menu option
                journalWindowSettings.setValue(CAN_DELETE, true);
                kViewEventBus.publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        journalController.setWindowView(windowSettings.getView());

        // Launch windows window pane inside journal view
        journalStageWindow.setOnShown(windowEvent -> {
            //TODO: Refactor factory constructor calls below to use PluggableService (make constructors private)
            KometNodeFactory navigatorNodeFactory = new GraphNavigatorNodeFactory();
            KometNodeFactory searchNodeFactory = new SearchNodeFactory();

            journalController.launchKometFactoryNodes(
                    journalWindowSettings.getValue(JOURNAL_TITLE),
                    navigatorNodeFactory,
                    searchNodeFactory);
            // load additional panels
            journalController.loadNextGenReasonerPanel();
            journalController.loadNextGenSearchPanel();
        });
        // disable the delete menu option for a Journal Card.
        journalWindowSettings.setValue(CAN_DELETE, false);
        kViewEventBus.publish(JOURNAL_TOPIC, new JournalTileEvent(this, UPDATE_JOURNAL_TILE, journalWindowSettings));
        journalControllersList.add(journalController);
        journalStageWindow.show();
    }

    private void saveJournalWindowsToPreferences() {
        KometPreferences appPreferences = KometPreferencesImpl.getConfigurationRootPreferences();
        KometPreferences journalPreferences = appPreferences.node(JOURNAL_WINDOW);

        // Non launched journal windows should be preserved.
        List<String> journalSubWindowFoldersFromPref = journalPreferences.getList(JOURNAL_NAMES);

        // launched (journal Controllers List) will overwrite existing window preferences.
        List<String> journalSubWindowFolders = new ArrayList<>(journalControllersList.size());
        for(JournalController controller : journalControllersList) {
            String journalSubWindowPrefFolder = controller.generateJournalDirNameBasedOnTitle();
            journalSubWindowFolders.add(journalSubWindowPrefFolder);

            KometPreferences journalSubWindowPreferences = appPreferences.node(JOURNAL_WINDOW +
                    File.separator + journalSubWindowPrefFolder);
            controller.saveConceptWindowPreferences(journalSubWindowPreferences);
            journalSubWindowPreferences.put(JOURNAL_TITLE, controller.getTitle());
            journalSubWindowPreferences.putDouble(JOURNAL_HEIGHT, controller.getHeight());
            journalSubWindowPreferences.putDouble(JOURNAL_WIDTH, controller.getWidth());
            journalSubWindowPreferences.putDouble(JOURNAL_XPOS, controller.getX());
            journalSubWindowPreferences.putDouble(JOURNAL_YPOS, controller.getY());
            journalSubWindowPreferences.put(JOURNAL_AUTHOR, LandingPageController.DEMO_AUTHOR);
            journalSubWindowPreferences.putLong(JOURNAL_LAST_EDIT, (LocalDateTime.now())
                    .atZone(ZoneId.systemDefault()).toEpochSecond());
            try {
                journalSubWindowPreferences.flush();
            } catch (BackingStoreException e) {
                throw new RuntimeException(e);
            }

        }

        // Make sure windows that are not summoned will not be deleted (not added to JOURNAL_NAMES)
        for (String x : journalSubWindowFolders){
            if (!journalSubWindowFoldersFromPref.contains(x)) {
                journalSubWindowFoldersFromPref.add(x);
            }
        }
        journalPreferences.putList(JOURNAL_NAMES, journalSubWindowFoldersFromPref);

        try {
            journalPreferences.flush();
            appPreferences.flush();
            appPreferences.sync();
        } catch (BackingStoreException e) {
            LOG.error("error writing journal window flag to preferences", e);
        }
    }

    @Override
    public void stop() {
        LOG.info("Stopping application\n\n###############\n\n");

        // close all journal windows
        journalControllersList.forEach(journalController -> journalController.close());
    }

    private void appStateChangeListener(ObservableValue<? extends AppState> observable, AppState oldValue, AppState newValue) {
        try {
            LOG.info("AppState: {}", newValue);
            switch (newValue) {
                case SELECTED_DATA_SOURCE -> {
                    Platform.runLater(() -> state.set(LOADING_DATA_SOURCE));
                    TinkExecutor.threadPool().submit(new LoadDataSourceTask(state));
                }
                case RUNNING -> launchLandingPage();
                case SHUTDOWN -> quit();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Platform.exit();
        }
    }


    private void quit() {
        //TODO: that this call will likely be moved into the landing page functionality
        saveJournalWindowsToPreferences();
        PrimitiveData.stop();
        Preferences.stop();
        Platform.exit();
    }

    public enum AppKeys {
        APP_INITIALIZED
    }
}