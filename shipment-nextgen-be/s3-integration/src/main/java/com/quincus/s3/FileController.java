package com.quincus.s3;

import com.quincus.ext.annotation.FileName;
import com.quincus.ext.annotation.FileNames;
import com.quincus.s3.domain.FileResult;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Size;
import java.util.List;

@RequestMapping("/files")
@Tag(name = "files", description = "This endpoint manages file transactions.")
@Validated
public interface FileController {

    @GetMapping("/upload-pre-signed-link")
    @Operation(summary = "Upload File Pre-Signed URL API", description = "Generates a pre-signed URL for uploading file.", tags = "files")
    Response<List<FileResult>> getUploadFilePreSignedUrl(@FileNames @RequestParam("file_name") final List<String> fileNames,
                                                         @Size(min = 1, max = 256) @RequestParam("directory") final String directory);

    @GetMapping("/read-pre-signed-link")
    @Operation(summary = "Read File Pre-Signed URL API", description = "Generates a pre-signed URL for reading file.", tags = "files")
    Response<List<FileResult>> getReadFilePreSignedUrl(@FileNames @RequestParam("file_name") final List<String> fileNames,
                                                       @Size(min = 1, max = 256) @RequestParam("directory") final String directory);

    @DeleteMapping
    @Operation(summary = "Delete File", description = "Delete an existing file saved in an external bucket.", tags = "files")
    ResponseEntity<Void> deleteFileFromBucket(@FileName @RequestParam("file_name") final String fileName,
                                              @Size(min = 1, max = 256) @RequestParam("directory") final String directory);
}
