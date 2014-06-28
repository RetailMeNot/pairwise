package com.rmn.pairwise;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PairwiseInventoryTests {
    @Test
    public void testLegalValues() {
        //Analyze the parameters, then pick a random sampling of the legal values from the PARAMETER_SET and make sure they're correct
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory(InventoryFactoryTests.PARAMETER_SET);
        List<List<Integer>> legalValues = inventory.getScenario().getLegalValues();
        
        Assert.assertEquals("Parameter [1][0] should be 2",   Integer.valueOf(2), legalValues.get(1).get(0));
        Assert.assertEquals("Parameter [2][2] should be 8",   Integer.valueOf(8), legalValues.get(2).get(2));
        Assert.assertEquals("Parameter [3][1] should be 10",  Integer.valueOf(10), legalValues.get(3).get(1));
    }

    @Test
    public void testProcessParameterPositions() {
        Inventory inventory = new PairwiseInventory();
        
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split(InventoryFactoryTests.PARAMETER_SET, System.getProperty("line.separator"))) {
            scenario.addParameterSet(PairwiseInventoryFactory.processOneLine(line));
        }
        
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        
        inventory.getScenario().updateParameterPositions();
        Assert.assertEquals("The parameter index at position 6 should be 2 (indicating that it is the third value in that particular set)", Integer.valueOf(2), inventory.getScenario().getParameterPositions().get(6));
    }
    
    @Test
    public void testInitMoleculeCountFull() {
        Inventory inventory = new PairwiseInventory();
        
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
        Inventory inventory = new PairwiseInventory();
        
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
        Inventory inventory = new PairwiseInventory();
        
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
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory("Param1: i, j, k, l, m, n, o, p\nParam0: a");
        inventory.buildMolecules();
        Assert.assertEquals("There should be 8 pairs", 8, inventory.getMoleculeCount());
    }
    
    @Test
    public void testGenerateTinySetPairs() {
        Inventory inventory = new PairwiseInventory();
        
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split(InventoryFactoryTests.TWO_X_THREE_SET, System.getProperty("line.separator"))) {
            scenario.addParameterSet(PairwiseInventoryFactory.processOneLine(line));
        }
        
        inventory.setScenario(scenario);
        inventory.buildMolecules();

        Assert.assertEquals("The pair count should be 6", 6, inventory.getMoleculeCount());
    }
}
