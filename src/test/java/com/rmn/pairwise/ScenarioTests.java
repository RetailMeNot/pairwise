package com.rmn.pairwise;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ScenarioTests {
    private Scenario getDefaultScenario() {
        ParameterSet<String> set = new ParameterSet<String>(Arrays.asList("a", "b", "c"));
        Scenario scenario = new Scenario();
        scenario.addParameterSet(set);
        return scenario;
    }

    @Test
    public void testParameterPositions() {
        Scenario scenario = getDefaultScenario();
        Assert.assertEquals(3, scenario.getParameterPositions().size());
        Assert.assertEquals(Integer.valueOf(0), scenario.getParameterPositions().get(2));

        ParameterSet<String> set = new ParameterSet<String>(Arrays.asList("d", "e", "f", "g"));
        scenario.addParameterSet( set );

        Assert.assertEquals(7, scenario.getParameterPositions().size());
        Assert.assertEquals(Integer.valueOf(1), scenario.getParameterPositions().get(5));
    }

    @Test
    public void testParameterValues() {
        Scenario scenario = getDefaultScenario();
        Assert.assertEquals("b", scenario.getParameterValues().get(1));

        ParameterSet<String> set = new ParameterSet<String>(Arrays.asList("d", "e", "f", "g"));
        scenario.addParameterSet(set);
        
        Assert.assertEquals("e", scenario.getParameterValues().get(4));
    }

    @Test
    public void testLegalValues() {
        Scenario scenario = getDefaultScenario();
        Assert.assertEquals(1, scenario.getLegalValues().size());
        Assert.assertEquals(3, scenario.getLegalValues().get(0).size());
        Assert.assertEquals(Integer.valueOf(1), scenario.getLegalValues().get(0).get(1));

        ParameterSet<String> set = new ParameterSet<String>(Arrays.asList("d", "e", "f", "g"));
        scenario.addParameterSet(set);
        
        Assert.assertEquals(2, scenario.getLegalValues().size());
        Assert.assertEquals(4, scenario.getLegalValues().get(1).size());
        Assert.assertEquals(Integer.valueOf(5), scenario.getLegalValues().get(1).get(2));
        
        Assert.assertEquals("e", scenario.getParameterValues().get(scenario.getLegalValues().get(1).get(1)));
    }
}
