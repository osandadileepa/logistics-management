package com.quincus.s3.api;

import com.quincus.s3.domain.FileResult;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileApiImplTest {

    private final String orgId = "karate-org";
    @InjectMocks
    private FileApiImpl fileApi;
    @Mock
    private FileService fileService;
    @Mock
    private UserDetailsContextHolder userDetailsContextHolder;

    @Test
    void getUploadPreSignedUrl_validArguments_shouldReturnUrl() {
        String directoryName = "dir";
        List<String> fileNames = List.of("dummy.jpg");
        String expectedUrl = "https://pre-signed-url";

        FileResult result = new FileResult();
        result.setUrl(expectedUrl);

        when(fileService.generateUploadPreSignedUrl(fileNames, orgId, directoryName)).thenReturn(List.of(result));
        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(orgId);

        List<FileResult> actualResults = fileApi.getUploadPreSignedUrl(fileNames, directoryName);
        Optional<FileResult> results = actualResults.stream().findFirst();
        String actualUrl = results.map(FileResult::getUrl).orElse(null);

        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    void getReadFilePreSignedUrl_validArguments_shouldReturnUrl() {
        String directoryName = "dir";
        List<String> fileNames = List.of("dummy.jpg");
        String expectedUrl = "https://pre-signed-url";


        FileResult result = new FileResult();
        result.setUrl(expectedUrl);

        when(fileService.findByNameAndGetSignedUrl(fileNames, orgId, directoryName)).thenReturn(List.of(result));
        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(orgId);

        List<FileResult> actualResults = fileApi.getReadFilePreSignedUrl(fileNames, directoryName);
        Optional<FileResult> results = actualResults.stream().findFirst();
        String actualUrl = results.map(FileResult::getUrl).orElse(null);

        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    void deleteFileFromBucket_validArguments_shouldExecute() {
        String directoryName = "dir";
        String fileName = "dummy.jpg";
        when(userDetailsContextHolder.getCurrentOrganizationId()).thenReturn(orgId);

        fileApi.deleteFileFromBucket(fileName, directoryName);

        verify(fileService, times(1)).deleteFile(fileName, orgId, directoryName);
    }
}
