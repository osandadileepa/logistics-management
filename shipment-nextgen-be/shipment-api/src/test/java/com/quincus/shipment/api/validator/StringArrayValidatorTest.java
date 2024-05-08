package com.quincus.shipment.api.validator;

import com.quincus.shipment.api.validator.constraint.ValidStringArray;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StringArrayValidatorTest {
    private static final String MESSAGE = "validation message";
    private StringArrayValidator validator;
    private ConstraintValidatorContext context;

    @Test
    void testSize_ValidSize() throws NoSuchFieldException {
        Data1 data1 = new Data1();
        data1.setValues(new String[]{"somevalue"});
        ValidStringArray annotation = Data1.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data1.values, context)).isTrue();
    }

    @Test
    void testSize_InvalidMaxSize() throws NoSuchFieldException {
        Data1 data1 = new Data1();
        data1.setValues(new String[]{"somevalue", "somevalue2", "somevalue3"});
        ValidStringArray annotation = Data1.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data1.values, context)).isFalse();
    }

    @Test
    void testSize_InvalidMinSize() throws NoSuchFieldException {
        Data1 data1 = new Data1();
        data1.setValues(new String[]{});
        ValidStringArray annotation = Data1.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data1.values, context)).isFalse();
    }

    @Test
    void testNotNullEach_Valid() throws NoSuchFieldException {
        Data2 data2 = new Data2();
        data2.setValues(new String[]{"abcd", "pqrs"});
        ValidStringArray annotation = Data2.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data2.values, context)).isTrue();
    }

    @Test
    void testNotNullEach_InValid() throws NoSuchFieldException {
        Data2 data2 = new Data2();
        data2.setValues(new String[]{"abcd", null});
        ValidStringArray annotation = Data2.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data2.values, context)).isFalse();
    }

    @Test
    void testNotBlankEach_Valid() throws NoSuchFieldException {
        Data3 data3 = new Data3();
        data3.setValues(new String[]{"abcd", "pqrs"});
        ValidStringArray annotation = Data3.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data3.values, context)).isTrue();
    }

    @Test
    void testNotBlankEach_InValid() throws NoSuchFieldException {
        Data3 data3 = new Data3();
        data3.setValues(new String[]{"abcd", ""});
        ValidStringArray annotation = Data3.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data3.values, context)).isFalse();
    }

    @Test
    void testMaxLengthEach_Valid() throws NoSuchFieldException {
        Data4 data4 = new Data4();
        data4.setValues(new String[]{"abcd4", "pqrs"});
        ValidStringArray annotation = Data4.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data4.values, context)).isTrue();

    }

    @Test
    void testMaxLengthEach_InValid() throws NoSuchFieldException {
        Data4 data4 = new Data4();
        data4.setValues(new String[]{"abcdiasudhia", "asdas"});
        ValidStringArray annotation = Data4.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data4.values, context)).isFalse();
    }

    @Test
    void testMinLengthEach_Valid() throws NoSuchFieldException {
        Data4 data4 = new Data4();
        data4.setValues(new String[]{"abcd2", "pqrs"});
        ValidStringArray annotation = Data4.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data4.values, context)).isTrue();
    }

    @Test
    void testMinLengthEach_InValid() throws NoSuchFieldException {
        Data4 data4 = new Data4();
        data4.setValues(new String[]{"abcd1", "p"});
        ValidStringArray annotation = Data4.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data4.values, context)).isFalse();
    }

    @Test
    void testUUID_Valid() throws NoSuchFieldException {
        Data5 data5 = new Data5();
        data5.setValues(new String[]{"0086a994-6785-4a7c-a722-6d0acbf9afd0", "f3b3ea1e-a75b-4eba-a986-069f77ff3b48"});
        ValidStringArray annotation = Data5.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data5.values, context)).isTrue();
    }

    @Test
    void testUUID_InValid() throws NoSuchFieldException {
        Data5 data5 = new Data5();
        data5.setValues(new String[]{"0086a994-6785-4a7c-a722-6d0acbf9afd0", "second-ID-invalid-UUID-value"});
        ValidStringArray annotation = Data5.class.getDeclaredField("values").getAnnotation(ValidStringArray.class);
        validator.initialize(annotation);
        assertThat(validator.isValid(data5.values, context)).isFalse();
    }

    @BeforeEach
    void setUp() {
        validator = new StringArrayValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Data
    public static class Data1 {
        @ValidStringArray(message = MESSAGE, maxSize = 2, minSize = 1)
        private String[] values;
    }

    @Data
    public static class Data2 {
        @ValidStringArray(message = MESSAGE, notNullEach = true)
        private String[] values;
    }

    @Data
    public static class Data3 {
        @ValidStringArray(message = MESSAGE, notNullEach = true, notBlankEach = true)
        private String[] values;
    }

    @Data
    public static class Data4 {
        @ValidStringArray(message = MESSAGE, notNullEach = true, maxLengthEach = 6, minLengthEach = 2)
        private String[] values;
    }

    @Data
    public static class Data5 {
        @ValidStringArray(message = MESSAGE, notNullEach = true, uuid = true)
        private String[] values;
    }
}
