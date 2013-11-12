package com.rmn.pairwise;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class InventoryFactoryTests {
    static final String TWO_X_THREE_SET = "Param0: x, y\nParam1: i, j, k";
    static final String PARAMETER_SET = 
            "Param0: a, b\n" +
            "Param1: c, d, e, f\n" +
            "Param2: g, h, i\n" +
            "Param3: j, k";

    static final String BIG_PARAMETER_SET =
            "Param0: a, b\n" +
            "Param1: c, d, e, f\n" +
            "Param2: g, h, i\n" +
            "Param3: j, k, l, m, n\n" +
            "Param4: o, p\n" +
            "Param5: q, r, s\n" +
            "Param6: t, u, v, w, x, y, z\n";

    @Test
    public void testValidateParameterSet() {
        Scenario<String> scenario = PairwiseInventoryFactory.generateScenario( PARAMETER_SET );
        Assert.assertEquals( "There should be 4 Parameter Sets", 4,  scenario.getParameterSetCount() );
        Assert.assertEquals( "There should be 11 total Parameter Values", 11, scenario.getParameterValuesCount() );
    }
    
    @Test
    public void testProcessParameterValues() {
        String line = "Param1: c, d, e, f";
        
        Scenario<String> scenario = PairwiseInventoryFactory.generateScenario( line );
        scenario.logParameterValues();
        
        Assert.assertEquals( "There should only be one Parameter Set", 1, scenario.getParameterSetCount() );
        Assert.assertEquals( "There should be 4 values in the whole Parameter Set", 4, scenario.getParameterValuesCount() );
        
        Assert.assertEquals( "The first parameter value in the set should be [c]", "c", scenario.getParameterValues().get( 0 ) );
    }
    
    @Test
    public void testLegalValues() {
        Scenario<String> scenario = PairwiseInventoryFactory.generateScenario( TWO_X_THREE_SET );
        scenario.logParameterValues();

        Assert.assertEquals( 2, scenario.getLegalValues().length );
        Assert.assertEquals( 2, scenario.getLegalValues()[0].length );
        Assert.assertEquals( 3, scenario.getLegalValues()[1].length );
        Assert.assertEquals( "j", scenario.getParameterSet( 1 ).getValue( 1 ) );
    }

    @Test
    public void testStreamParsingFactory() throws IOException {
        FileInputStream stream = new FileInputStream( "src/test/resources/Tiny Test Set.txt" );
        IInventory scenario = PairwiseInventoryFactory.generateParameterInventory( stream );
        scenario.getScenario().logParameterValues();

        Assert.assertEquals( 2, scenario.getScenario().getLegalValues().length );
        Assert.assertEquals( 2, scenario.getScenario().getLegalValues()[0].length );
        Assert.assertEquals( 3, scenario.getScenario().getLegalValues()[1].length );
        Assert.assertEquals( "j", scenario.getScenario().getParameterSet( 1 ).getValue( 1 ) );
    }
}
