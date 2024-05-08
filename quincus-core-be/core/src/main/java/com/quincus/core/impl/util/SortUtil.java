package com.quincus.core.impl.util;

import lombok.experimental.UtilityClass;
import se.sawano.java.text.AlphanumericComparator;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@UtilityClass
public class SortUtil {
    private static final AlphanumericComparator ALPHANUMERIC_COMPARATOR = new AlphanumericComparator(Locale.ENGLISH);

    public static <T> void sortListAlphanumerically(List<T> list, Function<T, String> toStringFunction) {
        list.sort(Comparator.comparing(toStringFunction, ALPHANUMERIC_COMPARATOR));
    }
}
