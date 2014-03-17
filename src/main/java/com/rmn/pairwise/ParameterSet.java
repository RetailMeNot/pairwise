package com.rmn.pairwise;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single "dimension" of parameters. If you're testing a web form for creating a new user, and the "user type" field has 4 
 * values "read-only", "power", "admin", and "root", that single set of values would be represented by this object
 * @author mmerrell
 *
 * @param <T>
 */
public class ParameterSet<T> {
    
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public ParameterSet(List<T> values) {
        parameterValues.addAll(values);
    }
    
    private List<T> parameterValues = new ArrayList<T>();
    public List<T> getParameterValues() { return parameterValues; }
    
    public T getValue(int index) {
        return parameterValues.get(index);
    }
}
