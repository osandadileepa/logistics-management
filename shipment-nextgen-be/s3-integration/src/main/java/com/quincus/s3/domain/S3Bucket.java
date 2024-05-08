package com.quincus.s3.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class S3Bucket {
    private final int uploadExpiryMinutesDuration;
    private final int readExpiryMinutesDuration;
    private String name;
    private String baseDir;
    private Date preSignedUploadUrlExpiryDate;
    private Date preSignedReadFileUrlExpiryDate;
    private Map<String, List<String>> supportedMediaTypes;
    private List<String> allowedSubDirectories;

    public S3Bucket(int uploadExpiryMinutesDuration, int readExpiryMinutesDuration) {
        this.uploadExpiryMinutesDuration = uploadExpiryMinutesDuration;
        this.readExpiryMinutesDuration = readExpiryMinutesDuration;
    }

    public void recalculatePreSignedUploadUrlExpiryDate() {
        this.preSignedUploadUrlExpiryDate = calculateExpiryDate(this.uploadExpiryMinutesDuration).getTime();
    }

    public void recalculatePreSignedReadFileUrlExpiryDate() {
        this.preSignedReadFileUrlExpiryDate = calculateExpiryDate(this.readExpiryMinutesDuration).getTime();
    }

    private Calendar calculateExpiryDate(int expiryDuration) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, expiryDuration);
        return calendar;
    }
}
