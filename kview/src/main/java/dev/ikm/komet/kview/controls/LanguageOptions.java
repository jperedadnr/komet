package dev.ikm.komet.kview.controls;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageOptions implements Serializable {

    public static final int MAX_LANGUAGE_ITEMS = 12;

    @Serial
    private final static long serialVersionUID = 1L;

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");

    public enum OPTION_ITEM {
        LANGUAGE("language", "Model concept, Tinkar Model concept, Language"),
        DIALECT("dialect", ""),
        PATTERN("pattern", ""),
        DESCRIPTION_TYPE("description", "");

        private final String name;
        private final String path;

        OPTION_ITEM(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }

    public static final class Option implements Serializable {
        @Serial
        private static final long serialVersionUID = 0L;

        private final OPTION_ITEM item;
        private final String title;
        private final boolean multiSelect;
        private final List<String> defaultOptions;
        private final List<String> availableOptions;
        private final List<String> selectedOptions;

        public Option(OPTION_ITEM item, String title, boolean multiSelect,
                      List<String> defaultOptions, List<String> availableOptions, List<String> selectedOptions) {
            this.item = item;
            this.title = title;
            this.multiSelect = multiSelect;
            this.defaultOptions = defaultOptions;
            this.availableOptions = availableOptions;
            this.selectedOptions = selectedOptions;
        }

        public OPTION_ITEM getItem() {
            return item;
        }

        public String getTitle() {
            return title;
        }

        public boolean isMultiSelect() {
            return multiSelect;
        }

        public List<String> getDefaultOptions() {
            return defaultOptions;
        }

        public List<String> getAvailableOptions() {
            return availableOptions;
        }

        public List<String> getSelectedOptions() {
            return selectedOptions;
        }

        public Option copy() {
            return new Option(item, title, multiSelect,
                    new ArrayList<>(defaultOptions.stream().toList()),
                    new ArrayList<>(availableOptions.stream().toList()),
                    new ArrayList<>(selectedOptions.stream().toList()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
            if (item != option.item) {
                System.out.println("e1");
                return false;
            }
            if (!Objects.equals(title, option.title)) {
                System.out.println("e2");
                return false;
            }
            if (multiSelect != option.multiSelect) {
                System.out.println("e3");
                return false;
            }
            if (!compareLists(defaultOptions, option.defaultOptions)) {
                System.out.println("e6: " + defaultOptions + ", vs " + option.defaultOptions);
                return false;
            }
            if (!compareLists(availableOptions, option.availableOptions)) {
                System.out.println("e5");
                return false;
            }
            if (!compareLists(selectedOptions, option.selectedOptions)) {
                System.out.println("e5");
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, title, multiSelect, defaultOptions, availableOptions, selectedOptions);
        }

        @Override
        public String toString() {
            return "{" + item +
                    ", av=" + availableOptions +
                    ", def=" + defaultOptions +
                    ", set=" + selectedOptions +
                    '}';
        }
    }

    private Option language = new Option(OPTION_ITEM.LANGUAGE, "language.option.title", false,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    private Option dialect;
    {
        List<String> dialectOptions = new ArrayList<>(Stream.of(
                        "dialect.option.item1", "dialect.option.item2",
                        "dialect.option.item3", "dialect.option.item4", "dialect.option.item5")
                .map(resources::getString)
                .toList());
        dialect = new Option(OPTION_ITEM.DIALECT, "dialect.option.title", false,
                dialectOptions, dialectOptions, new ArrayList<>());
    }

    private Option pattern;
    {
        List<String> patternOptions = new ArrayList<>(Stream.of(
                        "pattern.option.item1", "pattern.option.item2",
                        "pattern.option.item3", "pattern.option.item4")
                .map(resources::getString)
                .toList());
        pattern = new Option(OPTION_ITEM.PATTERN, "pattern.option.title", false,
                new ArrayList<>(List.of(patternOptions.getFirst())), patternOptions, new ArrayList<>());
    }

    private Option descriptionType;
    {
        List<String> descriptionTypeOptions = new ArrayList<>(Stream.of(
                        "description.option.fqn", "description.option.preferred", "description.option.regular",
                        "description.option.preferredfqn", "description.option.regularfqn")
                .map(resources::getString)
                .toList());
        descriptionType = new Option(OPTION_ITEM.DESCRIPTION_TYPE, "description.option.title", true,
                descriptionTypeOptions, descriptionTypeOptions, new ArrayList<>());
    }

    private final List<Option> options;

    public LanguageOptions() {
        options = new ArrayList<>(List.of(
                language, dialect, pattern, descriptionType));
    }

    public Option getLanguage() {
        return language;
    }

    public Option getDialect() {
        return dialect;
    }

    public Option getPattern() {
        return pattern;
    }

    public Option getDescriptionType() {
        return descriptionType;
    }

    public List<Option> getOptions() {
        return options;
    }

    public Option getOptionForItem(OPTION_ITEM item) {
        return options.stream()
                .filter(o -> o.item == item)
                .findFirst()
                .orElseThrow();
    }

    public void setOptionForItem(OPTION_ITEM item, Option option) {
        switch (item) {
            case LANGUAGE -> language = option;
            case DIALECT -> dialect = option;
            case PATTERN -> pattern = option;
            case DESCRIPTION_TYPE -> descriptionType = option;
        }
        options.clear();
        options.addAll(List.of(
                language, dialect, pattern, descriptionType));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LanguageOptions that = (LanguageOptions) o;
        return Objects.equals(language, that.language) &&
                Objects.equals(dialect, that.dialect) &&
                Objects.equals(pattern, that.pattern) &&
                Objects.equals(descriptionType, that.descriptionType) &&
                Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                language, dialect, pattern, descriptionType,
                options);
    }

    @Override
    public String toString() {
        return "LanguageOptions{" +
                options.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")) +
                "}";
    }

    private static boolean compareLists(List<String> list1, List<String> list2) {
        if (list1 == null && list2 != null) {
            System.out.println("r1");
            return false;
        }
        if (list1 != null && list2 == null) {
            System.out.println("r2");
            return false;
        }
        if (list1 != null && list1.size() != list2.size()) {
            System.out.println("r3");
            return false;
        }
        if (list1 != null && !list1.stream().sorted().toList().equals(list2.stream().sorted().toList())) {
            System.out.println("r4");
            return false;
        }
        return true;
    }

}
