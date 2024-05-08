package com.quincus.shipment.api.domain;

import com.quincus.ext.annotation.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Organization {

    @UUID
    private String id;
    @Size(max = 128, message = "Must be maximum of 128 characters.")
    private String name;
    @Size(max = 64, message = "Must be maximum of 64 characters.")
    private String code;

    public Organization(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Organization that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
