package com.wsm.pairwise;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.testng.annotations.Test;

public class TestCaseGeneratorTests {
    private static final String TWO_X_THREE_SET = "Param0: a, b\nParam1: i, j, k";
    private static final String THREE_X_THREE_SET = "Param0: a, b, c\nParam1: i, j, k\nParam2: x, y, z";
    private static final String PARAMETER_SET = 
            "Param0: a, b\n" +
            "Param1: c, d, e, f\n" +
            "Param2: g, h, i\n" +
            "Param3: j, k";

    private static final String BIG_PARAMETER_SET = 
            "Param0: a, b\n" +
            "Param1: c, d, e, f\n" +
            "Param2: g, h, i\n" +
            "Param3: j, k, l, m, n, o\n" +
            "Param4: p, q\n" +
            "Param5: r, s, t\n" +
            "Param6: u, v, w\n" +
            "Param7: x, y";

    private static final String OUTCLICK_PARAMETER_SET = 
            "Browser: Chrome, Firefox, IE, Safari\n" +
            "Page Type: Home, Store, Landing, Landing2, Search Results, Printables, Deals, Community, 404, Category, Offer-Level\n" +
            "Coupon Type: Code, Sale, Printable (printableImagePath), Printable (affiliateLink), Printable (printableImagePath & affiliateLink)\n" +
            "Coupon Presentation: Normal, Suggested, Coupon featured on a page with a #cid.<offerid> hash, Modal, Conquested\n" +
            "Click Type: CTR, CTC\n";

    @Test( enabled = true )
    public void testGetBestPair() {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory( PARAMETER_SET );
        int[] firstBestPair = inventory.getBestMolecule();
            
        //The first time around, it should be 0,9
        Assert.assertEquals( 0, firstBestPair[0] );
        Assert.assertEquals( 9, firstBestPair[1] );
        Assert.assertEquals( "j", inventory.getScenario().getParameterValues().get( firstBestPair[1] ) );
    }

    @Test( enabled = true )
    public void testGetParameterOrdering() {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory( PARAMETER_SET );
        int[] bestPair = inventory.getBestMolecule();
            
        TestDataSet dataSet = new TestDataSet( inventory, inventory.getScenario() );
        int[] ordering = dataSet.getParameterOrdering( inventory.getScenario().getParameterPositions()[ bestPair[0] ], inventory.getScenario().getParameterPositions()[ bestPair[1] ] );
        Assert.assertEquals( 0, ordering[0] );
        Assert.assertEquals( 3, ordering[1] );
    }

    @Test( enabled = true )
    public void testBuildTestSets() {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory( PARAMETER_SET );
        TestDataSet dataSet = inventory.getTestDataSet();
        dataSet.logResults();
        
        List<int[]> testSets = dataSet.getRawTestSets();
        Iterator<int[]> iter = testSets.iterator();
        
        int count = 0;
        while ( iter.hasNext() ) {
            System.out.println( "Test Set: " + count++ + ": " + Arrays.toString( iter.next() ) );
        }
    }

    @Test( enabled = true )
    public void testBuildTinyTestSets() {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory( TWO_X_THREE_SET );
        TestDataSet dataSet = inventory.getTestDataSet();
        dataSet.logResults();
    }

    @Test( enabled = true )
    public void testBuild3x3TestSets() {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory( THREE_X_THREE_SET );
        TestDataSet dataSet = inventory.getTestDataSet();
        dataSet.logResults();
    }

    @Test( enabled = true )
    public void testBuildBigTestSets() {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory( BIG_PARAMETER_SET );
        TestDataSet dataSet = inventory.getTestDataSet();
        dataSet.logResults();
        
        Assert.assertEquals( "a", dataSet.getTestSets().get( 0 ).get( "Param0" ) );
    }

    @Test( enabled = true )
    public void testBuildOutclickTestSets() {
        IInventory inventory = PairwiseInventoryFactory.generateParameterInventory( OUTCLICK_PARAMETER_SET );
        TestDataSet dataSet = inventory.getTestDataSet();
        List<int[]> testSets = dataSet.getRawTestSets();

        String parameterHeadings = "";
        for ( int parameterIndex = 0; parameterIndex < inventory.getScenario().getParameterSetCount(); parameterIndex++ ) {
            parameterHeadings += inventory.getScenario().getParameterSet( parameterIndex ).getName() + "\t";
        }
        System.out.println( parameterHeadings );
        
        for ( int i = 0; i < testSets.size(); i++ ) {
            String outputStr = "";
            int[] curr = testSets.get( i );
            for ( int j = 0; j < inventory.getScenario().getParameterSetCount(); j++ ) {
                outputStr += inventory.getScenario().getParameterValues().get( curr[j] ) + "\t";
            }
            System.out.println( outputStr );
        }
    }
}
