package com.quincus.s3.web;

import com.quincus.s3.api.FileApi;
import com.quincus.s3.constant.UrlType;
import com.quincus.s3.domain.FileResult;
import com.quincus.web.common.model.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerImplTest {

    @InjectMocks
    FileControllerImpl fileController;

    @Mock
    FileApi fileApi;

    @Test
    void getUploadFilePreSignedUrl_validData_shouldReturnSuccess() {
        String directory = "attachment";
        List<String> fileNames = List.of( "dummy.jpg");
        String expectedUrl ="https://dummy-url/dummy.jpg";

        FileResult result = new FileResult();
        result.setUrlType(UrlType.PRE_SIGNED_UPLOAD);
        result.setUrl(expectedUrl);

        when(fileApi.getUploadPreSignedUrl(fileNames, directory))
                .thenReturn(List.of(result));

        Response<List<FileResult>> response = fileController.getUploadFilePreSignedUrl(fileNames, directory);
        assertThat(response).isNotNull();

        FileResult fileResult = response.getData().stream().findFirst().orElse(null);
        assertThat(fileResult).isNotNull();
        assertThat(fileResult.getUrlType()).isEqualTo(UrlType.PRE_SIGNED_UPLOAD);
        assertThat(fileResult.getUrl()).isEqualTo(expectedUrl);
    }

    @Test
    void getReadFilePreSignedUrl_validData_shouldReturnSuccess() {
        String directory = "attachment";
        List<String> fileName = List.of("dummy.jpg");
        String expectedUrl = "https://dummy-url/dummy.jpg";

        FileResult result = new FileResult();
        result.setUrlType(UrlType.PRE_SIGNED_READ);
        result.setUrl(expectedUrl);

        when(fileApi.getReadFilePreSignedUrl(fileName, directory))
                .thenReturn(List.of(result));

        Response<List<FileResult>> response = fileController.getReadFilePreSignedUrl(fileName, directory);
        assertThat(response).isNotNull();

        FileResult fileResult = response.getData().stream().findFirst().orElse(null);
        assertThat(fileResult).isNotNull();
        assertThat(fileResult.getUrlType()).isEqualTo(UrlType.PRE_SIGNED_READ);
        assertThat(fileResult.getUrl()).isEqualTo(expectedUrl);
    }

    @Test
    void deleteFileFromBucket_validData_shouldExecuteDelete() {
        String directory = "attachment";
        String fileName = "dummy.jpg";
        ResponseEntity<Void> response = fileController.deleteFileFromBucket(fileName, directory);
        assertThat(response).isNotNull();

        assertThat(response.getStatusCode()).withFailMessage("Response code mismatch.").isEqualTo(HttpStatus.NO_CONTENT);
    }
}
