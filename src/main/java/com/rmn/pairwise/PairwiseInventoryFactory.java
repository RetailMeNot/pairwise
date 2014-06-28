package com.rmn.pairwise;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class PairwiseInventoryFactory {
    private static Logger log = LoggerFactory.getLogger(PairwiseInventoryFactory.class);

    /**
     * Go through the parameter sets to populate the list of ParameterSets we're going to use. These are the raw materials
     *  from which the test cases will be generated
     * @param contents The contents of the Scenario you're testing
     * @return The Scenario, fully populated
     */
    public static Scenario generateScenario(String contents) {
        Scenario scenario = new Scenario();
        for (String line: StringUtils.split(contents, System.getProperty("line.separator"))) {
            scenario.addParameterSet(processOneLine(line));
        }
        return scenario;
    }

    /**
     * Parses a String representing the contents of the Scenario, and returns the Scenario
     * @param contents The contents of the Scenario you're testing
     * @return the Scenario
     */
    public static Inventory generateParameterInventory(String contents) {
        Inventory inventory = new PairwiseInventory();
        Scenario scenario = generateScenario(contents);
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        return inventory;
    }

    public static Inventory generateParameterInventory(InputStream stream) throws IOException {
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr);
   
        Scenario scenario = new Scenario();
        String line;
        while ((line = br.readLine()) != null) {
            scenario.addParameterSet(processOneLine(line));
        }
        
        Inventory inventory = new PairwiseInventory();
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        return inventory;
    }

    /**
     * Processes a single line of inputs
     * @param line One line, containing one parameter space (e.g. "Title: Value1, Value2, Value3")
     * @return The ParameterSet representing the line
     */
    public static ParameterSet<String> processOneLine(String line) {
        log.debug("Processing line: {}", line);
        String[] lineTokens = line.split(":", 2);
        List<String> strValues = splitAndTrim(",", lineTokens[1]);
        ParameterSet<String> parameterSet = new ParameterSet<String>(strValues);
        parameterSet.setName(lineTokens[0]);
        return parameterSet;
    }

    private static List<String> splitAndTrim(String regex, String lineTokens) {
        String[] rawTokens = lineTokens.split(regex);
        String[] processedTokens = new String[ rawTokens.length ];
        for (int i = 0; i< rawTokens.length; i++) {
            processedTokens[i] = StringUtils.trim(rawTokens[i]);
        }
        return Arrays.asList(processedTokens);
    }
}
