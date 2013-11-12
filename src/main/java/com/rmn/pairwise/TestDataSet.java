package com.rmn.pairwise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TestDataSet {
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    private Scenario<?> scenario;
    private IInventory inventory;
    private List<int[]> testSets = new ArrayList<int[]>();
    public List<int[]> getRawTestSets() { return testSets; }
    
    private Random r = new Random(2);

    public TestDataSet( IInventory inventory, Scenario<?> scenario ) {
        this.inventory = inventory;
        this.scenario = scenario;
    }
    
    public void buildTestCases() {
        int poolSize = 1; // number of candidate testSet arrays to generate before picking one to add to testSets List
        log.debug( String.format( "Candidate Pool Size: %d", poolSize ) );
        while ( inventory.getUnusedMolecules().size() > 0 ) { //keep iterating until all pairs are used
            // as long as there are unused pairs to account for
            log.debug( String.format( "Unused Pair Count: %d", inventory.getUnusedMolecules().size() ) );
            int[][] candidateSets = new int[poolSize][scenario.getParameterSetCount()]; // holds candidate testSets
    
            for ( int candidate = 0; candidate < poolSize; ++candidate ) {
                log.debug( String.format( "Candidate: %d", candidate ) );
                int[] testSet = getSingleTestSet();
                logCandidateTestSet(testSet);
                candidateSets[candidate] = testSet;  // add candidate testSet to candidateSets array
            } // for each candidate testSet
            logCandidateTestSets( candidateSets );
            int[] bestTestSet = determineBestCandidateSet(candidateSets);

            testSets.add( bestTestSet ); // Add the best candidate to the main testSets List
            inventory.updateAllCounts( bestTestSet );
        } //while loop from hell
    }
    
    //This is hard-coded for Strings right now--should be able to refactor the generic back in later
    public List<Map<String, String>> getTestSets() {
        List<int[]> testSetIndexes = getRawTestSets();
        List<Map<String, String>> completeDataSet = new ArrayList<Map<String, String>>();
        for ( int i = 0; i < testSetIndexes.size(); i++ ) {
            Map<String, String> singleTestSet = new LinkedHashMap<String, String>();
            int[] curr = testSetIndexes.get( i );
            for ( int j = 0; j < scenario.getParameterSetCount(); j++ ) {
                String value = ( String ) scenario.getParameterValues().get( curr[j] );
                singleTestSet.put( scenario.getParameterSet( scenario.getParameterPositions()[curr[j]] ).getName(), value );
            }
            completeDataSet.add( singleTestSet );
        }
        return completeDataSet;
    }
    
    //It's hard to figure out how to break this up into smaller chunks--everything in inter-dependent
    protected int[] getSingleTestSet() {
        int[] bestMolecule = inventory.getBestMolecule();
        
        int firstPos = scenario.getParameterPositions()[ bestMolecule[0] ];  // position of first parameter set from best unused pair
        int secondPos = scenario.getParameterPositions()[ bestMolecule[1] ]; // position of second parameter set from best unused pair
        log.debug( String.format( "The best pair belongs at positions %d and %d", firstPos, secondPos ) );
        
        // place two parameter values from best unused pair into candidate testSet
        int[] testSet = new int[scenario.getParameterSetCount()]; // make an empty candidate testSet
        testSet[firstPos] = bestMolecule[0];
        testSet[secondPos] = bestMolecule[1];

        int[] ordering = getParameterOrdering( firstPos, secondPos );
        
        // for remaining parameter positions in candidate testSet, try each possible legal value, picking the one which captures the most unused pair
        for ( int i = 2; i < scenario.getParameterSetCount(); i++ ) {
            int currPos = ordering[i];
            int[] possibleValues = scenario.getLegalValues()[currPos];
            logPossibleValues( currPos, possibleValues );
            
            int highestCount = 0;
            int bestJ = 0;
            for ( int j=0; j < possibleValues.length; j++ ) {
                int currentCount = 0;
                for (int p = 0; p < i; ++p) {
                    int[] candidatePair = new int[] { possibleValues[j], testSet[ordering[p]] };
                    if ( inventory.getUnusedMoleculesSearch()[candidatePair[0]][candidatePair[1]] == 1 ||
                         inventory.getUnusedMoleculesSearch()[candidatePair[1]][candidatePair[0]] == 1)
                        ++currentCount;
                }
                if (currentCount > highestCount) {
                    highestCount = currentCount;
                    bestJ = j;
                  }
            }
            log.debug( String.format( "Best possible value: [%d: %s], Parameter Set [%d: %s], Test Set Position %d", possibleValues[bestJ], scenario.getParameterValues().get( possibleValues[bestJ] ), i, scenario.getParameterSet( currPos ).getName(), currPos ) );
            testSet[currPos] = possibleValues[bestJ];
        } // i -- each testSet position 

        return testSet;
    }
    
    protected int[] determineBestCandidateSet( int[][] candidateSets ) {
        // Iterate through candidateSets to determine the best candidate
        r.setSeed( r.nextLong() );
        int indexOfBestCandidate = r.nextInt( candidateSets.length ); // pick a random index as best
        int mostPairsCaptured = inventory.numberMoleculesCaptured( candidateSets[indexOfBestCandidate] );
   
        // Determine "best" candidate to use
        for (int i = 0; i < candidateSets.length; ++i) {
            int pairsCaptured = inventory.numberMoleculesCaptured( candidateSets[i] );
            if (pairsCaptured > mostPairsCaptured) {
                mostPairsCaptured = pairsCaptured;
                indexOfBestCandidate = i;
            }
            log.debug( String.format( "Candidate %d captured %d", i, mostPairsCaptured ) );
        }
        log.debug( String.format( "Candidate number %d is best", indexOfBestCandidate ) );
        
        int[] bestTestSet = candidateSets[indexOfBestCandidate];
        return bestTestSet;
    }
    
    protected int[] getParameterOrdering( int firstPos, int secondPos ) {
        // generate a random order to fill parameter positions
        int[] ordering = new int[scenario.getLegalValues().length];
        for ( int i = 0; i < scenario.getLegalValues().length; i++ ) { // initially all in order
            ordering[i] = i;
        }
        
        // put firstPos at ordering[0] && secondPos at ordering[1]
        ordering[0] = firstPos;
        ordering[firstPos] = 0;
   
        int t = ordering[1];
        ordering[1] = secondPos;
        ordering[secondPos] = t;
   
        // shuffle ordering[2] thru ordering[last]
        for ( int i = 2; i < ordering.length; i++ ) { // Knuth shuffle. start at i=2 because want first two slots left alone
            int j = r.nextInt( ordering.length - i ) + i;
            int temp = ordering[j];
            ordering[j] = ordering[i];
            ordering[i] = temp;
        }
        log.debug( String.format( "Order: %s", Arrays.toString( ordering ) ) );
        return ordering;
    }
    
    private void logCandidateTestSet( int[] testSet ) {
        log.debug("Adding candidate Test Molecules to candidateSets array: ");
        log.debug( "Candidate Test Set (indexes): " + Arrays.toString( testSet ) );
        for ( int i = 0; i < testSet.length; i++ ) {
            log.debug( String.format( "Candidate Test Set: (parameter %d): %s", i, scenario.getParameterValues().get( testSet[i] ) ) );
        }
    }
    
    private void logPossibleValues( int paramSetIndex, int[] possibleValues ) {
        log.debug("Possible values are ");
        for ( int z = 0; z < possibleValues.length; z++ ) {
            log.debug( String.format( "%d->%d: %s", paramSetIndex, possibleValues[z], scenario.getParameterValues().get( scenario.getLegalValues()[paramSetIndex][z] ) ) );
        }
    }
    
    private void logCandidateTestSets( int[][] candidateSets ) {
        log.debug( "Candidate Test Molecules: " );
        for ( int i = 0; i < candidateSets.length; ++i ) {
            int[] curr = candidateSets[i];
            log.debug( String.format( " Parameter Set %d: Current: %s, Captures: %d", i, Arrays.toString( curr ), inventory.numberMoleculesCaptured( curr ) ) );
        }
    }

    public void logResults() {
        log.debug( "Result Test Sets: " );
        for ( int i = 0; i < testSets.size(); i++ ) {
            String outputStr = i + ": ";
            int[] curr = testSets.get( i );
            for ( int j = 0; j < scenario.getParameterSetCount(); j++ ) {
                outputStr += scenario.getParameterValues().get( curr[j] ) + " ";
            }
            log.debug( outputStr );
        }
    }

    public void logFullCombinationCount() {
        log.info( String.format( "All possible combinations: %d", inventory.getFullCombinationCount() ) );
        log.info( String.format( "      After set reduction: %d", this.getRawTestSets().size() ) );
    }
}
