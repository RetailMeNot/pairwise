package com.rmn.pairwise;

import java.util.List;

public interface IInventory {
    /**
     * Returns the entire set of Test Cases this inventory has produced, by running through "the algorithm" after all the Parameter Sets
     * have been added
     * @return
     */
    TestDataSet getTestDataSet();

    /**
     * Returns the number of unused pairs still outstanding for the given test set (set of parameter indexes). If your test set is [2, 4, 7] as
     * in the above example, this will look at molecules [2, 4], [2, 7], and [4, 7], and determine which of those molecules has not been used yet.
     * In this example the answer would be 0, 1, 2, or 3, depending on how many have not previously been used
     * @param testSet
     * @return
     */
    int numberMoleculesCaptured(int[] testSet);

    /**
     * Pick "best" unused molecule -- the pair with the highest number of unused values
     * @return
     */
    int[] getBestMolecule();

    /**
     *
     * @param bestTestSet
     */
    void updateAllCounts(int[] bestTestSet);

    /**
     * Process the "used" sets to determine which sets have not been used yet
     */
    void processUnusedValues();

    /**
     *  Process the legalValues array to populate the allSets, unusedPairs, and unusedPairsSearch collections
     */
    void buildMolecules();

    int[][] getUnusedMoleculesSearch();

    List<Molecule> getUnusedMolecules();

    /**
     * Determine the number of pairs for this input set
     * @return
     */
    int initMoleculeCount();

    int getMoleculeCount();

    List<Molecule> getAllMolecules();

    /**
     * Returns the count of the number of possible combinations that could be generated from this parameter set
     * @return The number of possible combinations if we had to examine 100%
     */
    long getFullCombinationCount();

    public abstract void setScenario(Scenario scenario);

    Scenario getScenario();

    void setAtomsPerMolecule(int atoms);
}