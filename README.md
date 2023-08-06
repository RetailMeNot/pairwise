pairwise
========

## Project
Pairwise is an open-source library for the generation of data sets, most commonly used for
testing. A complete discussion of the ideas and algorithms is available from http://www.pairwise.org/

## Installation

This is a Maven project, so you can either deploy a SNAPSHOT jar to your local Maven repo, or you can just point to the latest version in Maven Central

### Building From Source
Download the source, then from the command-line, run the `mvn install` command. This will deploy a SNAPSHOT jar file to your local Men repo

Once the jar has been deployed, you should be able to include a small snippet in your pom file:

```
    <dependency>
        <groupId>com.retailmenot</groupId>
        <artifactId>pairwise</artifactId>
        <version>0.9-SNAPSHOT</version>
    </dependency>
```

If you're currently using Maven, and your repos, paths, and IDE are all set up correctly, you should be able to address the classes in this project immediately.

If you prefer to build a jar file and include it into your classpath, run `mvn package`, and the jar file should appear in the target folder under the main folder.

### Maven Central
To point to the jar file in Maven Central, include this xml snippet in your pom.xml file:

```
    <dependency>
        <groupId>com.retailmenot</groupId>
        <artifactId>pairwise</artifactId>
        <version>0.9</version>
    </dependency>
```

## Concept
Simply put, this library takes an **input space**, then combines and reduces the data set down
from **all possible permutations** to a set that will guarantee **pairwise permutations**.

Put another way, this algorithm will guarantee not that every possible input was tested with
all possible other inputs, but that **every possible input was tested with all other *single*
inputs**. The initial input space is analyzed and broken down into a series of all possible **pairs** of inputs, and these pairs are then combined to produce the **smallest number of data sets** that can be iterated over in order to guarantee this "pairwise" coverage.

## Links
Please see http://pairwise.org/ for more information


## Example
This snippet takes several variables, and generates a combined set of test cases that will guarantee this "pairwise" coverage.

Imagine a shopping web site, where you want to verify that the various methods of adding an item to a shopping cart will
result in the proper check-out procedure. You want to make sure all supported browsers work, and that you have a representative
set of products

```java
private static final String NAV_SCENARIO =
        "Browser: Chrome, Firefox, InternetExplorer, Safari"
     + "\nPage: Home, Category, Search, New Products"
     + "\nProduct: Phone, Movie, Computer, Blender, Microwave, Book, Sweater"
     + "\nClick: Link, Image, Description"
        ;

public static void buildTestSets() {
    //First, generate the list of vectors we *want*
    IInventory inventory = PairwiseInventoryFactory.generateParameterInventory(NAV_SCENARIO);
    List<Map<String, String>> rawDataSet = inventory.getTestDataSet().getTestSets();

    //Now, go through the vectors in the database to figure out what we already *have*
    // If we don't have it already, create it
    for (Map<String, String> rawTestCase: rawDataSet) {
        log.debug(String.format("Looking for Vector: [%s] [%s] [%s] [%s]", 
            rawTestCase.get("Browser"), rawTestCase.get("Page"), 
            rawTestCase.get("Product"), rawTestCase("Click"));
        //Now do something with it...
    }
}
```

Internally, it first generates the list of all possible pairs:

```
Browser: Chrome, Page: Home
Chrome, Category
Chrome, Search
Chrome, New Products
Browser: Chrome, Product: Phone
Chrome, Movie

<snip>

Page: Home, Click: Link
Home, Image

//etc.
```

Next, it generates a set of test cases that will combine and reduce the pairs in such a way as to examine the highest number of pairs
in the lowest number of iterations:

* Test Case 000: [Chrome] [Home] [Phone] [Link]
* Test Case 001: [Firefox] [Category] [Phone] [Image]
* Test Case 002: [InternetExplorer] [Search] [Phone] [Description]
* Test Case 003: [Safari] [New Products] [Phone] [Link]
* Test Case 004: [Chrome] [Search] [Movie] [Image]

&lt;snip&gt;

* Test Case 025: [Firefox] [Home] [Sweater] [Link]
* Test Case 026: [InternetExplorer] [New Products] [Microwave] [Link]
* Test Case 027: [Safari] [Search] [Blender] [Link]

Test Case 000 covers these pairs:  

* [Chrome, Home]
* [Home, Phone]
* [Phone, Link]
* [Chrome, Phone]
* [Chrome, Link]
* [Home, Link]

Test Case 001 tests 6 new pairs, 002 covers 6 more, etc. There are 116 pairs we want to test, and we're able to guarantee the coverage of all of them in 28 test cases.

You would think that the number of test cases would always be ~19 (116 / 6 (total pairs / #
coverable by one test case)), but it's more complicated than that: when you have a high number
of values in one parameter set (in this case, **Product**), you will end up having to execute more and more iterations in order to guarantee that the products are adequately tested with all other values. Therefore, you will see a lot of repetitions of other pairs as you iterate through the test cases.

### Project Maturity
This project has been in general use within our offices for over a year, having been developed initially to generate simple pairwise data sets for a very small number of scenarios. The algorithm is stable, and does what it purports to do, but that said, there are a lot of features we'd like to add:

* Randomizing test set generation, so that it's not the same set every time
* Along those lines, we would like to provide a "token", which would allow you to generate the same data set over and over, should you have the desire
* Model constraints, which would allow you to make rules such as "these values can never be used together"
* Parameter sub-sets: if there is more than one kind of blender and you need to test with them all, the parameter needs to offer another "dimension" besides just "blender"
* Order-N combinations: currently it only generates pairs of data, but we'd like to allow 3, 4, 5, or N number of combinations
* Non-String data: currently all operations are String-based, but it would be nice to allow any kind of object to be used
* Ability to generate **full** data set: If there are only 300 different test sets possible, and you are ok with testing all 300 of them, you should be able to do that by specifying an Order-1 data set

In conclusion, though this project is stable as-is, it needs a lot of work in order to become truly useful in many scenarios.

Unit testing is roughly 80%, though there is no practical reason it couldn't be 100%.

## Contributing
All pull requests are greatly appreciated! This project is intended for anyone who wants to add pairwise coverage to their projects easily. If you need new features, open an issue on github, or better yet, contribute your own features! We've made every attempt to keep the code simple and clean, so you should be able to follow our examples.

## Versioning
We intend to use Semantic Versioning for this project. As such we are starting with v0.9, and might conceivably not be backward compatible when releasing v1.x in the future. See http://semver.org/ for more details.

## License
This project has been released under the MIT license. Please see the license.txt file for more details.
