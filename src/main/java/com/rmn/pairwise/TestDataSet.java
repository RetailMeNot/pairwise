package com.rmn.pairwise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TestDataSet {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Scenario scenario;
    private Inventory inventory;
    private List<List<Integer>> testSets = new ArrayList<List<Integer>>();
    public List<List<Integer>> getRawTestSets() { return testSets; }
    
    private Random r = new Random(2);

    public TestDataSet(Inventory inventory, Scenario scenario) {
        this.inventory = inventory;
        this.scenario = scenario;
    }
    
    public void buildTestCases() {
        int poolSize = 1; // number of candidate testSet arrays to generate before picking one to add to testSets List
        log.debug("Candidate Pool Size: {}", poolSize);
        while (inventory.getUnusedMolecules().size() > 0) { //keep iterating until all pairs are used
            // as long as there are unused pairs to account for
            log.debug("Unused Pair Count: {}", inventory.getUnusedMolecules().size());
            List<List<Integer>> candidateSets = new ArrayList<List<Integer>>(); // holds candidate testSets
    
            for (int candidate = 0; candidate < poolSize; ++candidate) {
                log.debug("Candidate: {}", candidate);
                List<Integer> testSet = getSingleTestSet();
                logCandidateTestSet(testSet);
                candidateSets.add(candidate, testSet);  // add candidate testSet to candidateSets array
            } // for each candidate testSet
            logCandidateTestSets(candidateSets);
            List<Integer> bestTestSet = determineBestCandidateSet(candidateSets);

            testSets.add(bestTestSet); // Add the best candidate to the main testSets List
            inventory.updateAllCounts(bestTestSet);
        } //while loop from hell
    }
    
    //This is hard-coded for Strings right now--should be able to refactor the generic back in later
    public List<Map<String, String>> getTestSets() {
        List<List<Integer>> testSetIndexes = getRawTestSets();
        List<Map<String, String>> completeDataSet = new ArrayList<Map<String, String>>();
        for (List<Integer> testSetIndex: testSetIndexes) {
            Map<String, String> singleTestSet = new LinkedHashMap<String, String>();
            for (int j = 0; j < scenario.getParameterSetCount(); j++) {
                String value = (String) scenario.getParameterValues().get(testSetIndex.get(j));
                singleTestSet.put(scenario.getParameterSet(scenario.getParameterPositions().get(testSetIndex.get(j))).getName(), value);
            }
            completeDataSet.add(singleTestSet);
        }
        return completeDataSet;
    }
    
    //It's hard to figure out how to break this up into smaller chunks--everything in inter-dependent
    protected List<Integer> getSingleTestSet() {
        List<Integer> bestMolecule = inventory.getBestMolecule();
        
        int firstPos = scenario.getParameterPositions().get(bestMolecule.get(0));  // position of first parameter set from best unused pair
        int secondPos = scenario.getParameterPositions().get(bestMolecule.get(1)); // position of second parameter set from best unused pair
        log.debug("The best pair belongs at positions {} and {}", firstPos, secondPos);
        
        // place two parameter values from best unused pair into candidate testSet
        List<Integer> testSet = new ArrayList<Integer>(); // make an empty candidate testSet
        testSet.add(firstPos, bestMolecule.get(0));
        testSet.add(secondPos, bestMolecule.get(1));

        List<Integer> ordering = getParameterOrdering(firstPos, secondPos);
        
        // for remaining parameter positions in candidate testSet, try each possible legal value, picking the one which captures the most unused pair
        for (int i = 2; i < scenario.getParameterSetCount(); i++) {
            int currPos = ordering.get(i);
            List<Integer> possibleValues = scenario.getLegalValues().get(currPos);
            logPossibleValues(currPos, possibleValues);
            
            int highestCount = 0;
            int bestJ = 0;
            for (int j=0; j < possibleValues.size(); j++) {
                int currentCount = 0;
                for (int p = 0; p < i; ++p) {
                    int[] candidatePair = new int[] { possibleValues.get(j), testSet.get(ordering.get(p)) };
                    if (inventory.getUnusedMoleculesSearch().get(candidatePair[0]).get(candidatePair[1]) == 1 ||
                        inventory.getUnusedMoleculesSearch().get(candidatePair[1]).get(candidatePair[0]) == 1)
                        ++currentCount;
                }
                if (currentCount > highestCount) {
                    highestCount = currentCount;
                    bestJ = j;
                  }
            }
            log.debug(String.format("Best possible value: [%d: %s], Parameter Set [%d: %s], Test Set Position %d", possibleValues.get(bestJ), scenario.getParameterValues().get(possibleValues.get(bestJ)), i, scenario.getParameterSet(currPos).getName(), currPos));
            testSet.add(currPos, possibleValues.get(bestJ));
        } // i -- each testSet position 

        return testSet;
    }
    
    protected List<Integer> determineBestCandidateSet(List<List<Integer>> candidateSets) {
        // Iterate through candidateSets to determine the best candidate
        r.setSeed(r.nextLong());
        int indexOfBestCandidate = r.nextInt(candidateSets.size()); // pick a random index as best
        int mostPairsCaptured = inventory.numberMoleculesCaptured(candidateSets.get(indexOfBestCandidate));
   
        // Determine "best" candidate to use
        for (int i = 0; i < candidateSets.size(); ++i) {
            int pairsCaptured = inventory.numberMoleculesCaptured(candidateSets.get(i));
            if (pairsCaptured > mostPairsCaptured) {
                mostPairsCaptured = pairsCaptured;
                indexOfBestCandidate = i;
            }
            log.debug("Candidate {} captured {}", i, mostPairsCaptured);
        }
        log.debug("Candidate number {} is best", indexOfBestCandidate);

        return candidateSets.get(indexOfBestCandidate);
    }
    
    protected List<Integer> getParameterOrdering(int firstPos, int secondPos) {
        // generate a random order to fill parameter positions
        List<Integer> ordering = new ArrayList<Integer>(scenario.getLegalValues().size());
        for (int i = 0; i < scenario.getLegalValues().size(); i++) { // initially all in order
            ordering.add(i, i);
        }
        
        // put firstPos at ordering[0] && secondPos at ordering[1]
        ordering.add(0, firstPos);
        ordering.add(firstPos, 0);
   
        int t = ordering.get(1);
        ordering.add(1,  secondPos);
        ordering.add(secondPos, t);
   
        // shuffle ordering[2] thru ordering[last]
        for (int i = 2; i < ordering.size(); i++) { // Knuth shuffle. start at i=2 because want first two slots left alone
            int j = r.nextInt(ordering.size() - i) + i;
            int temp = ordering.get(j);
            ordering.add(j, ordering.get(i));
            ordering.add(i, temp);
        }
        log.debug("Order: {}", ordering.toString());
        return ordering;
    }
    
    private void logCandidateTestSet(List<Integer> testSet) {
        log.debug("Adding candidate Test Molecules to candidateSets array: ");
        log.debug("Candidate Test Set (indexes): {}", testSet.toString());
        for ( int i = 0; i < testSet.size(); i++ ) {
            log.debug("Candidate Test Set: (parameter {}): {}", i, scenario.getParameterValues().get(testSet.get(i)));
        }
    }
    
    private void logPossibleValues(int paramSetIndex, List<Integer> possibleValues) {
        log.debug("Possible values are ");
        for (int z = 0; z < possibleValues.size(); z++) {
            log.debug(String.format("%d->%d: %s", paramSetIndex, possibleValues.get(z), scenario.getParameterValues().get(scenario.getLegalValues().get(paramSetIndex).get(z))));
        }
    }
    
    private void logCandidateTestSets(List<List<Integer>> candidateSets) {
        log.debug( "Candidate Test Molecules: " );
        for (int i = 0; i < candidateSets.size(); ++i) {
            List<Integer> curr = candidateSets.get(i);
            log.debug(String.format(" Parameter Set %d: Current: %s, Captures: %d", i, curr.toString(), inventory.numberMoleculesCaptured(curr)));
        }
    }

    public void logResults() {
        log.debug("Result Test Sets: ");
        for (int i = 0; i < testSets.size(); i++) {
            String outputStr = i + ": ";
            List<Integer> curr = testSets.get( i );
            for (int j = 0; j < scenario.getParameterSetCount(); j++) {
                outputStr += scenario.getParameterValues().get(curr.get(j)) + " ";
            }
            log.debug(outputStr);
        }
    }

    public void logFullCombinationCount() {
        log.info("All possible combinations: {}", inventory.getFullCombinationCount());
        log.info("      After set reduction: {}", this.getRawTestSets().size());
    }
}
