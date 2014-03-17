package com.rmn.pairwise;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class PairwiseInventoryTests {

    @Test
    public void testLegalValues() {
        //Analyze the parameters, then pick a random sampling of the legal values from the PARAMETER_SET and make sure they're correct
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory(InventoryFactoryTests.PARAMETER_SET);
        int[][] legalValues = inventory.getScenario().getLegalValues();
        
        Assert.assertEquals("Parameter [1][0] should be 2",   2, legalValues[1][0]);
        Assert.assertEquals("Parameter [2][2] should be 8",   8, legalValues[2][2]);
        Assert.assertEquals("Parameter [3][1] should be 10", 10, legalValues[3][1]);
    }

    @Test
    public void testProcessParameterPositions() {
        IInventory inventory = new PairwiseInventory();
        
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split(InventoryFactoryTests.PARAMETER_SET, System.getProperty("line.separator"))) {
            scenario.addParameterSet(PairwiseInventoryFactory.processOneLine(line));
        }
        
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        
        inventory.getScenario().updateParameterPositions();
        Assert.assertEquals("The parameter index at position 6 should be 2 (indicating that it is the third value in that particular set)", 2, inventory.getScenario().getParameterPositions()[6]);
    }
    
    @Test
    public void testInitMoleculeCountFull() {
        IInventory inventory = new PairwiseInventory();
        
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split(InventoryFactoryTests.PARAMETER_SET, System.getProperty("line.separator"))) {
            scenario.addParameterSet(PairwiseInventoryFactory.processOneLine(line));
        }
        
        inventory.setScenario(scenario);
        inventory.buildMolecules();

        Assert.assertEquals("There should be 44 total pairs", 44, inventory.getMoleculeCount());
    }

    @Test
    public void testInitPairCountTiny() {
        IInventory inventory = new PairwiseInventory();
        
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split(InventoryFactoryTests.TWO_X_THREE_SET, System.getProperty("line.separator"))) {
            scenario.addParameterSet(PairwiseInventoryFactory.processOneLine(line));
        }
        
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        
        Assert.assertEquals("There should be 6 pairs", 6, inventory.getMoleculeCount());
    }
    
    @Test
    public void testInitPairCountOneToMany() {
        IInventory inventory = new PairwiseInventory();
        
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split( "Param0: x\nParam1: i, j, k, l, m, n, o, p", System.getProperty("line.separator"))) {
            scenario.addParameterSet(PairwiseInventoryFactory.processOneLine(line));
        }
        
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        
        Assert.assertEquals("There should be 8 pairs", 8, inventory.getMoleculeCount());
    }
    
    @Test
    public void testInitPairCountManyToOne() {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory("Param1: i, j, k, l, m, n, o, p\nParam0: a");
        inventory.buildMolecules();
        Assert.assertEquals("There should be 8 pairs", 8, inventory.getMoleculeCount());
    }
    
    @Test
    public void testGenerateTinySetPairs() {
        IInventory inventory = new PairwiseInventory();
        
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split(InventoryFactoryTests.TWO_X_THREE_SET, System.getProperty("line.separator"))) {
            scenario.addParameterSet(PairwiseInventoryFactory.processOneLine(line));
        }
        
        inventory.setScenario(scenario);
        inventory.buildMolecules();

        Assert.assertEquals("The pair count should be 6", 6, inventory.getMoleculeCount());
    }

    @Test
    public void testOrderNInventory4Atoms() {
        IInventory inventory = new OrderNInventory();
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split(InventoryFactoryTests.BIG_PARAMETER_SET, System.getProperty("line.separator"))) {
            scenario.addParameterSet(PairwiseInventoryFactory.processOneLine(line));
        }
        inventory.setScenario(scenario);
        inventory.setAtomsPerMolecule(4);

        int moleculeSetCount = inventory.initMoleculeCount();
        Assert.assertEquals("With the Big Parameter set and an atom size of 4, there should be 420 molecule sets", 420, moleculeSetCount);
    }

    @Test
    public void testOrderNInventory3Atoms() {
        IInventory inventory = new OrderNInventory();
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split(InventoryFactoryTests.BIG_PARAMETER_SET, System.getProperty("line.separator"))) {
            scenario.addParameterSet(PairwiseInventoryFactory.processOneLine(line));
        }
        inventory.setScenario(scenario);
        inventory.setAtomsPerMolecule(3);

        int moleculeSetCount = inventory.initMoleculeCount();
        Assert.assertEquals("With the Big Parameter set and an atom size of 3, there should be 420 molecule sets", 105, moleculeSetCount);
    }
}
