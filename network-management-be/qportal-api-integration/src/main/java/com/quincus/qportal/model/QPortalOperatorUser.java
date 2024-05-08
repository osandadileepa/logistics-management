package com.quincus.qportal.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QPortalOperatorUser {
    private List<String> emails;
    private List<String> mobileNos;
}
