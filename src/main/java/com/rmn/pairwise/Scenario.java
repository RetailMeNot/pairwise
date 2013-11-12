package com.rmn.pairwise;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scenario<T> {
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    private List<ParameterSet<T>> parameterSets = new ArrayList<ParameterSet<T>>();
    public List<ParameterSet<T>> getParameterSets() { return parameterSets; }
    public ParameterSet<T> getParameterSet( int index ) {
        return getParameterSets().get( index );
    }

    private int[][] legalValues;
    
    /**
     * An array of arrays representing the parameter set (x), and the index of the flattened-out "parameterValues" array (y).
     * See documentation at the top of the PairwiseInventory class for details
     * @return
     */
    public int[][] getLegalValues() { return legalValues; }
    
    public void addParameterSet( ParameterSet<T> parameterSet ) {
        parameterSets.add( parameterSet );
        int[] parameterValueIndexes = new int[ parameterSet.getParameterValues().size() ];

        //Rebuild the various metadata arrays each time (since we'll never know from here whether or not we're "done"--they can keep adding Parameter Sets)
        updateLegalValues( parameterSet, parameterValueIndexes );
        updateParameterValues( parameterSet );
        updateParameterPositions();
    }
    
    /**
     * A flattened array representing the values of all the parameters in the set
     */
    private List<T> parameterValues = new ArrayList<T>();
    public List<T> getParameterValues() { return parameterValues; }

    protected void updateParameterValues( ParameterSet<T> parameterSet ) {
        parameterValues.addAll( parameterSet.getParameterValues() );
    }

    protected void updateLegalValues( ParameterSet<?> parameterSet, int[] parameterValueIndexes ) {
        for ( int i=0, j = getParameterValuesCount(); j < getParameterValuesCount() + parameterSet.getParameterValues().size(); i++, j++ ) {
            parameterValueIndexes[i] = j;
        }
        legalValues = ArrayUtils.addAll( legalValues, parameterValueIndexes );
    }

    /**
     * The total number of Parameter Sets under analysis. In a web form, this would be analogous to the number of fields
     * we're testing (possible values for User Type would be one Parameter Set, etc)
     * @return
     */
    public int getParameterSetCount() {
        return legalValues.length;
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
    private int[] parameterPositions = null; // The parameter position for a given value
    public int[] getParameterPositions() { return this.parameterPositions; }

    /**
     * The parameterPositions field (int[]) represents the "parameter position" for each given value. See above for details
     */
    public void updateParameterPositions() {
        int[] parameterPositions = new int[ this.getParameterValuesCount() ]; // the indexes tell us which parameter set the value belongs to

        int k = 0; //The index of the parameter set attached to this value
        for ( int i = 0; i < this.getLegalValues().length; ++i ) {
            int[] curr = this.getLegalValues()[i];
            for ( int j = 0; j < curr.length; ++j ) {
                parameterPositions[k++] = i;
            }
        }
        log.debug( "Parameter Positions: " + Arrays.toString( parameterPositions ) );
        this.parameterPositions = parameterPositions;  
    }

    /**
     * Logs the current Parameter values contained in this Scenario
     */
    public void logParameterValues() {
        log.debug( "Parameter Values: " + getParameterValues().toString() );
    }
 }
