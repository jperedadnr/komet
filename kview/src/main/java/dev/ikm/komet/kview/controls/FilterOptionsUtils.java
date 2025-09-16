package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.temp.FxGet;
import dev.ikm.komet.framework.view.ObservableCoordinate;
import dev.ikm.komet.framework.view.ObservableLanguageCoordinate;
import dev.ikm.komet.framework.view.ObservableNavigationCoordinate;
import dev.ikm.komet.framework.view.ObservableStampCoordinate;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.StampService;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import dev.ikm.tinkar.terms.PatternFacade;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Subscription;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.ikm.tinkar.common.service.PrimitiveData.PREMUNDANE_TIME;

public class FilterOptionsUtils {

    public FilterOptionsUtils() {}

    private Subscription nodeSubscription;
    private Subscription viewSubscription;
    private Subscription optionSubscription;

    // pass down changes from View (typically the ParentView) to FilterOptions (typically the defaultFilterOptions)
    public void bindFilterOptionsToView(FilterOptions filterOptions, ObservableView observableView) {

        removeViewSubscriptions();

        ObservableView observableViewForFilterProperty = filterOptions.observableViewForFilterProperty();
        FilterOptions.MainFilterCoordinates mainCoordinates = filterOptions.getMainCoordinates();
        List<FilterOptions.LanguageFilterCoordinates> languageCoordinatesList = filterOptions.getLanguageCoordinatesList();

        // note: when any coordinate property from the View changes, the binding changes immediately the F.O. coordinate property,
        // and that change is propagated via subscription to the related Option, but also directly to the top F.O.
        // observableViewForFilter, so it is safe to add a listener just to this property to get notified of any change
        // in any of its coordinates (that is, options).
        for (ObservableCoordinate<?> observableCoordinate : observableView.getCompositeCoordinates()) {
            if (observableCoordinate instanceof ObservableNavigationCoordinate observableNavigationCoordinate) {

                // NAVIGATION
                viewSubscription = viewSubscription.and(observableNavigationCoordinate.navigationPatternsProperty().subscribe(nav -> {
                    if (nav != null) {
                        mainCoordinates.getNavigator().selectedOptions().clear();
                        mainCoordinates.getNavigator().selectedOptions().addAll(nav);
                    }
                    observableViewForFilterProperty.navigationCoordinate().navigationPatternsProperty().set(nav);
                }));

            } else if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {

                // STATUS
                viewSubscription = viewSubscription.and(observableStampCoordinate.allowedStatesProperty().subscribe(stateSet -> {
                    mainCoordinates.getStatus().selectedOptions().clear();
                    if (stateSet != null) {
                        mainCoordinates.getStatus().selectedOptions().addAll(stateSet.toEnumSet().stream().toList());
                    }
                    observableViewForFilterProperty.stampCoordinate().allowedStatesProperty().set(stateSet);
                }));

                // TIME
                viewSubscription = viewSubscription.and(observableStampCoordinate.timeProperty().subscribe(t -> {
                    mainCoordinates.getTime().selectedOptions().clear();
                    if (t != null) {
                        Long time = t.longValue();
                        if (!time.equals(Long.MAX_VALUE) && !time.equals(PREMUNDANE_TIME)) {
                            //FIXME the custom control doesn't support premundane yet
                            mainCoordinates.getTime().selectedOptions().addAll(String.valueOf(time));
                        } else if (time.equals(Long.MAX_VALUE)) {
                            mainCoordinates.getTime().selectedOptions().addAll(mainCoordinates.getTime().availableOptions().getFirst());
                        }
                    }
                    observableViewForFilterProperty.stampCoordinate().timeProperty().setValue(t);
                }));

                // MODULE
                viewSubscription = viewSubscription.and(observableStampCoordinate.moduleSpecificationsProperty().subscribe(m -> {
                    mainCoordinates.getModule().selectedOptions().clear();
                    if (m != null) {
                        mainCoordinates.getModule().selectedOptions().addAll(m);
                    }
                    observableViewForFilterProperty.stampCoordinate().moduleSpecificationsProperty().set(m);
                }));
                viewSubscription = viewSubscription.and(observableStampCoordinate.excludedModuleSpecificationsProperty().subscribe(e -> {
                    mainCoordinates.getModule().excludedOptions().clear();
                    if (e != null) {
                        mainCoordinates.getModule().excludedOptions().addAll(e);
                    }
                    observableViewForFilterProperty.stampCoordinate().excludedModuleSpecificationsProperty().set(e);
                }));

                // PATH
                viewSubscription = viewSubscription.and(observableStampCoordinate.pathConceptProperty().subscribe(path -> {
                    if (path != null) {
                        mainCoordinates.getPath().selectedOptions().clear();
                        mainCoordinates.getPath().selectedOptions().addAll(path);
                    }
                    observableViewForFilterProperty.stampCoordinate().pathConceptProperty().set(path);
                }));

            } else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate) {

                // LANGUAGE
                // todo: support more language coordinates for secondary+ languages

                FilterOptions.LanguageFilterCoordinates languageFilterCoordinates = languageCoordinatesList.getFirst();
                viewSubscription = viewSubscription.and(observableLanguageCoordinate.languageConceptProperty().subscribe(lang -> {
                    languageFilterCoordinates.getLanguage().selectedOptions().clear();
                    if (lang != null) {
                        languageFilterCoordinates.getLanguage().selectedOptions().addAll(lang);
                    }
                    // update dialect
                    languageFilterCoordinates.getDialect().selectedOptions().clear();
                    if (TinkarTerm.ENGLISH_LANGUAGE.equals(lang)) {
                        ObservableList<PatternFacade> list = observableLanguageCoordinate.dialectPatternPreferenceListProperty().get();
                        languageFilterCoordinates.getDialect().selectedOptions().addAll(list);
                        observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().set(list);
                    } else {
                        observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().clear();
                    }
                    observableViewForFilterProperty.languageCoordinates().getFirst().languageConceptProperty().set(lang);
                }));

                viewSubscription = viewSubscription.and(observableLanguageCoordinate.dialectPatternPreferenceListProperty().subscribe(list -> {
                    if (!TinkarTerm.ENGLISH_LANGUAGE.equals(observableLanguageCoordinate.languageConcept())) {
                        // ignore
                        return;
                    }
                    languageFilterCoordinates.getDialect().selectedOptions().clear();
                    if (list != null) {
                        languageFilterCoordinates.getDialect().selectedOptions().addAll(list);
                    }
                    observableViewForFilterProperty.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().set(list);
                }));

                viewSubscription = viewSubscription.and(observableLanguageCoordinate.descriptionTypePreferenceListProperty().subscribe(list -> {
                    languageFilterCoordinates.getDescriptionType().selectedOptions().clear();
                    if (list != null) {
                        languageFilterCoordinates.getDescriptionType().selectedOptions().addAll(list);
                    }
                    observableViewForFilterProperty.languageCoordinates().getFirst().descriptionTypePreferenceListProperty().set(list);
                }));
            }
        }
    }

    private void removeViewSubscriptions() {
        if (viewSubscription != null) {
            viewSubscription.unsubscribe();
        }
        viewSubscription = Subscription.EMPTY;
    }

    public void unbindFilterOptions() {
        if (nodeSubscription != null) {
            nodeSubscription.unsubscribe();
        }
    }

    // pass up changes from FilterOptions to view (typically the nodeView)
    public void bindViewToFilterOptions(FilterOptions filterOptions, ObservableView observableView) {
        unbindFilterOptions();
        addOptionSubscriptions(filterOptions);
        nodeSubscription = Subscription.EMPTY;
        // get parent menu settings
        for (ObservableCoordinate<?> observableCoordinate : observableView.getCompositeCoordinates()) {
            if (observableCoordinate instanceof ObservableNavigationCoordinate observableNavigationCoordinate) {
                // NAVIGATION
                nodeSubscription = nodeSubscription.and(
                    filterOptions.observableViewForFilterProperty().navigationCoordinate().navigationPatternsProperty().subscribe(nav ->
                        observableNavigationCoordinate.navigationPatternsProperty().set(nav)));

            } else if (observableCoordinate instanceof ObservableStampCoordinate observableStampCoordinate) {

                // STATUS
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().allowedStatesProperty().subscribe(state ->
                                observableStampCoordinate.allowedStatesProperty().set(state)));

                // TIME
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().timeProperty().subscribe(time ->
                                observableStampCoordinate.timeProperty().set(time.longValue())));

                // MODULE
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().moduleSpecificationsProperty().subscribe(m ->
                                observableStampCoordinate.moduleSpecificationsProperty().set(m)));

                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().excludedModuleSpecificationsProperty().subscribe(e ->
                                observableStampCoordinate.excludedModuleSpecificationsProperty().set(e)));

                // PATH
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().stampCoordinate().pathConceptProperty().subscribe(path ->
                                observableStampCoordinate.pathConceptProperty().set(path)));

            } else if (observableCoordinate instanceof ObservableLanguageCoordinate observableLanguageCoordinate) {

                // LANGUAGE
                // todo: more languages
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().languageConceptProperty().subscribe(lang ->
                                observableLanguageCoordinate.languageConceptProperty().set(lang)));
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().dialectPatternPreferenceListProperty().subscribe(list ->
                                observableLanguageCoordinate.dialectPatternPreferenceListProperty().set(list)));
                nodeSubscription = nodeSubscription.and(
                        filterOptions.observableViewForFilterProperty().languageCoordinates().getFirst().descriptionTypePreferenceListProperty().subscribe(list ->
                                observableLanguageCoordinate.descriptionTypePreferenceListProperty().set(list)));
            }
        }
    }

    private void removeOptionSubscriptions() {
        if (optionSubscription != null) {
            optionSubscription.unsubscribe();
        }
        optionSubscription = Subscription.EMPTY;
    }

    private void addOptionSubscriptions(FilterOptions filterOptions) {
        removeOptionSubscriptions();

        ObservableView observableViewForFilter = filterOptions.observableViewForFilterProperty();
        FilterOptions.MainFilterCoordinates mainCoordinates = filterOptions.getMainCoordinates();
        List<FilterOptions.LanguageFilterCoordinates> languageCoordinatesList = filterOptions.getLanguageCoordinatesList();

        // NAVIGATOR
        optionSubscription = mainCoordinates.getNavigator().selectedOptions().subscribe(() -> {
            ObservableList<PatternFacade> selectedOptions = mainCoordinates.getNavigator().selectedOptions();
            if (!selectedOptions.isEmpty()) {
                PatternFacade patternFacade = selectedOptions.getFirst();
//                System.out.println("from filter -> nav: " + patternFacade+ ", fo: " + filterOptions.hashCode());
                observableViewForFilter.navigationCoordinate().navigationPatternsProperty().clear();
                observableViewForFilter.navigationCoordinate().navigationPatternsProperty().add(patternFacade);
            }
        });

        // STATUS
        optionSubscription = optionSubscription.and(mainCoordinates.getStatus().selectedOptions().subscribe(() -> {
            List<State> stateList = mainCoordinates.getStatus().selectedOptions().stream().toList();
            observableViewForFilter.stampCoordinate().allowedStatesProperty().set(StateSet.of(stateList));
        }));

        // TIME
        optionSubscription = optionSubscription.and(mainCoordinates.getTime().selectedOptions().subscribe(() -> {
            ObservableList<String> selectedOptions = mainCoordinates.getTime().selectedOptions();
            if (!selectedOptions.isEmpty()) {
                String time = selectedOptions.getFirst();
                long t;
                try {
                    t = Long.parseLong(time);
                } catch (NumberFormatException e) {
                    t = Long.MAX_VALUE;
                }
                observableViewForFilter.stampCoordinate().timeProperty().set(t);
            }
        }));

        // MODULE
        optionSubscription = optionSubscription.and(mainCoordinates.getModule().selectedOptions().subscribe(() -> {
            Set<ConceptFacade> includedSet = new HashSet<>(mainCoordinates.getModule().selectedOptions());
            observableViewForFilter.stampCoordinate().moduleSpecificationsProperty().addAll(includedSet);
        }));
        optionSubscription = optionSubscription.and(mainCoordinates.getModule().excludedOptions().subscribe(() -> {
            Set<ConceptFacade> excludedSet = new HashSet<>(mainCoordinates.getModule().excludedOptions());
            observableViewForFilter.stampCoordinate().excludedModuleSpecificationsProperty().addAll(excludedSet);
        }));

        // PATH
        optionSubscription = optionSubscription.and(mainCoordinates.getPath().selectedOptions().subscribe(() -> {
            ObservableList<ConceptFacade> selectedOptions = mainCoordinates.getPath().selectedOptions();
            if (!selectedOptions.isEmpty()) {
                ConceptFacade conceptFacade = selectedOptions.getFirst();
                observableViewForFilter.stampCoordinate().pathConceptProperty().set(conceptFacade);
            }
        }));

        // LANGUAGE
        optionSubscription = optionSubscription.and(languageCoordinatesList.getFirst().getLanguage().selectedOptions().subscribe(() -> {
            ObservableList<EntityFacade> selectedOptions = languageCoordinatesList.getFirst().getLanguage().selectedOptions();
            if (!selectedOptions.isEmpty()) {
                EntityFacade entityFacade = selectedOptions.getFirst();
                observableViewForFilter.languageCoordinates().getFirst().languageConceptProperty().set((ConceptFacade) entityFacade);
            }
        }));
        optionSubscription = optionSubscription.and(
                languageCoordinatesList.getFirst().getDialect().selectedOptions().subscribe(() -> {
                    ObservableList<PatternFacade> selectedOptions = languageCoordinatesList.getFirst().getDialect().selectedOptions().stream()
                            .map(e -> (PatternFacade) e).collect(Collectors.toCollection(FXCollections::observableArrayList));
                    if (!selectedOptions.isEmpty()) {
                        observableViewForFilter.languageCoordinates().getFirst().dialectPatternPreferenceListProperty().setValue(selectedOptions);
                    }
                }));
        optionSubscription = optionSubscription.and(
                languageCoordinatesList.getFirst().getDescriptionType().selectedOptions().subscribe(() -> {
                    ObservableList<ConceptFacade> selectedOptions = languageCoordinatesList.getFirst().getDescriptionType().selectedOptions().stream()
                            .map(e -> (ConceptFacade) e).collect(Collectors.toCollection(FXCollections::observableArrayList));
                    if (!selectedOptions.isEmpty()) {
                        observableViewForFilter.languageCoordinates().getFirst().descriptionTypePreferenceListProperty().setValue(selectedOptions);
                    }
                }));
    }

    public static List<ZonedDateTime> getTimesInUse() {
        ImmutableLongList times = StampService.get().getTimesInUse().toReversed();
        return Arrays.stream(times.toArray())
                .filter(time -> time != PREMUNDANE_TIME)
                .boxed()
                .map(DateTimeUtil::epochToZonedDateTime)
                .toList();
    }

    private static int findNidForDescription(Navigator navigator, int nid, String description) {
        return navigator.getChildEdges(nid).stream()
                .filter(edge -> Entity.getFast(edge.destinationNid()).description().equals(description))
                .findFirst()
                .map(Edge::destinationNid)
                .orElseThrow();
    }

    public static List<EntityFacade> getDescendentsList(Navigator navigator, int parentNid, String description) {
        int nid = parentNid;
        for (String s : description.split(", ")) {
            nid = findNidForDescription(navigator, nid, s);
        }
        return navigator.getViewCalculator().descendentsOf(nid).intStream().boxed()
                .map(i -> (EntityFacade) Entity.getFast(i))
                .sorted()
                .toList();
    }

    public static <T> String getDescription(ViewCalculator viewCalculator, T t) {
        return switch (t) {
            case String value -> value;
            case State value -> viewCalculator == null ?
                    Entity.getFast(value.nid()).description() :
                    viewCalculator.getPreferredDescriptionTextOrNid(value.nid());
            case Long value -> String.valueOf(value);
            case EntityFacade value -> viewCalculator == null ?
                    value.description() : viewCalculator.getPreferredDescriptionTextOrNid(value);
            default -> throw new RuntimeException("Unsupported type: " + t.getClass().getName());
        };
    }
}
