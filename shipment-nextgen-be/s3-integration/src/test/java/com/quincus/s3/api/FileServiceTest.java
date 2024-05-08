package com.quincus.s3.api;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.quincus.s3.domain.FileResult;
import com.quincus.s3.domain.S3Bucket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks
    FileService fileService;

    @Mock
    AmazonS3 amazonS3;

    @Mock
    S3Bucket bucket;

    @Test
    void findByNameAndGetSignedUrl_fileExists_shouldReturnUrl() throws MalformedURLException {
        String bucketName = "bucket-test";
        Date expiryDate = new Date();
        String organizationId = "org1";
        String dirName = "directory";
        String fileName = "happy.png";
        String expectedUrl = "https://presigned-url";
        URL url = new URL(expectedUrl);

        when(bucket.getName()).thenReturn(bucketName);
        when(bucket.getAllowedSubDirectories()).thenReturn(List.of(dirName));
        when(bucket.getPreSignedReadFileUrlExpiryDate()).thenReturn(expiryDate);
        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(true);
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(url);

        List<FileResult> actualResults = fileService.findByNameAndGetSignedUrl(List.of(fileName), organizationId, dirName);
        Optional<FileResult> results = actualResults.stream().findFirst();
        String actualUrl = results.isPresent() ? results.get().getUrl() : null;

        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(bucket, times(1)).recalculatePreSignedReadFileUrlExpiryDate();
    }

    @Test
    void findByNameAndGetSignedUrl_fileDoesNotExist_shouldThrowException() {
        String bucketName = "bucket-test";
        String organizationId = "org1";
        String dirName = "directory";
        String fileName = "happy.png";
        String expectedError = "File not found in directory";

        when(bucket.getName()).thenReturn(bucketName);
        when(bucket.getAllowedSubDirectories()).thenReturn(List.of(dirName));
        when(amazonS3.doesObjectExist(anyString(), anyString())).thenReturn(false);

        List<FileResult> actualResults = fileService.findByNameAndGetSignedUrl(List.of(fileName), organizationId, dirName);
        Optional<FileResult> results = actualResults.stream().findFirst();
        String actualError = results.isPresent() ? results.get().getError() : null;

        assertThat(actualError).contains(expectedError);
    }

    @Test
    void generateUploadPreSignedUrl_validArguments_shouldReturnUrl() throws MalformedURLException {
        String bucketName = "bucket-test";
        Date expiryDate = new Date();
        String contentType = "image/png";
        String organizationId = "org1";
        String dirName = "directory";
        String fileName = "happy.png";
        String expectedUrl = "https://presigned-url";
        URL url = new URL(expectedUrl);
        Map<String, List<String>> subDirectoriesAndSupportedFiles = new HashMap<>();
        subDirectoriesAndSupportedFiles.put(dirName, List.of(contentType));

        when(bucket.getName()).thenReturn(bucketName);
        when(bucket.getAllowedSubDirectories()).thenReturn(List.of(dirName));
        when(bucket.getPreSignedUploadUrlExpiryDate()).thenReturn(expiryDate);
        when(bucket.getSupportedMediaTypes()).thenReturn(subDirectoriesAndSupportedFiles);
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(url);

        List<FileResult> actualResults = fileService.generateUploadPreSignedUrl(List.of(fileName), organizationId, dirName);
        Optional<FileResult> results = actualResults.stream().findFirst();
        String actualUrl = results.isPresent() ? results.get().getUrl() : null;

        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(bucket, times(1)).recalculatePreSignedUploadUrlExpiryDate();
    }
}
