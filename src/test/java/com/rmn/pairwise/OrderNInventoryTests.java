package com.rmn.pairwise;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class OrderNInventoryTests {

    @Test
    public void testLegalValues() {
        //Analyze the parameters, then pick a random sampling of the legal values from the PARAMETER_SET and make sure they're correct
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory( InventoryFactoryTests.PARAMETER_SET );
        int[][] legalValues = inventory.getScenario().getLegalValues();
        
        Assert.assertEquals( "Parameter [1][0] should be 2",   2, legalValues[1][0] );
        Assert.assertEquals( "Parameter [2][2] should be 8",   8, legalValues[2][2] );
        Assert.assertEquals( "Parameter [3][1] should be 10", 10, legalValues[3][1] );
    }

    @Test
    public void testProcessParameterPositions() {
        IInventory inventory = new PairwiseInventory();

        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.PARAMETER_SET ) );
        inventory.buildMolecules();
        
        inventory.getScenario().updateParameterPositions();
        Assert.assertEquals( "The parameter index at position 6 should be 2 (indicating that it is the third value in that particular set)", 2, inventory.getScenario().getParameterPositions()[6] );
    }
    
    @Test
    public void testInitMoleculeCountFull() {
        IInventory inventory = new PairwiseInventory();

        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.PARAMETER_SET ) );
        inventory.buildMolecules();

        Assert.assertEquals( "There should be 44 total pairs", 44, inventory.getMoleculeCount() );
    }

    @Test
    public void testInitPairCountTiny() {
        IInventory inventory = new PairwiseInventory();

        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.TWO_X_THREE_SET ) );
        inventory.buildMolecules();
        
        Assert.assertEquals( "There should be 6 pairs", 6, inventory.getMoleculeCount() );
    }
    
    @Test
    public void testInitPairCountOneToMany() {
        IInventory inventory = new PairwiseInventory();
        
        Scenario<String> scenario = new Scenario<String>();
        for ( String line: StringUtils.split( "Param0: x\nParam1: i, j, k, l, m, n, o, p", System.getProperty( "line.separator" ) ) ) {
            scenario.addParameterSet( PairwiseInventoryFactory.processOneLine( line ) );
        }
        
        inventory.setScenario( scenario );
        inventory.buildMolecules();
        
        Assert.assertEquals( "There should be 8 pairs", 8, inventory.getMoleculeCount() );
    }
    
    @Test
    public void testInitPairCountManyToOne() {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory( "Param1: i, j, k, l, m, n, o, p\nParam0: a" );
        inventory.buildMolecules();
        Assert.assertEquals( "There should be 8 pairs", 8, inventory.getMoleculeCount() );
    }
    
    @Test
    public void testGenerateTinySetPairs() {
        IInventory inventory = new PairwiseInventory();

        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.TWO_X_THREE_SET ) );
        inventory.buildMolecules();

        Assert.assertEquals( "The pair count should be 6", 6, inventory.getMoleculeCount() );
    }

    @Test
    public void testOrderNInventory4Atoms() {
        IInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.BIG_PARAMETER_SET ) );
        inventory.setAtomsPerMolecule(4);

        int moleculeSetCount = inventory.initMoleculeCount();
        Assert.assertEquals( "With the Big Parameter set and an atom size of 4, there should be 420 molecule sets", 420, moleculeSetCount );
    }

    @Test
    public void testOrderNInventory3Atoms() {
        IInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.BIG_PARAMETER_SET ) );
        inventory.setAtomsPerMolecule( 3 );

        int moleculeSetCount = inventory.initMoleculeCount();
        Assert.assertEquals( "With the Big Parameter set and an atom size of 3, there should be 105 molecule sets", 105, moleculeSetCount );
    }

    @Test
    public void testAllPossibleMoleculeCalculation() {
        IInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.TWO_X_THREE_SET ) );
        Assert.assertEquals("With the 2 x 3 Parameter set, there should be 6 possible combinations", 6, inventory.getFullCombinationCount());

        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.PARAMETER_SET ) );
        Assert.assertEquals( "With the normal sized Parameter set, there should be 48 possible combinations", 48, inventory.getFullCombinationCount() );

        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.BIG_PARAMETER_SET ) );
        Assert.assertEquals( "With the Big Parameter set, there should be 5040 possible combinations", 5040, inventory.getFullCombinationCount() );
    }

    @Test
    public void testGetParameterIndexList() {
        OrderNInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.BIG_PARAMETER_SET ) );
        Assert.assertEquals( "With the Big Parameter set, the ParameterIndexList should be { 0, 1, 2, 3, 4, 5, 6 }", Arrays.asList( 0, 1, 2, 3, 4, 5, 6 ), inventory.getParameterIndexList() );
    }

    @Test
    public void testPurgeValuesFromList() {
        List<Integer> values = Arrays.asList( 0, 1, 2, 3, 4, 5, 6, 7 );
        OrderNInventory inventory = new OrderNInventory();
        List<Integer> newList = inventory.purgeValuesFromList( values, new int[] { 4, 6, 0 } );
        Assert.assertEquals( "After purging values from the list, the new array should contain only the expected elements", newList, Arrays.asList( 1, 2, 3, 5, 7 ) );
    }

    @Test
    public void testPurgeValuesNoList() {
        List<Integer> values = Arrays.asList( 0, 1, 2, 3, 4, 5 );
        OrderNInventory inventory = new OrderNInventory();
        List<Integer> newList = inventory.purgeValuesFromList( values, new int[] {} );
        Assert.assertEquals( newList, Arrays.asList( 0, 1, 2, 3, 4, 5 ) );
    }

    @Test( expected = IndexOutOfBoundsException.class )
    public void testPurgeValuesOutOfBounds() {
        List<Integer> values = Arrays.asList( 0, 1, 2, 3, 4, 5 );
        OrderNInventory inventory = new OrderNInventory();
        inventory.purgeValuesFromList( values, new int[] { 6 } );
    }

    @Test
    public void testSetAtomsPerMolecule() {
        OrderNInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.BIG_PARAMETER_SET ) );
        inventory.setAtomsPerMolecule( 4 );
        Assert.assertEquals( 4, inventory.getAtomsPerMolecule() );
    }

    @Test
    public void testSetAtomsPerMoleculeNullScenario() {
        OrderNInventory inventory = new OrderNInventory();
        inventory.setAtomsPerMolecule( 3 );
        Assert.assertEquals( 2, inventory.getAtomsPerMolecule() );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testSetAtomsPerMoleculeGreaterThanScenario() {
        OrderNInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.BIG_PARAMETER_SET ) );
        inventory.setAtomsPerMolecule( 8 );
    }

    @Test
    public void testSetAtomsPerMoleculeTwoParameterSets() {
        OrderNInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.TWO_X_THREE_SET ) );
        inventory.setAtomsPerMolecule( 2 );
        Assert.assertEquals( 2, inventory.getAtomsPerMolecule() );
    }

    @Test
    public void testSetAtomsPerMoleculeToOne() {
        OrderNInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.TWO_X_THREE_SET ) );
        inventory.setAtomsPerMolecule( 1 );
        Assert.assertEquals( 1, inventory.getAtomsPerMolecule() );
    }

    @Test
    public void testBuildMoleculesTiny() {
        OrderNInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.TWO_X_THREE_SET ) );
        inventory.setAtomsPerMolecule( 2 );
        inventory.initMoleculeCount();

        inventory.buildMolecules();
    }

    @Test
    public void testBuildMoleculesThreePerSet() {
        OrderNInventory inventory = new OrderNInventory();
        inventory.setScenario( PairwiseInventoryFactory.generateScenario( InventoryFactoryTests.BIG_PARAMETER_SET ) );
        inventory.setAtomsPerMolecule( 3 );
        inventory.initMoleculeCount();

        inventory.buildMolecules();
    }
}
