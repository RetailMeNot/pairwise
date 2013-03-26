package com.wsm.pairwise;

import java.util.List;

public interface IInventory {

    /**
     * Get a single test data set
     * @return
     */
    TestDataSet getTestDataSet();

    int numberMoleculesCaptured( int[] testSet );

    int[] getBestMolecule();

    void updateAllCounts( int[] bestTestSet );

    void processUnusedValues();

    void buildMolecules();

    int[][] getUnusedMoleculesSearch();

    List<Molecule> getUnusedMolecules();

    int initMoleculeCount();

    int getMoleculeCount();

    List<Molecule> getAllMolecules();

    long getFullCombinationCount();

    public abstract void setScenario( Scenario<?> scenario );

    Scenario<?> getScenario();

    void setAtomsPerMolecule( int atoms );
}