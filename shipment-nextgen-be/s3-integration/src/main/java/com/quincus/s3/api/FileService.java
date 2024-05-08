package com.quincus.s3.api;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.quincus.s3.constant.UrlType;
import com.quincus.s3.domain.FileResult;
import com.quincus.s3.domain.S3Bucket;
import com.quincus.web.common.exception.model.ObjectNotFoundException;
import com.quincus.web.common.exception.model.OperationNotAllowedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FileService {
    private static final String OPERATION_UPLOAD = "Upload";
    private static final String OPERATION_READ = "Read";
    private static final String OPERATION_DELETE = "Delete";
    private static final String ERR_NOT_ALLOWED_MSG = "%s is not allowed in directory %s";
    private static final String ERR_NOT_ALLOWED_LOG = "{} is not allowed on bucket {} with sub-directory {}";
    private static final String ERR_FILE_NOT_FOUND = "File not found in directory %s";
    private final AmazonS3 amazonS3;
    private final S3Bucket bucket;

    public List<FileResult> findByNameAndGetSignedUrl(List<String> fileNames, String organizationId, String directoryName) {
        checkDirectory(directoryName, OPERATION_READ);

        List<FileResult> results = new ArrayList<>();
        for (String fileName : fileNames) {
            String filePath = getFilePath(fileName, organizationId, directoryName);
            String url = null;
            String error = null;

            try {
                checkObjectExistsInBucket(bucket.getName(), filePath);

                log.info("Generating signed URL for file name {}", filePath);
                bucket.recalculatePreSignedReadFileUrlExpiryDate();
                url = generateUrl(filePath, HttpMethod.GET, bucket.getPreSignedReadFileUrlExpiryDate());
            } catch (ObjectNotFoundException objectNotFoundException) {
                error = String.format(ERR_FILE_NOT_FOUND, directoryName);
            }

            FileResult result = new FileResult();
            result.setFilename(fileName);
            result.setUrl(url);
            result.setUrlType(UrlType.PRE_SIGNED_READ);
            result.setError(error);

            results.add(result);
        }
        return results;
    }

    public List<FileResult> generateUploadPreSignedUrl(List<String> fileNames, String organizationId, String directoryName) {
        checkDirectory(directoryName, OPERATION_UPLOAD);

        List<FileResult> results = new ArrayList<>();
        for (String fileName : fileNames) {
            String contentType = getContentType(fileName);

            if (!isFileSupported(directoryName, contentType)) {
                log.error("Cannot generate pre-signed URL for file {}: Unsupported Content-Type {}", fileNames, contentType);
                throw new UnsupportedMediaTypeStatusException(String.format("Unsupported content-type %s", contentType));
            }

            String filePath = getFilePath(fileName, organizationId, directoryName);
            log.info("Generating pre-signed URL for uploading file to {}", filePath);
            bucket.recalculatePreSignedUploadUrlExpiryDate();
            String url = generateUrl(filePath, contentType, HttpMethod.PUT, bucket.getPreSignedUploadUrlExpiryDate());

            FileResult result = new FileResult();
            result.setFilename(fileName);
            result.setUrl(url);
            result.setUrlType(UrlType.PRE_SIGNED_UPLOAD);

            results.add(result);
        }
        return results;
    }

    public void deleteFile(String fileName, String organizationId, String directoryName) {
        checkDirectory(directoryName, OPERATION_DELETE);
        String filePath = getFilePath(fileName, organizationId, directoryName);
        checkObjectExistsInBucket(bucket.getName(), filePath);
        amazonS3.deleteObject(bucket.getName(), filePath);
    }

    private void checkDirectory(String directoryName, String operation) {
        if (!isDirectoryAllowed(directoryName)) {
            log.error(ERR_NOT_ALLOWED_LOG, operation, bucket.getName(), directoryName);
            throw new OperationNotAllowedException(String.format(ERR_NOT_ALLOWED_MSG, operation, directoryName));
        }
    }

    private boolean isDirectoryAllowed(String directory) {
        return bucket.getAllowedSubDirectories().contains(directory);
    }

    private boolean isFileSupported(String directory, String contentType) {
        if (bucket.getSupportedMediaTypes().containsKey(directory)) {
            return bucket.getSupportedMediaTypes().get(directory).contains(contentType);
        }
        return false;
    }

    private void checkObjectExistsInBucket(String bucketName, String filePath) {
        if (!amazonS3.doesObjectExist(bucketName, filePath)) {
            log.warn("Bucket {} does not contain file {}", bucketName, filePath);
            throw new ObjectNotFoundException("file", filePath);
        }
    }

    private String generateUrl(String filePath, HttpMethod httpMethod, Date expiry) {
        return generateUrl(filePath, null, httpMethod, expiry);
    }

    private String generateUrl(String filePath, String contentType, HttpMethod httpMethod, Date expiry) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket.getName(), filePath, httpMethod);
        request.setExpiration(expiry);
        if (contentType != null) {
            request.setContentType(contentType);
        }
        return amazonS3.generatePresignedUrl(request).toString();
    }

    private String getFilePath(String fileName, String organizationId, String directoryName) {
        return String.format("%s/%s/%s/%s", bucket.getBaseDir(), organizationId, directoryName, fileName);
    }

    private String getContentType(String fileName) {
        File file = new File(fileName);
        String fileType = null;
        try {
            fileType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            log.error("File type not detected for " + fileName);
        }
        return fileType;
    }
}
