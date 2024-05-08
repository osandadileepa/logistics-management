package com.quincus.shipment.impl.validator;

import com.quincus.shipment.api.exception.PackageDimensionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageDimensionValidatorTest {

    private final PackageDimensionValidator packageDimensionValidator = new PackageDimensionValidator();

    @Test
    void testValidatePackageDimensionUpdateFile_shouldThrowInvalidExtension() {
        File file = new File("file.ext");

        assertThatThrownBy(() -> packageDimensionValidator.validatePackageDimensionUpdateFile(file))
                .isInstanceOfSatisfying(PackageDimensionException.class, exception -> {
                    assertThat(exception.getMessage()).isEqualTo(PackageDimensionValidator.ERR_MSG_INVALID_FILE_FORMAT);
                });
    }

    @Test
    void testValidatePackageDimensionUpdateFile_shouldThrowInvalidFileSizeLimit() {
        File file = createMockFile(12111000, "file.csv");

        assertThatThrownBy(() -> packageDimensionValidator.validatePackageDimensionUpdateFile(file))
                .isInstanceOfSatisfying(PackageDimensionException.class, exception -> {
                    assertThat(exception.getMessage()).isEqualTo(PackageDimensionValidator.ERR_MSG_FILE_SIZE_LIMIT);
                });
    }

    @Test
    void testValidatePackageDimensionUpdateFile_shouldThrowInvalidTemplateFormat() {
        String line = "ABC, DEF";
        File file = createAndWriteFile(line, "file.csv");

        assertThatThrownBy(() -> packageDimensionValidator.validatePackageDimensionUpdateFile(file))
                .isInstanceOfSatisfying(PackageDimensionException.class, exception -> {
                    assertThat(exception.getMessage()).isEqualTo(PackageDimensionValidator.ERR_MSG_FILE_INVALID_TEMPLATE_FORMAT);
                });
    }

    @Test
    void testValidatePackageDimensionUpdateFile_shouldNotThrowAnyException() {
        String line = "Shipment ID,Packaging Type,Unit,Height,Width,Length,Weight";
        File file = createAndWriteFile(line, "file.csv");
        packageDimensionValidator.validatePackageDimensionUpdateFile(file);
        file.delete();
    }

    private File createMockFile(long length, String filename) {
        File mockFile = mock(File.class);
        when(mockFile.length()).thenReturn(length);
        when(mockFile.getName()).thenReturn(filename);
        return mockFile;
    }

    private File createAndWriteFile(String line, String path) {
        try {
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(line);
            fileWriter.close();
            return new File(path);
        } catch (IOException ignored) {
        }
        return null;
    }

}
