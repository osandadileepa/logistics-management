package com.quincus.s3.api;

import com.quincus.s3.domain.FileResult;
import com.quincus.web.common.multitenant.UserDetailsContextHolder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FileApiImpl implements FileApi {
    private final FileService fileService;
    private final UserDetailsContextHolder userDetailsContextHolder;

    @Override
    public List<FileResult> getUploadPreSignedUrl(List<String> fileNames, String directoryName) {
        return fileService.generateUploadPreSignedUrl(fileNames, userDetailsContextHolder.getCurrentOrganizationId(), directoryName);
    }

    @Override
    public List<FileResult> getReadFilePreSignedUrl(final List<String> fileName, final String directoryName) {
        return fileService.findByNameAndGetSignedUrl(fileName, userDetailsContextHolder.getCurrentOrganizationId(), directoryName);
    }

    @Override
    public void deleteFileFromBucket(final String fileName, final String directoryName) {
        fileService.deleteFile(fileName, userDetailsContextHolder.getCurrentOrganizationId(), directoryName);
    }

}
