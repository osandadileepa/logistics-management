package com.quincus.s3.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class S3BucketTest {

    @Test
    void testRecalculatePreSignedUploadExpiryShouldAlwaysBeFuture() throws MalformedURLException {
        S3Bucket s3Bucket = new S3Bucket(10, 100);

        s3Bucket.recalculatePreSignedUploadUrlExpiryDate();

        assertThat(s3Bucket.getPreSignedUploadUrlExpiryDate()).isAfter(new Date());
    }

    @Test
    void testRecalculatePreSignedReadExpiryShouldAlwaysBeFuture() throws MalformedURLException {
        S3Bucket s3Bucket = new S3Bucket(10, 100);

        s3Bucket.recalculatePreSignedReadFileUrlExpiryDate();

        assertThat(s3Bucket.getPreSignedReadFileUrlExpiryDate()).isAfter(new Date());
    }
}
