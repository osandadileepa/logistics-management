package com.quincus.networkmanagement.api.domain;

public record FacilityPair<T, U>(T arrival, U departure) {
    @Override
    public String toString() {
        return "(" + arrival + ", " + departure + ")";
    }
}
