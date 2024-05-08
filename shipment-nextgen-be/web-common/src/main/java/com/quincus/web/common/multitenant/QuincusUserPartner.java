package com.quincus.web.common.multitenant;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class QuincusUserPartner implements Serializable {
    private String partnerId;
    private String partnerName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuincusUserPartner that)) {
            return false;
        }
        return (partnerId != null) && Objects.equals(partnerId, that.partnerId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
