package com.rmn.pairwise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class PairwiseInventory implements Inventory {
    private Logger log = LoggerFactory.getLogger( this.getClass() );

    //********************************************
    //Parameter Set info and methods
    private Scenario scenario;
    public Scenario getScenario() { return scenario; }
    
    @Override
    public void setScenario(Scenario scenario) { this.scenario = scenario; }
    
    private List<Integer> unusedParameterIndexCounts;
    public List<Integer> getUnusedParameterIndexCounts() { return this.unusedParameterIndexCounts; }
    
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
     * The molecules that have not been used yet. As they are used, they get removed from this list
     */
    private List<Molecule> unusedMolecules = null;
    
    @Override
    public List<Molecule> getUnusedMolecules() { return unusedMolecules; }

    private List<List<Integer>> unusedMoleculesSearch = null;
    
    @Override
    public List<List<Integer>> getUnusedMoleculesSearch() { return unusedMoleculesSearch; };
    
    public void buildMolecules(int atomsPerMolecule) {
        List<Molecule> allMolecules = new ArrayList<Molecule>();
        List<Molecule> unusedMolecules = new ArrayList<Molecule>();          // List of pairs which have not yet been captured

        List<List<Integer>> unusedMoleculesSearch = new ArrayList<List<Integer>>(scenario.getParameterValuesCount());
        for (int parameterSet = 0; parameterSet < scenario.getLegalValues().size() - 1; parameterSet++) {
            for (int nextParameterValue = parameterSet + 1; nextParameterValue < scenario.getLegalValues().size(); nextParameterValue++) {
                List<Integer> firstRow = scenario.getLegalValues().get(parameterSet);
                List<Integer> secondRow = scenario.getLegalValues().get(nextParameterValue);

                for (int aFirstRow : firstRow) {
                    for (int aSecondRow : secondRow) {
                        List<Integer> atoms = new ArrayList<Integer>(scenario.getParameterValuesCount());
                        atoms.addAll(Arrays.asList(aFirstRow, aSecondRow));
                        Molecule molecule = new Molecule(atomsPerMolecule);
                        molecule.setAtoms(atoms);

                        unusedMolecules.add(molecule);
                        if (unusedMoleculesSearch.size() <= aFirstRow) {
                            unusedMoleculesSearch.add(aFirstRow, new ArrayList<Integer>(scenario.getParameterValuesCount()));
                        }
                        unusedMoleculesSearch.get(aFirstRow).add(1);
                        allMolecules.add(molecule);
                        logUnusedMolecules(unusedMolecules);
                    } // y
                } // x
                
            } // j
        } // i

        scenario.updateParameterPositions();
        this.allMolecules = allMolecules;
        this.unusedMolecules = unusedMolecules;
        this.unusedMoleculesSearch = unusedMoleculesSearch;
        this.processUnusedValues();
        this.logAllMolecules(this.getAllMolecules());
        this.logUnusedMolecules(this.getUnusedMolecules());
    }
    
    @Override
    public void buildMolecules() {
        this.buildMolecules(2);
    }

    @Override
    public void processUnusedValues() {
        List<Integer> unusedCounts = new ArrayList<Integer>(scenario.getParameterValuesCount());  // indexes are parameter values, cell values are counts of how many times the parameter value apperas in the analyzer.getUnusedPairs() collection
        for (Molecule molecule: this.getAllMolecules()) {
            if (unusedCounts.size() <= molecule.getAtoms().get(0)) {
                unusedCounts.add(molecule.getAtoms().get(0));
            }
            if (unusedCounts.size() <= molecule.getAtoms().get(1)) {
                unusedCounts.add(molecule.getAtoms().get(1));
            }

            unusedCounts.add(molecule.getAtoms().get(0), unusedCounts.get(molecule.getAtoms().get(0)) + 1);
            unusedCounts.add(molecule.getAtoms().get(1), unusedCounts.get(molecule.getAtoms().get(1)) + 1);
        }
        
        this.logUnusedMolecules(unusedMolecules);
        this.unusedParameterIndexCounts = unusedCounts;
    }
    
    @Override
    public void updateAllCounts(List<Integer> bestTestSet) {
        for (int i = 0; i <= scenario.getParameterSetCount() - 2; ++i) {
            for (int j = i + 1; j <= scenario.getParameterSetCount() - 1; ++j) {
                int v1 = bestTestSet.get(i); // value 1 of newly added pair
                int v2 = bestTestSet.get(j); // value 2 of newly added pair
   
                log.debug("Adjusting the unused counts for [{}][{}]", v1, v2);
                unusedParameterIndexCounts.set(v1, unusedParameterIndexCounts.get(v1) - 1);
                unusedParameterIndexCounts.set(v2, unusedParameterIndexCounts.get(v2) - 1);

                log.debug("Setting getUnusedMoleculesSearch() at [{}][{}] to 0", v1, v2);
                this.getUnusedMoleculesSearch().get(v1).set(v2, 0);
   
                //Set up a new list of unused molecules, then assign it back to the unusedMolecules field--otherwise we get a ConcurrentModificationException
                List<Molecule> tempUnusedMolecules = new ArrayList<Molecule>();
                tempUnusedMolecules.addAll(this.getUnusedMolecules());
                
                for (Molecule molecule: this.getUnusedMolecules()) {
                    List<Integer> curr = molecule.getAtoms();
   
                    //TODO this is a huge performance sink--we should build a map or lookup table and remove the molecules that way
                    if (curr.get(0) == v1 && curr.get(1) == v2) {
                        log.debug("Removing pair [{}, {}] from the Unused Molecule list", v1, v2);
                        tempUnusedMolecules.remove( molecule );
                    }
                }
                this.unusedMolecules = tempUnusedMolecules;
            } // j
        } // i
    }
    
    @Override
    public List<Integer> getBestMolecule() {
        //Weight the pair by looping through the unused set
        int bestWeight = 0;
        int indexOfBestMolecule = 0;
        for (int unusedMoleculeIndex = 0; unusedMoleculeIndex < this.getUnusedMolecules().size(); unusedMoleculeIndex++) {
            List<Integer> curr = this.getUnusedMolecules().get( unusedMoleculeIndex ).getAtoms();
            int weight = this.getUnusedParameterIndexCounts().get(curr.get(0)) + this.getUnusedParameterIndexCounts().get(curr.get(1));
            log.debug(String.format("Pair %d: [%s,%s], Weight: %2d", unusedMoleculeIndex, scenario.getParameterValues().get(curr.get(0)), scenario.getParameterValues().get (curr.get(1)), weight));
            
            //If the new pair is weighted more highly than the previous, make it the new "best"
            if (weight > bestWeight) {
                bestWeight = weight;
                indexOfBestMolecule = unusedMoleculeIndex;
            }
        }
         
        //log and return the best pair
        List<Integer> best = this.getUnusedMolecules().get(indexOfBestMolecule).getAtoms();
        log.debug(String.format("Best pair is [%s, %s] at %d with weight %d", scenario.getParameterValues().get(best.get(0)), scenario.getParameterValues().get(best.get(1)), indexOfBestMolecule, bestWeight));
        return best;
    }

    @Override
    public int numberMoleculesCaptured(List<Integer> testSet) {
        int moleculesCapturedCount = 0;
        for (int i = 0; i <= testSet.size() - 2; ++i) {
            for (int j = i + 1; j <= testSet.size() - 1; ++j) {
                if (unusedMoleculesSearch.get(testSet.get(i)).get(testSet.get(j)) == 1) {
                    ++moleculesCapturedCount;
                }
            }
        }
        return moleculesCapturedCount;
    }

    @Override
    public TestDataSet getTestDataSet() {
        TestDataSet dataSet = new TestDataSet(this, scenario);
        dataSet.buildTestCases();
        dataSet.logFullCombinationCount();
        return dataSet;
    }

   protected void logAllMolecules(List<Molecule> allMolecules) {
        log.debug("All Molecules:");
        int moleculeCount = 0;
        for (Molecule molecule: allMolecules) {
            log.debug( String.format("%03d: %s", moleculeCount, molecule.getAtoms().toString()));
            moleculeCount++;
        }
    }
    
    protected void logUnusedMolecules(List<Molecule> unusedMolecules) {
        String unusedPairsStr = "Unused Molecules: ";
        int moleculeCount = 0;
        for (Molecule molecule: unusedMolecules) {
            if (null != molecule) {
                List<Integer> curr = molecule.getAtoms();
                unusedPairsStr += molecule + ",";
                log.debug(String.format("%3d: %2d,  %2d", moleculeCount, curr.get(0), curr.get(1)));
            }
            moleculeCount++;
        }
        unusedPairsStr = unusedPairsStr.substring( 0, unusedPairsStr.length() - 1 );
        log.debug(unusedPairsStr);
    }
}