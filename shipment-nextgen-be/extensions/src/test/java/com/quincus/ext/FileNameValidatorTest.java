package com.quincus.ext;

import com.quincus.ext.annotation.FileName;
import com.quincus.ext.annotation.validator.FileNameValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileNameValidatorTest {

    private final FileNameValidator validator = new FileNameValidator();
    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    @Mock
    private FileName fileNameAnnotation;

    @BeforeEach
    public void setUp() {
        lenient().when(fileNameAnnotation.required()).thenReturn(true).thenReturn(false);
        lenient().when(context.buildConstraintViolationWithTemplate(org.mockito.Mockito.anyString())).thenReturn(violationBuilder);
    }

    @ParameterizedTest
    @MethodSource("provideFileNamesForValidation")
    void testFileNameValidation(String input, boolean required, boolean expected) {
        when(fileNameAnnotation.required()).thenReturn(required);
        validator.initialize(fileNameAnnotation);
        assertThat(validator.isValid(input, context)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideFileNamesForValidation() {
        String longFileName = new String(new char[257]).replace('\0', 'a') + ".txt";
        return Stream.of(
                Arguments.of("", true, false),
                Arguments.of(null, false, true),
                Arguments.of("", false, true),
                Arguments.of(longFileName, true, false),
                Arguments.of("invalidFileNameWithoutExtension", true, false),
                Arguments.of("validFileName.txt", true, true),
                Arguments.of("Screenshot 2023-08-17 at 16.18.07.png", true, true),
                Arguments.of("signature - Copy (2).png", true, true),
                Arguments.of("      signature - Copy (2)     .png       ", true, true),
                Arguments.of("file_name.txt", true, true),
                Arguments.of("file-name.txt", true, true),
                Arguments.of("file name.txt", true, true),
                Arguments.of("file.name.txt", true, true),
                Arguments.of("file, name.txt", true, true),
                Arguments.of("file; name.txt", true, true),
                Arguments.of("file's name.txt", true, true),
                Arguments.of("file\"name\".txt", true, true),
                Arguments.of("file!name.txt", true, true),
                Arguments.of("file?name.txt", true, true),
                Arguments.of("filename!.txt", true, true),
                Arguments.of("file[name].txt", true, true),
                Arguments.of("file(name).txt", true, true),
                Arguments.of("file+name.txt", true, true),
                Arguments.of("file=name.txt", true, true),
                Arguments.of("file@name.txt", true, true),
                Arguments.of("file#name.txt", true, true),
                Arguments.of("file&name.txt", true, true),
                Arguments.of("file$name.txt", true, true),
                Arguments.of("file%name.txt", true, true),
                Arguments.of("file^name.txt", true, true),
                Arguments.of("file~name.txt", true, true)
        );
    }

}
