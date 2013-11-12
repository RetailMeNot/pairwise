package com.rmn.pairwise;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Terms:
 *  Inventory: The full definition of all parameter sets to be used in a test scenario
 *  Scenario: A single set of values from one parameter definitions
 *  Molecule: A combination of values (atoms) from different parameter sets that must be tested (originally this was always a "pair", but we want to be able to do order-3, order-4, order-n combinations)
 *  Atom: A single value from a parameter set, the combination of which becomes "molecules"
 * 
 * Consider this parameter set:
 *  Param1: a, b, c
 *  Param2: i, j, k, l
 *  Param3: x, y
 * 
 * The fields within this class will be set as follows:
 *  
 *  parameterValues:          [ a, b, c, i, j, k, l, x, y ] --  A flattened out array of all possible values
 *  getParameterSetCount:     3 
 *  getParameterValuesCount:  9
 *  pairCount:         
 *  legalValues:              [ [ 0, 1, 2 ], [ 3, 4, 5, 6 ], [ 7, 8 ] ] -- A list of arrays representing the parameter set (x), and the pointer to the value of the flattened-out "parameterValues" array (y)
 *  allMolecules:           
 *  parameterPositions:       [ 0, 0, 0, 1, 1, 1, 1, 2, 2 ]  --  Allows us to look up which parameter set a given value is attached to, given the flattened out array of values
 *  one possible test set:    [ 2, 4, 7 ], representing one value (index) from each parameter set. This is conceivably a "test case"
 */
public class PairwiseInventory implements IInventory {
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    //********************************************
    //Parameter Set info and methods
    private Scenario<?> scenario;
    public Scenario<?> getScenario() { return scenario; }
    
    @Override
    public void setScenario( Scenario<?> scenario ) { this.scenario = scenario; }
    
    private int[] unusedParameterIndexCounts;
    public int[] getUnusedParameterIndexCounts() { return this.unusedParameterIndexCounts; }
    
    /**
     * Returns the count of the number of possible combinations that could be generated from this parameter set
     * @return 
     */
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
    
    /**
     * Determine the number of pairs for this input set
     * @return
     */
    @Override
    public int initMoleculeCount() {
        //TODO Needs to be adjusted for Order-N molecules. This can be done now, while we're still using just 2, and will serve
        // as a model for how the rest of it can be done. It won't get easier than this
        int moleculeCount = 0;
        for ( int i = 0; i < scenario.getLegalValues().length - 1; ++i ) {
            for ( int j = i + 1; j < scenario.getLegalValues().length; ++j ) {
                moleculeCount += ( scenario.getLegalValues()[i].length * scenario.getLegalValues()[j].length );
            }
        }
        log.debug( String.format( "Number of molecules: %d", moleculeCount ) );
        return moleculeCount;
    }
    
    /**
     * The molecules that have not been used yet. As they are used, they get removed from this list
     */
    private List<Molecule> unusedMolecules = null;
    
    @Override
    public List<Molecule> getUnusedMolecules() { return unusedMolecules; }

    private int[][] unusedMoleculesSearch = null;
    
    @Override
    public int[][] getUnusedMoleculesSearch() { return unusedMoleculesSearch; };
    
    public void buildMolecules( int atomsPerMolecule ) {
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
    
    /**
     *  Process the legalValues array to populate the allSets, unusedPairs, and unusedPairsSearch collections
     * @param inventory 
     */
    @Override
    public void buildMolecules() {
        this.buildMolecules( 2 );
    }

    /**
     * Process the "used" sets to determine which sets have not been used yet
     */
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
    
    /**
     * Pick "best" unused molecule -- the pair with the highest number of unused values
     * @return
     */
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

    /**
     * Returns the number of unused pairs still outstanding for the given test set (set of parameter indexes). If your test set is [2, 4, 7] as 
     * in the above example, this will look at molecules [2, 4], [2, 7], and [2, 7], and determine which of those molecules has not been used yet. 
     * In this example the answer would be 0, 1, 2, or 3, depending on how many have not previously been used
     * @param testSet
     * @return
     */
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

    /**
     * Returns the entire set of Test Cases this inventory has produced, by running through "the algorithm" after all the Parameter Sets
     * have been added
     * @return
     */
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
                log.debug( String.format( "%3d: %2d,  %2d", moleculeCount, curr[0], curr[1] ) );
            }
            moleculeCount++;
        }
        unusedPairsStr = unusedPairsStr.substring( 0, unusedPairsStr.length() - 1 );
        log.debug( unusedPairsStr );
    }

    public void setAtomsPerMolecule ( int atoms ) {
        log.warn( "This feature not available in a strictly pairwise environment--atoms will be set to 2" );
    }
}