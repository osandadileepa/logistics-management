package com.quincus.s3.domain;

import com.quincus.s3.constant.UrlType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileResult {
    private String filename;
    private UrlType urlType;
    private String url;
    private String error;
}
