package com.quincus.s3.api;

import com.quincus.s3.domain.FileResult;

import java.util.List;

public interface FileApi {

    List<FileResult> getUploadPreSignedUrl(final List<String> fileNames, final String directoryName);

    List<FileResult> getReadFilePreSignedUrl(final List<String> fileNames, final String directoryName);

    void deleteFileFromBucket(final String fileName, final String directoryName);
}
