package com.quincus.shipment.impl.web;

import com.quincus.shipment.api.PackageDimensionApi;
import com.quincus.shipment.api.dto.BulkPackageDimensionUpdateRequest;
import com.quincus.shipment.api.dto.BulkPackageDimensionUpdateResponse;
import com.quincus.web.common.model.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageDimensionControllerImplTest {

    @InjectMocks
    private PackageDimensionControllerImpl packageDimensionControllerImpl;
    @Mock
    private PackageDimensionApi packageDimensionApi;

    @AfterAll
    static void deleteFile() {
        File file = new File("test");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testGetPackageUpdateFileTemplate() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        try {
            when(httpServletResponse.getWriter()).thenReturn(writer);
            when(httpServletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
            packageDimensionControllerImpl.getPackageUpdateFileTemplate(httpServletResponse);
            verify(packageDimensionApi, times(1)).getPackageUpdateFileTemplate(any(PrintWriter.class));
        } catch (IOException e) {
            fail("Exception occurred in normal path testing: " + e.getMessage());
        }
    }

    @Test
    void testBulkPackageDimensionUpdateImport_withNoError() {
        String[] cells = new String[]{"test", "test", "test", "1", "1", "1", "1"};
        List<BulkPackageDimensionUpdateRequest> bulkResult = new ArrayList<>();
        BulkPackageDimensionUpdateRequest record1 = new BulkPackageDimensionUpdateRequest(cells);
        bulkResult.add(record1);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("test");
        when(packageDimensionApi.bulkPackageDimensionUpdateImport(any(File.class))).thenReturn(bulkResult);
        Response<BulkPackageDimensionUpdateResponse> response = packageDimensionControllerImpl.bulkPackageDimensionUpdateImport(multipartFile);
        assertThat(response.getData().getNumberOfSuccess()).isEqualTo(1);
    }

    @Test
    void testBulkPackageDimensionUpdateImport_withError() {
        String[] cells = new String[]{"test", "test", "test", "1", "1", "1", "1"};
        List<BulkPackageDimensionUpdateRequest> bulkResult = new ArrayList<>();
        BulkPackageDimensionUpdateRequest record1 = new BulkPackageDimensionUpdateRequest(cells);
        record1.setWeightError("error");
        bulkResult.add(record1);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("test");
        when(packageDimensionApi.bulkPackageDimensionUpdateImport(any(File.class))).thenReturn(bulkResult);
        Response<BulkPackageDimensionUpdateResponse> response = packageDimensionControllerImpl.bulkPackageDimensionUpdateImport(multipartFile);
        assertThat(response.getData().getNumberOfSuccess()).isZero();
    }
}
