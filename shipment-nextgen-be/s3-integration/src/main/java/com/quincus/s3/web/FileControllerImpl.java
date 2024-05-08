package com.quincus.s3.web;

import com.quincus.s3.FileController;
import com.quincus.s3.api.FileApi;
import com.quincus.s3.domain.FileResult;
import com.quincus.web.common.model.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/files")
@AllArgsConstructor
public class FileControllerImpl implements FileController {
    private FileApi fileApi;

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_VIEW', 'COST_CREATE', 'COST_EDIT', 'COST_VIEW', 'S2S')")
    public Response<List<FileResult>> getUploadFilePreSignedUrl(final List<String> fileNames, final String directory) {
        return new Response<>(fileApi.getUploadPreSignedUrl(fileNames, directory));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SHIPMENTS_EDIT', 'SHIPMENTS_VIEW', 'COST_CREATE', 'COST_EDIT', 'COST_VIEW', 'S2S')")
    public Response<List<FileResult>> getReadFilePreSignedUrl(final List<String> fileNames, final String directory) {
        return new Response<>(fileApi.getReadFilePreSignedUrl(fileNames, directory));
    }

    @Override
    public ResponseEntity<Void> deleteFileFromBucket(final String fileName, final String directory) {
        fileApi.deleteFileFromBucket(fileName, directory);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
