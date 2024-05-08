package com.quincus.ext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ListUtilTest {

    @ParameterizedTest
    @MethodSource("provideListAndCriteria")
    void findIndex_listHasCriteria_shouldReturnPositiveInteger(List<String> sourceList, Predicate<String> predicate,
                                                               int expectedPosition) {
        int pos = ListUtil.findIndex(sourceList, predicate);
        assertThat(pos).isEqualTo(expectedPosition);
    }

    @Test
    void findIndex_nullList_shouldReturnNegativeOne() {
        int pos = ListUtil.findIndex(null, (i -> true));
        assertThat(pos).isEqualTo(-1);
    }

    @Test
    void findIndex_emptyList_shouldReturnNegativeOne() {
        List<String> list = Collections.emptyList();
        int pos = ListUtil.findIndex(list, null);
        assertThat(pos).isEqualTo(-1);
    }

    @Test
    void findIndex_nullPredicate_shouldReturnNegativeOne() {
        List<String> list = List.of("test-only");
        int pos = ListUtil.findIndex(list, null);
        assertThat(pos).isEqualTo(-1);
    }

    @Test
    void getUniqueEntries_uniqueStrings_shouldReturnListSameSize() {
        List<String> list = List.of("str1", "str2", "str3");
        List<String> result = ListUtil.getUniqueEntries(list);
        assertThat(result).isNotEmpty()
                .hasSize(list.size())
                .containsAll(list);
    }

    @Test
    void getUniqueEntries_duplicateStrings_shouldReturnUniqueList() {
        List<String> list = List.of("str1", "str2", "str3", "str2");
        List<String> result = ListUtil.getUniqueEntries(list);
        assertThat(result).isNotEmpty()
                .hasSize(list.size() - 1)
                .containsAll(list);
    }

    @Test
    void getUniqueEntries_emptyList_shouldReturnEmptyList() {
        assertThat(ListUtil.getUniqueEntries(Collections.emptyList())).isEmpty();
    }

    @Test
    void getUniqueEntries_nullArgument_shouldReturnNull() {
        assertThat(ListUtil.getUniqueEntries(null)).isNull();
    }

    private static Stream<Arguments> provideListAndCriteria() {
        List<String> sourceList = List.of("test1", "test2", "white");
        return Stream.of(
                Arguments.of(sourceList, (Predicate<String>) (s -> s.startsWith("test")), 0),
                Arguments.of(sourceList, (Predicate<String>) (s -> s.equals("test2")), 1),
                Arguments.of(sourceList, (Predicate<String>) (s -> s.equals("white")), 2),
                Arguments.of(sourceList, (Predicate<String>) (s -> s.equals("not-found")), -1)
        );
    }
}
