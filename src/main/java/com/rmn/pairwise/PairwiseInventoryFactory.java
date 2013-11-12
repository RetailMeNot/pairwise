package com.rmn.pairwise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PairwiseInventoryFactory {
    private static Logger log = LoggerFactory.getLogger( PairwiseInventoryFactory.class );

    /**
     * Go through the parameter sets to populate the list of ParameterSets we're going to use. These are the raw materials
     *  from which the test cases will be generated
     * @param contents The contents of the Scenario you're testing
     * @return The Scenario, fully populated
     */
    public static Scenario<String> generateScenario( String contents ) {
        Scenario<String> scenario = new Scenario<String>();
        for ( String line: StringUtils.split( contents, System.getProperty( "line.separator" ) ) ) {
            scenario.addParameterSet( processOneLine( line ) );
        }
        return scenario;
    }
    
    public static IInventory generateParameterInventory( String contents ) {
        IInventory inventory = new PairwiseInventory();
        Scenario<String> scenario = generateScenario( contents );
        inventory.setScenario( scenario );
        inventory.buildMolecules();
        return inventory;
    }

    public static IInventory generateParameterInventory( InputStream stream ) throws IOException {
        InputStreamReader isr = new InputStreamReader( stream );
        BufferedReader br = new BufferedReader( isr );
   
        Scenario<String> scenario = new Scenario<String>();
        String line;
        while ( ( line = br.readLine() ) != null ) {
            scenario.addParameterSet( processOneLine( line ) );
        }
        
        IInventory inventory = new PairwiseInventory();
        inventory.setScenario( scenario );
        inventory.buildMolecules();
        return inventory;
    }

    /**
     * Processes a single line of inputs
     * @param line One line, containing one parameter space (e.g. "Title: Value1, Value2, Value3")
     * @return The ParameterSet representing the line
     */
    public static ParameterSet<String> processOneLine( String line ) {
        log.debug( "Processing line: " + line );
        String[] lineTokens = line.split( ":", 2 );
        List<String> strValues = splitAndTrim( ",", lineTokens[1] );
        ParameterSet<String> parameterSet = new ParameterSet<String>( strValues );
        parameterSet.setName( lineTokens[0] );
        return parameterSet;
    }

    private static List<String> splitAndTrim( String regex, String lineTokens ) {
        String[] rawTokens = lineTokens.split( regex );
        String[] processedTokens = new String[ rawTokens.length ];
        for ( int i = 0; i< rawTokens.length; i++ ) {
            processedTokens[i] = StringUtils.trim( rawTokens[i] );
        }
        return Arrays.asList( processedTokens );
    }
}
