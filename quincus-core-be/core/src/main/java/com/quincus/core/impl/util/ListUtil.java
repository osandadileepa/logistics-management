package com.quincus.core.impl.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@NoArgsConstructor(access = AccessLevel.NONE)
public class ListUtil {
    public static <T> int findIndex(List<T> list, Predicate<T> predicate) {
        if (list == null || predicate == null) {
            return -1;
        }
        OptionalInt indexOpt = IntStream.range(0, list.size())
                .filter(i -> predicate.test(list.get(i)))
                .findFirst();
        return indexOpt.orElse(-1);
    }

    public static <T> List<T> getUniqueEntries(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        Set<T> uniqueEntries = new HashSet<>(list);
        return new ArrayList<>(uniqueEntries);
    }
}
