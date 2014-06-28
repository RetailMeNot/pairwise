package com.rmn.pairwise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Scenario {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private List<ParameterSet<?>> parameterSets = new ArrayList<ParameterSet<?>>();
    public List<ParameterSet<?>> getParameterSets() { return parameterSets; }
    public ParameterSet<?> getParameterSet(int index) {
        return getParameterSets().get(index);
    }

    private List<List<Integer>> legalValues = new ArrayList<List<Integer>>();
    
    /**
     * An array of arrays representing the parameter set (x), and the index of the flattened-out "parameterValues" array (y).
     * See documentation at the top of the PairwiseInventory class for details
     * @return
     */
    public List<List<Integer>> getLegalValues() { return legalValues; }
    
    public void addParameterSet(ParameterSet<?> parameterSet) {
        parameterSets.add(parameterSet);
        List<Integer> parameterValueIndexes = new ArrayList<Integer>(this.getParameterValues().size());

        //Rebuild the various metadata arrays each time (since we'll never know from here whether or not we're "done"--they can keep adding Parameter Sets)
        updateLegalValues(parameterSet, parameterValueIndexes);
        updateParameterValues(parameterSet);
        updateParameterPositions();
    }
    
    /**
     * A flattened array representing the values of all the parameters in the set
     */
    private List<?> parameterValues = new ArrayList();
    public List<?> getParameterValues() { return parameterValues; }

    protected void updateParameterValues(ParameterSet<?> parameterSet) {
        parameterValues.addAll((Collection)parameterSet.getParameterValues());
    }

    protected void updateLegalValues(ParameterSet<?> parameterSet, List<Integer> parameterValueIndexes) {
        for (int i=0, j = getParameterValuesCount(); j < getParameterValuesCount() + parameterSet.getParameterValues().size(); i++, j++) {
            parameterValueIndexes.add(i, j);
        }
        Collections.addAll(legalValues, parameterValueIndexes);
    }

    /**
     * The total number of Parameter Sets under analysis. In a web form, this would be analogous to the number of fields
     * we're testing (possible values for User Type would be one Parameter Set, etc)
     * @return
     */
    public int getParameterSetCount() {
        return legalValues.size();
    }
    
    /**
     * The total number of values represented by all Parameter Sets under analysis
     * @return
     */
    public int getParameterValuesCount() {
        return parameterValues.size();
    }

    /**
     * A flattened array representing the parameter set to which this value belongs
     * @return An array of int, containing the indices of the parameter sets to which this value belongs
     */
    private List<Integer> parameterPositions = null; // The parameter position for a given value
    public List<Integer> getParameterPositions() { return this.parameterPositions; }

    /**
     * The parameterPositions field (int[]) represents the "parameter position" for each given value. See above for details
     */
    public void updateParameterPositions() {
        List<Integer> parameterPositions = new ArrayList<Integer>(this.getParameterValuesCount()); // the indexes tell us which parameter set the value belongs to

        int k = 0; //The index of the parameter set attached to this value
        for (int i = 0; i < this.getLegalValues().size(); ++i) {
            List<Integer> curr = this.getLegalValues().get(i);
            for (int aCurr : curr) {
                parameterPositions.add(k++, i);
            }
        }
        log.debug("Parameter Positions: {}", parameterPositions.toString());
        this.parameterPositions = parameterPositions;  
    }

    /**
     * Logs the current Parameter values contained in this Scenario
     */
    public void logParameterValues() {
        log.debug("Parameter Values: {}", getParameterValues().toString());
    }
 }
