package com.quincus.shipment.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Facility {
    private String id;
    private String externalId;
    private String name;
    private String group;
    private String code;
    private String type;
    private String function;
    private String status;
    private Address location;
    private String tag;
    private String note;
    private List<Milestone> hubEvents;
    private String locationCode;
    private String timezone;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Facility that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
