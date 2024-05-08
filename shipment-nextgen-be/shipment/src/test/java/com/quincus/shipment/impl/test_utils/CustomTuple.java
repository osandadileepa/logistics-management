package com.quincus.shipment.impl.test_utils;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomTuple implements Tuple {

    private final Map<String, Object> elements = new HashMap<>();

    public void put(String key, Object value) {
        elements.put(key, value);
    }

    @Override
    public <X> X get(TupleElement<X> tupleElement) {
        return null;
    }

    @Override
    public <X> X get(String alias, Class<X> type) {
        return (X) elements.get(alias);
    }

    @Override
    public Object get(String alias) {
        return elements.get(alias);
    }

    @Override
    public <X> X get(int i, Class<X> type) {
        return null;
    }

    @Override
    public Object get(int i) {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public List<TupleElement<?>> getElements() {
        return null;
    }
}
