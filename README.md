pairwise
========

```java
private static final String NAV_SCENARIO =
        "Browser: Chrome, Firefox, InternetExplorer"
     + "\nPage: Home, Cart, Profile"
     + "\nEntity: User, Car, Computer"
     + "\nClick: Link, Image, Description, MoreInfo"
        ;

public static void buildTestSets() {
    //First, generate the list of vectors we *want*
    IInventory inventory = PairwiseInventoryFactory.generateParameterInventory(NAV_SCENARIO);
    List<Map<String, String>> rawDataSet = inventory.getTestDataSet().getTestSets();

    //Now, go through the vectors in the database to figure out what we already *have*
    // If we don't have it already, create it
    for (Map<String, String> rawTestCase: rawDataSet) {
        log.debug(String.format("Looking for Vector: [%s] [%s] [%s] [%s]", rawTestCase.get("Browser"), rawTestCase.get("Page"), rawTestCase.get("Entity"), rawTestCase("Click"));
        //Now do something with it...
    }
}
```
