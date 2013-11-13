package com.rmn.pairwise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Most of the documentation in the methods below will rely on this example for explanation
 *
 * Example:
 *
 * Atoms Per Molecule: 3
 *
 * Legal Values: 0: [ 0, 1, 2, 3, 4, 5, 6, 7, 8 ]
 *               1: [ 9, 10, 11, 12, 13, 14 ]
 *               2: [ 15, 16, 17 ]
 *               3: [ 18, 19, 20, 21, 22 ]
 *               4: [ 23, 24, 25, 26 ]
 *               5: [ 27, 28 ]
 *               6: [ 29, 30, 31, 32, 33 ]
 *
 */
public class OrderNInventory implements IInventory {
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    private int atomsPerMolecule;

    /**
     * This will attempt to validate the int received as much as possible before setting the atomsPerMolecule. It defaults
     * to 2 in most cases, but if you specify more Atoms than there are Parameter Sets, it will just throw an exception
     * If you want ALL possible combinations (i.e. no set reduction), just set this to 1
     * @param atomsPerMolecule The number of desired Atoms per Molecule (the "Order-N" number you're looking for). Defaults to 2, for "pairwise"
     */
    public void setAtomsPerMolecule( int atomsPerMolecule ) {
        if ( null == scenario ) {
            log.error( "The scenario has not yet been specified. Defaulting to 2" );
            this.atomsPerMolecule = 2;
            return;
        }
        if ( atomsPerMolecule == 1 ) {
            //When you want ALL possible combinations, no set reduction
            this.atomsPerMolecule = 1;
        }
        if ( atomsPerMolecule < 1 ) {
            log.error(String.format("Cannot specify fewer than 2 atoms per test molecule (you specified %d). Defaulting to 2", atomsPerMolecule));
            this.atomsPerMolecule = 2;
            return;
        }

        if ( atomsPerMolecule > scenario.getParameterSetCount() ) {
            throw new IllegalArgumentException( String.format( "Cannot specify more than %d atoms per test molecule. You cannot have more atoms than there are parameter sets!", scenario.getParameterSetCount() ) );
        }
        this.atomsPerMolecule = atomsPerMolecule;
    }

    /**
     * @return The number of atoms per molecule desired in the final set of Test Cases
     */
    public int getAtomsPerMolecule() {
        return atomsPerMolecule;
    }

    //********************************************
    //Scenario info and methods
    private Scenario<?> scenario;
    public Scenario<?> getScenario() { return scenario; }
    public void setScenario( Scenario<?> scenario ) { this.scenario = scenario; }
    
    private int[] unusedParameterIndexCounts;
    public int[] getUnusedParameterIndexCounts() { return this.unusedParameterIndexCounts; }
    
    @Override
    public long getFullCombinationCount() {
        long count = 1;
        //Just multiply out all the parameters, X * Y * Z
        for ( ParameterSet<?> set: scenario.getParameterSets() ) {
            count *= set.getParameterValues().size();
        }
        return count;
    }

    //********************************************
    // Molecule info and methods
    private List<Molecule> allMolecules = null;
    
    @Override
    public List<Molecule> getAllMolecules() { return allMolecules; }
    
    @Override
    public int getMoleculeCount() { return allMolecules.size(); }

    @Override
    public int initMoleculeCount() {
        //recursive version, necessary to handle N atoms per molecule
        //Call the recursive method with the scenario
        List<int[]> moleculeSets = new ArrayList<int[]>();
        moleculeSets.addAll( calculateMoleculeSets() );

        log.info(String.format("Number of molecules: %d", moleculeSets.size()));
        return moleculeSets.size();
    }

    /**
     * Figure out the number of Parameter Set combinations--not the actual test molecules, but the ways in which the parameter sets
     * need to be combined. This will greatly simplify the molecule generation, leaving us fewer nests of for loops in the end
     * @return
     */
    private List<int[]> calculateMoleculeSets() {
        List<int[]> initialSets = getParameterPairCombinations( scenario.getParameterSetCount() );

        //Start with pairs, combine with one other value at a time (so we're only ever comparing two things)
        return getNewSets( initialSets );
    }

    /**
     * This does not calculate the actual pairs, rather, the list of pairs of Parameter Sets--all the
     * ways in which 2 parameter sets might interact with each other
     * @param parameterIndexes The number of Parameter Sets there are
     * @return A list of tuples--all possible ways in which 2 Parameter Sets can interact
     */
    private List<int[]> getParameterPairCombinations( int parameterIndexes ) {
        //seed pairs first, then send to the grinder
        List<int[]> initialSets = new ArrayList<int[]>();
        for ( int firstIndex = 0; firstIndex < parameterIndexes - 1; firstIndex++ ) {
            for ( int secondIndex = firstIndex + 1; secondIndex < parameterIndexes; secondIndex++ ) {
                int[] pair = { firstIndex, secondIndex };
                initialSets.add( pair );
                log.info( String.format( "[%d %d]", pair[0], pair[1] ) );
            }
        }
        log.info( String.format( "%d pairs total", initialSets.size() ) );
        return initialSets;
    }

    /**
     * @return The list of all Parameter Indexes (this will always be an int array of 0..X. I'd love to find a better way to generate it)
     */
    public List<Integer> getParameterIndexList() {
        //Initialize the "master" list of integers (basically just 0 .. parameterSetCount)
        List<Integer> allValues = new ArrayList<Integer>();
        for ( int index = 0; index < scenario.getParameterSetCount(); index++ ) {
            allValues.add( index );
        }
        return allValues;
    }

    /**
     * Calculates the Parameter Set combinations for this scenario--not the Molecules, but the different ways in which the
     * Parameter Sets can be combined for the given number of Atoms per Molecule
     * @param initialSets
     * @return
     */
    protected List<int[]> getNewSets( List<int[]> initialSets ) {
        //I don't like calling this more than once per run, but this cleans up the getNewSets method signature, and is only called once per
        // Parameter Set, in theory
        List<Integer> allValues = getParameterIndexList();

        //Evaluate whether or not we need to call into here again. If we do, figure out what values need to be sent, then send it
        // If we don't need to call again, commence The Algorithm
        if ( initialSets.get( 0 ).length == atomsPerMolecule ) {
            log.info( "The size of the sets we're looking at is equal to the number of atoms per molecule" );
            return initialSets;
        }
        log.info( String.format( "Set size: %d, atoms per molecule: %d. Now going to work on sets with size %d", initialSets.get( 0 ).length, atomsPerMolecule, initialSets.get( 0 ).length + 1 ) );

        //The Algorithm
        // Take the initial Molecule Set given, and for each set, generate all combinations of that set with *one* other Parameter Set index
        List<int[]> newSets = new ArrayList<int[]>();
        for ( int[] set: initialSets ) {
            //remove the values from the set under test
            List<Integer> values = purgeValuesFromList(allValues, set);

            //Now, step through the remaining "unused" values, and create a new set (the set under test plus the value we're currently "studying")
            for ( int value: values ) {
                extractNewSet(newSets, set, value);
            }
        }

        if ( newSets.get( 0 ).length < atomsPerMolecule ) {
            log.info( String.format( "Just finished sets with size %d. Now working on size %d", newSets.get( 0 ).length, newSets.get( 0 ).length + 1 ) );
            return getNewSets( newSets );
        }

        return newSets;
    }

    protected void extractNewSet(List<int[]> newSets, int[] set, int value) {
        //Initialize this new set to have the same length as the old set plus one--the new value we're adding
        int[] newSet = new int[set.length + 1];
        System.arraycopy(set, 0, newSet, 0, set.length);
        newSet[set.length] = value;
        newSets.add( newSet );
        log.info( String.format( "New Set: " + Arrays.toString( newSet ) ) );
    }

    /**
     * Remove a set of "used" values from the set of all values. Allows us to keep a running tally of what has
     *  and what hasn't been used
     * @param allValues The complete list of all value indices
     * @param set The set of indices to remove from the list
     * @return
     */
    protected List<Integer> purgeValuesFromList(List<Integer> allValues, int[] set) {
        //Sort the array so we can remove them from the list in the correct order
        Arrays.sort( set );

        List<Integer> values = new ArrayList<Integer>();
        values.addAll( allValues );

        log.info(String.format( String.format( "Before we remove the values: values: [%s], set: [%s]",  values.toString(), Arrays.toString(set) ) ) );
        for ( int singleValue = set.length - 1; singleValue >= 0; singleValue-- ) {
            if ( set[singleValue] >= values.size() ) {
                log.error( "Somehow, I've been asked to remove a value from the Parameter Set indexes which is out of bounds--this should not be possible" );
                log.error( String.format( "Value: %d, Values Array Size: %d", set[singleValue], values.size() ) );
                throw new IndexOutOfBoundsException( String.format( "Value: %d, Values Array Size: %d", set[singleValue], values.size() ) );
            }
            values.remove( set[singleValue] );
            log.info(String.format( String.format( "Removing value [%d: %d] from allValues, leaving [%s]",  singleValue, set[singleValue], values.toString() ) ) );
        }
        log.info( String.format( "Values array has been set: %s", values.toString() ) );
        return values;
    }

     // The molecules that have not been used yet. As they are used, they get removed from this list
    private List<Molecule> unusedMolecules = null;

    @Override
    public List<Molecule> getUnusedMolecules() { return unusedMolecules; }

    private int[][] unusedMoleculesSearch = null;
    
    @Override
    public int[][] getUnusedMoleculesSearch() { return unusedMoleculesSearch; };

    @Override
    public void buildMolecules() {
        List<Molecule> allMolecules = new ArrayList<Molecule>();
        List<Molecule> unusedMolecules = new ArrayList<Molecule>();          // List of pairs which have not yet been captured
        
        //TODO Needs to be adjusted for Order-N molecules
        int[][] unusedMoleculesSearch = new int[ scenario.getParameterValuesCount() ][ scenario.getParameterValuesCount() ];
        for ( int parameterSet = 0; parameterSet < scenario.getLegalValues().length - 1; parameterSet++ ) {
            for ( int nextParameterValue = parameterSet + 1; nextParameterValue < scenario.getLegalValues().length; nextParameterValue++ ) {
                int[] firstRow = scenario.getLegalValues()[parameterSet];
                int[] secondRow = scenario.getLegalValues()[nextParameterValue];
                
                for ( int x = 0; x < firstRow.length; ++x ) {
                    for ( int y = 0; y < secondRow.length; ++y ) {
                        int[] atoms = new int[] { firstRow[x], secondRow[y] };
                        Molecule molecule = new Molecule( atomsPerMolecule );
                        molecule.setAtoms( atoms );
                        
                        unusedMolecules.add( molecule );
                        unusedMoleculesSearch[ firstRow[x] ][ secondRow[y] ] = 1;
                        allMolecules.add( molecule );
                        logUnusedMolecules( unusedMolecules );
                    } // y
                } // x
                
            } // j
        } // i

        scenario.updateParameterPositions();
        this.allMolecules = allMolecules;
        this.unusedMolecules = unusedMolecules;
        this.unusedMoleculesSearch = unusedMoleculesSearch;
        this.processUnusedValues();
        this.logAllMolecules( this.getAllMolecules() );
        this.logUnusedMolecules( this.getUnusedMolecules() );
    }
    
    @Override
    public void processUnusedValues() {
        //TODO Needs to be adjusted for Order-N molecules
        int[] unusedCounts = new int[scenario.getParameterValuesCount()];  // indexes are parameter values, cell values are counts of how many times the parameter value apperas in the analyzer.getUnusedPairs() collection
        for ( Molecule molecule: this.getAllMolecules() ) {
            ++unusedCounts[ molecule.getAtoms()[0] ];
            ++unusedCounts[ molecule.getAtoms()[1] ];
        }
        
        this.logUnusedMolecules( unusedMolecules );
        this.unusedParameterIndexCounts = unusedCounts;
    }
    
    @Override
    public void updateAllCounts( int[] bestTestSet ) {
        //TODO Needs to be adjusted for Order-N molecules
        for ( int i = 0; i <= scenario.getParameterSetCount() - 2; ++i ) {
            for ( int j = i + 1; j <= scenario.getParameterSetCount() - 1; ++j ) {
                int v1 = bestTestSet[i]; // value 1 of newly added pair
                int v2 = bestTestSet[j]; // value 2 of newly added pair
   
                log.debug( String.format( "Adjusting the unused counts for [%d][%d]", v1, v2 ) );
                --unusedParameterIndexCounts[v1];
                --unusedParameterIndexCounts[v2];
   
                log.debug( String.format( "Setting getUnusedMoleculesSearch() at [%d][%d] to 0",  v1, v2 ) );
                this.getUnusedMoleculesSearch()[v1][v2] = 0;
   
                //Set up a new list of unused molecules, then assign it back to the unusedMolecules field--otherwise we get a ConcurrentModificationException
                List<Molecule> tempUnusedMolecules = new ArrayList<Molecule>();
                tempUnusedMolecules.addAll( this.getUnusedMolecules() );
                
                for ( Molecule molecule: this.getUnusedMolecules() ) {
                    int[] curr = molecule.getAtoms();
   
                    //TODO this is a huge performance sink--we should build a map or lookup table and remove the molecules that way
                    if ( curr[0] == v1 && curr[1] == v2 ) {
                        log.debug( String.format( "Removing pair [%d, %d] from the Unused Molecole list", v1, v2 ) );
                        tempUnusedMolecules.remove( molecule );
                    }
                }
                this.unusedMolecules = tempUnusedMolecules;
            } // j
        } // i
    }
    
    @Override
    public int[] getBestMolecule() {
        //TODO Needs to be adjusted for Order-N molecules

        //Weight the pair by looping through the unused set
        int bestWeight = 0;
        int indexOfBestMolecule = 0;
        for ( int unusedMoleculeIndex = 0; unusedMoleculeIndex < this.getUnusedMolecules().size(); unusedMoleculeIndex++ ) {
            int[] curr = this.getUnusedMolecules().get( unusedMoleculeIndex ).getAtoms();
            int weight = this.getUnusedParameterIndexCounts()[ curr[0] ] + this.getUnusedParameterIndexCounts()[ curr[1] ];
            log.debug( String.format( "Pair %d: [%s,%s], Weight: %2d", unusedMoleculeIndex, scenario.getParameterValues().get( curr[0] ), scenario.getParameterValues().get ( curr[1] ), weight ) );
            
            //If the new pair is weighted more highly than the previous, make it the new "best"
            if ( weight > bestWeight ) {
                bestWeight = weight;
                indexOfBestMolecule = unusedMoleculeIndex;
            }
        }
         
        //log and return the best pair
        int[] best = this.getUnusedMolecules().get( indexOfBestMolecule ).getAtoms();
        log.debug( String.format( "Best pair is [%s, %s] at %d with weight %d", scenario.getParameterValues().get( best[0] ), scenario.getParameterValues().get( best[1] ), indexOfBestMolecule, bestWeight ) );
        return best;
    }

    @Override
    public int numberMoleculesCaptured( int[] testSet ) {
        //TODO Needs to be adjusted for Order-N molecules

        int moleculesCapturedCount = 0;
        for ( int i = 0; i <= testSet.length - 2; ++i ) {
            for ( int j = i + 1; j <= testSet.length - 1; ++j ) {
                if ( unusedMoleculesSearch[ testSet[i] ][ testSet[j] ] == 1 ) {
                    ++moleculesCapturedCount;
                }
            }
        }
        return moleculesCapturedCount;
    }

    @Override
    public TestDataSet getTestDataSet() {
        TestDataSet dataSet = new TestDataSet( this, scenario );
        dataSet.buildTestCases();
        dataSet.logFullCombinationCount();
        return dataSet;
    }

   protected void logAllMolecules( List<Molecule> allMolecules ) {
        log.debug( "All Molecules:" );
        int moleculeCount = 0;
        for ( Molecule molecule: allMolecules ) {
            log.debug( String.format( "%03d: %s", moleculeCount, molecule.getAtoms().toString() ) );
            moleculeCount++;
        }
    }

   protected void logUnusedMolecules( List<Molecule> unusedMolecules ) {
       //TODO Needs to be adjusted for Order-N molecules
       String unusedPairsStr = "Unused Molecules: ";
       int moleculeCount = 0;
       for ( Molecule molecule: unusedMolecules ) {
           if ( null != molecule ) {
               int[] curr = molecule.getAtoms();
               unusedPairsStr += molecule + ",";
               log.debug( String.format( "%03d: %2d,  %2d", moleculeCount, curr[0], curr[1] ) );
           }
           moleculeCount++;
       }
       unusedPairsStr = unusedPairsStr.substring( 0, unusedPairsStr.length() - 1 );
       log.debug( unusedPairsStr );
   }
}