package com.rmn.pairwise;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestCaseGeneratorTests {
    Logger log = LoggerFactory.getLogger(TestCaseGeneratorTests.class);

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

    private static final String USER_PARAMETER_SET =
            "Browser: Chrome, Firefox, IE, Safari\n" +
            "Page Type: Home, Store, Landing, Search Results, Community, 404, Category\n" +
            "Login Type: Organic, Direct, EmailGenerated\n" +
            "User Type: Administrator, User, Operations\n";

    @Test
    public void testGetBestPair() {
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory(PARAMETER_SET);
        List<Integer> firstBestPair = inventory.getBestMolecule();
            
        //The first time around, it should be 0,9
        Assert.assertEquals(Integer.valueOf(0), firstBestPair.get(0));
        Assert.assertEquals(Integer.valueOf(9), firstBestPair.get(1));
        Assert.assertEquals("j", inventory.getScenario().getParameterValues().get(firstBestPair.get(1)));
    }

    @Test
    public void testGetParameterOrdering() {
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory(PARAMETER_SET);
        List<Integer> bestPair = inventory.getBestMolecule();
            
        TestDataSet dataSet = new TestDataSet(inventory, inventory.getScenario());
        List<Integer> ordering = dataSet.getParameterOrdering(inventory.getScenario().getParameterPositions().get(bestPair.get(0)), inventory.getScenario().getParameterPositions().get(bestPair.get(1)));
        Assert.assertEquals(Integer.valueOf(0), ordering.get(0));
        Assert.assertEquals(Integer.valueOf(3), ordering.get(1));
    }

    @Test
    public void testBuildTestSets() {
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory(PARAMETER_SET);
        TestDataSet dataSet = inventory.getTestDataSet();
        dataSet.logResults();
        
        List<List<Integer>> testSets = dataSet.getRawTestSets();
        Iterator<List<Integer>> iter = testSets.iterator();
        
        int count = 0;
        while (iter.hasNext()) {
            log.info("Test Set: {}: {}", count++, iter.next().toString());
        }
    }

    @Test
    public void testBuildTinyTestSets() {
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory(TWO_X_THREE_SET);
        TestDataSet dataSet = inventory.getTestDataSet();
        dataSet.logResults();
    }

    @Test
    public void testBuild3x3TestSets() {
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory(THREE_X_THREE_SET);
        TestDataSet dataSet = inventory.getTestDataSet();
        dataSet.logResults();
    }

    @Test
    public void testBuildBigTestSets() {
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory(BIG_PARAMETER_SET);
        TestDataSet dataSet = inventory.getTestDataSet();
        dataSet.logResults();
        
        Assert.assertEquals("a", dataSet.getTestSets().get(0).get("Param0"));
    }

    @Test
    public void testBuildOutclickTestSets() {
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory(USER_PARAMETER_SET);
        TestDataSet dataSet = inventory.getTestDataSet();
        List<List<Integer>> testSets = dataSet.getRawTestSets();

        String parameterHeadings = "";
        for (int parameterIndex = 0; parameterIndex < inventory.getScenario().getParameterSetCount(); parameterIndex++) {
            parameterHeadings += inventory.getScenario().getParameterSet( parameterIndex ).getName() + "\t";
        }
        log.info(parameterHeadings);

        for (List<Integer> testSet : testSets) {
            String outputStr = "";
            for (int j = 0; j < inventory.getScenario().getParameterSetCount(); j++) {
                outputStr += inventory.getScenario().getParameterValues().get(testSet.get(j)) + "\t";
            }
            log.info(outputStr);
        }
    }

    @Test
    public void buildTestSets() {
        String NAV_SCENARIO =
                "Browser: Chrome, Firefox, InternetExplorer, Safari"
                        + "\nPage: Home, Category, Search, New Products"
                        + "\nProduct: Phone, Movie, Computer, Blender, Microwave, Book, Sweater"
                        + "\nClick: Link, Image, Description"
                ;

        //First, generate the list of vectors we *want*
        Inventory inventory = PairwiseInventoryFactory.generateParameterInventory(NAV_SCENARIO);
        List<Map<String, String>> rawDataSet = inventory.getTestDataSet().getTestSets();

        //Now, go through the vectors in the database to figure out what we already *have*
        // If we don't have it already, create it
        int index = 0;
        for (Map<String, String> rawTestCase: rawDataSet) {
            System.out.println(String.format("Test Case %03d: [%s] [%s] [%s] [%s]", index++, rawTestCase.get("Browser"), rawTestCase.get("Page"), rawTestCase.get("Product"), rawTestCase.get("Click")));
        }
    }
}
