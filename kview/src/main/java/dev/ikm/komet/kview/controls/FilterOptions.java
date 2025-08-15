package dev.ikm.komet.kview.controls;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilterOptions implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.filter-options");

    public enum OPTION_ITEM {
        TYPE("type", ""),
        HEADER("header", ""),
        STATUS("status", "Status"),
        TIME("time", ""),
        MODULE("module", "Module"),
        PATH("path", "Path"),
        KIND_OF("kindof", ""),
        MEMBERSHIP("membership", ""),
        SORT_BY("sortby", "");

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

        public enum BUTTON {
            NONE(""),
            ALL(".button.all"),
            ANY(".button.any"),
            EXCLUDING(".button.excluding");

            private final String label;

            BUTTON(String label) {
                this.label = label;
            }

            public String getLabel() {
                return label;
            }
        }

        @Serial
        private static final long serialVersionUID = 0L;

        private final OPTION_ITEM item;
        private final String title;
        private final EnumSet<BUTTON> buttonType;
        private final boolean multiSelect;
        private final List<String> defaultOptions;
        private final List<String> availableOptions;
        private OptionSet optionSet;

        public record OptionSet(boolean any, List<String> selectedOptions, List<String> excludedOptions)
                implements Serializable {

            @Serial
            private static final long serialVersionUID = 0L;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                OptionSet option = (OptionSet) o;
                if (any != option.any) {
                    System.out.println("o1");
                    return false;
                }
                if (!compareLists(selectedOptions , option.selectedOptions)) {
                    System.out.println("o2");
                    return false;
                }
                if (!compareLists(excludedOptions , option.excludedOptions)) {
                    System.out.println("o3");
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                return Objects.hash(any, selectedOptions, excludedOptions);
            }

            @Override
            public String toString() {
                return (any ? "Any of: " : "All of: ") + selectedOptions +
                        (excludedOptions == null || excludedOptions.isEmpty() ? "" : " - " + excludedOptions);
            }

            public OptionSet copy() {
                return new OptionSet(any, new ArrayList<>(selectedOptions.stream().toList()),
                        excludedOptions == null ? null : new ArrayList<>(excludedOptions.stream().toList()));
            }
        }

        public Option(OPTION_ITEM item, String title, EnumSet<BUTTON> buttonType, boolean multiSelect, List<String> availableOptions) {
            this.item = item;
            this.title = title;
            this.buttonType = buttonType;
            this.multiSelect = multiSelect;
            this.availableOptions = availableOptions;
            this.defaultOptions = new ArrayList<>();

            optionSet = new OptionSet(false, new ArrayList<>(), null);
        }

        public String title() {
            return resources.getString(title);
        }

        public boolean isMultiSelectionAllowed() {
            return multiSelect;
        }

        public boolean isAll() {
            return buttonType.contains(BUTTON.ALL);
        }

        public boolean isAny() {
            return buttonType.contains(BUTTON.ANY);
        }

        public boolean isExcluding() {
            return buttonType.contains(BUTTON.EXCLUDING);
        }

        public Option copy() {
            Option option = new Option(item, title, buttonType, multiSelect, new ArrayList<>(availableOptions.stream().toList()));
            option.defaultOptions.addAll(new ArrayList<>(defaultOptions.stream().toList()));
            option.setOptionSet(optionSet.copy());
            return option;
        }

        public OPTION_ITEM item() {
            return item;
        }

        public List<String> defaultOptions() {
            return defaultOptions;
        }

        public List<String> availableOptions() {
            return availableOptions;
        }

        public EnumSet<BUTTON> buttonType() {
            return buttonType;
        }

        public OptionSet getOptionSet() {
            return optionSet;
        }

        public void setOptionSet(OptionSet optionSet) {
            this.optionSet = optionSet;
        }

        public boolean isAllSelected() {
            return optionSet.selectedOptions.size() == availableOptions.size();
        }

        public boolean hasExclusions() {
            return isExcluding() && optionSet.excludedOptions() != null && !optionSet.excludedOptions().isEmpty();
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
            if (!Objects.equals(buttonType, option.buttonType)) {
                System.out.println("e4");
                return false;
            }
            if (!compareLists(availableOptions, option.availableOptions)) {
                System.out.println("e5");
                return false;
            }
            if (!compareLists(defaultOptions, option.defaultOptions)) {
                System.out.println("e6: " + defaultOptions + ", vs " + option.defaultOptions);
                return false;
            }
            return Objects.equals(optionSet, option.optionSet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, title, buttonType, multiSelect, defaultOptions, availableOptions, optionSet);
        }

        @Override
        public String toString() {
            return "{" + item +
                    ", av=" + availableOptions +
                    ", def=" + defaultOptions +
                    ", set=" + optionSet +
                    '}';
        }
    }

    private final EnumSet<Option.BUTTON> noneSet = EnumSet.of(Option.BUTTON.NONE);
    private final EnumSet<Option.BUTTON> allSet = EnumSet.of(Option.BUTTON.ALL);
    private final EnumSet<Option.BUTTON> allExcludingSet = EnumSet.of(Option.BUTTON.ALL, Option.BUTTON.EXCLUDING);
    private final EnumSet<Option.BUTTON> allAnyExcludingSet = EnumSet.of(Option.BUTTON.ALL, Option.BUTTON.ANY, Option.BUTTON.EXCLUDING);

    private Option type;
    {
        List<String> typeOptions = new ArrayList<>(Stream.of(
                "type.option.concepts", "type.option.semantics")
                .map(resources::getString)
                .toList());
        type = new Option(OPTION_ITEM.TYPE, "type.title", allSet, true, typeOptions);
        type.setOptionSet(new Option.OptionSet(false, typeOptions, null));
    }

    private Option header = new Option(OPTION_ITEM.HEADER, "header.title", allSet, false, new ArrayList<>());

    // can we pass a lambda to default options here?
    private Option status = new Option(OPTION_ITEM.STATUS, "status.title", allSet, true, new ArrayList<>());

    private Option time;
    {
        List<String> timeOptions = new ArrayList<>(Stream.of("time.item1", "time.item2", "time.item3")
                .map(resources::getString)
                .toList());
        time = new Option(OPTION_ITEM.TIME, "time.title", noneSet, true, timeOptions);
        time.setOptionSet(new Option.OptionSet(false, timeOptions, new ArrayList<>()));
    }

    private Option module;
    {
        module = new Option(OPTION_ITEM.MODULE, "module.title", allAnyExcludingSet, true, new ArrayList<>());
        module.setOptionSet(new Option.OptionSet(false, new ArrayList<>(), new ArrayList<>()));
    }

    private Option path = new Option(OPTION_ITEM.PATH, "path.title", allSet, false, new ArrayList<>());

    private Option kindOf;
    {
        List<String> kindOfOptions = new ArrayList<>(Stream.of(
                "kindof.option.item1", "kindof.option.item2", "kindof.option.item3", "kindof.option.item4",
                        "kindof.option.item5", "kindof.option.item6", "kindof.option.item7", "kindof.option.item8",
                        "kindof.option.item9", "kindof.option.item10", "kindof.option.item11")
                .map(resources::getString)
                .toList());
        kindOf = new Option(OPTION_ITEM.KIND_OF, "kindof.title", allExcludingSet, true, kindOfOptions);
        kindOf.setOptionSet(new Option.OptionSet(false, kindOfOptions, new ArrayList<>()));
    }

    private Option membership;
    {
        List<String> membershipOptions = new ArrayList<>(Stream.of(
                "membership.option.member1", "membership.option.member2", "membership.option.member3",
                        "membership.option.member4", "membership.option.member5")
                .map(resources::getString)
                .toList());
        membership = new Option(OPTION_ITEM.MEMBERSHIP, "membership.title", allSet, true, membershipOptions);
        membership.setOptionSet(new Option.OptionSet(false, membershipOptions, null));
    }

    private Option sortBy;
    {
        List<String> sortByOptions = new ArrayList<>(Stream.of(
                        "sortby.option.relevant", "sortby.option.alphabetical", "sortby.option.groupedby")
                .map(resources::getString)
                .toList());
        sortBy = new Option(OPTION_ITEM.SORT_BY, "sortby.title", allSet, false, sortByOptions);
        sortBy.setOptionSet(new Option.OptionSet(false, sortByOptions, null));
    }

    private final List<Option> options;

    public FilterOptions() {
        options = new ArrayList<>(List.of(
                type, header, status, time, module, path, kindOf, membership, sortBy));
    }

    public Option getType() {
        return type;
    }

    public Option getHeader() {
        return header;
    }

    public Option getStatus() {
        return status;
    }

    public Option getTime() {
        return time;
    }

    public Option getModule() {
        return module;
    }

    public Option getPath() {
        return path;
    }

    public Option getKindOf() {
        return kindOf;
    }

    public Option getMembership() {
        return membership;
    }

    public Option getSortBy() {
        return sortBy;
    }

    public List<Option> getOptions() {
        return options;
    }

    public Option getOptionForItem(OPTION_ITEM item) {
        return options.stream()
                .filter(o -> o.item() == item)
                .findFirst()
                .orElseThrow();
    }

    public void setOptionForItem(OPTION_ITEM item, Option option) {
        switch (item) {
            case TYPE -> type = option;
            case HEADER -> header = option;
            case STATUS -> status = option;
            case TIME -> time = option;
            case MODULE -> module = option;
            case PATH -> path = option;
            case KIND_OF -> kindOf = option;
            case MEMBERSHIP -> membership = option;
            case SORT_BY -> sortBy = option;
        }
        options.clear();
        options.addAll(List.of(
                type, header, status, time, module, path, kindOf, membership, sortBy));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterOptions that = (FilterOptions) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(header, that.header) &&
                Objects.equals(status, that.status) &&
                Objects.equals(time, that.time) &&
                Objects.equals(module, that.module) &&
                Objects.equals(path, that.path) &&
                Objects.equals(kindOf, that.kindOf) &&
                Objects.equals(membership, that.membership) &&
                Objects.equals(sortBy, that.sortBy) &&
                Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                type, header, status, time, module, path, kindOf, membership, sortBy,
                options);
    }

    @Override
    public String toString() {
        return "FilterOptions{" +
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