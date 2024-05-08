package com.quincus.web.common.uitility;

import com.quincus.web.common.utility.FileConverter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class FileConverterTest {

    @Test
    void convertMultipartToFileNullInput() {
        MultipartFile file = null;
        File result = FileConverter.convertMultipartToFile(file);
        assertThat(result).isNull();
    }

    @Test
    void convertMultipartToFileValidInput() throws IOException {
        String content = "Test file content";
        MultipartFile multipartFile = createMockMultipartFile("test.txt", content.getBytes());

        File result = FileConverter.convertMultipartToFile(multipartFile);

        assertThat(result).isNotNull().exists();
        assertThat(content).isEqualTo(readFileContent(result));
        result.deleteOnExit();
    }

    @Test
    void convertMultipartToFileEmptyFile() {
        byte[] emptyContent = new byte[0];
        MultipartFile multipartFile = createMockMultipartFile("empty.txt", emptyContent);

        File result = FileConverter.convertMultipartToFile(multipartFile);

        assertThat(result).isNotNull().exists().isEmpty();
        result.deleteOnExit();
    }

    private MockMultipartFile createMockMultipartFile(String originalFilename, byte[] content) {
        return new MockMultipartFile(originalFilename, originalFilename, MimeTypeUtils.TEXT_PLAIN_VALUE, content);
    }

    private String readFileContent(File file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return new String(inputStream.readAllBytes());
        }
    }
}

