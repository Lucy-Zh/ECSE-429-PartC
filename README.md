# ECSE 429 Project Part C
## December 3rd, 2024

Note that this code was written and tested on macOS 13.2.1 (M1 chip) through IntelliJ.

## Context
This repo includes performance testing for a "rest api todo manager", which can be found online at: [link](https://github.com/eviltester/thingifier/releases)

## How to run tests

1. Download the "rest api todo manager".
2. Change into directory of the application.
3. Run ````java -jar runTodoManagerRestAPI-1.5.5.jar```` in that directory. The application is now running.
4. Search [link](http://localhost:4567/docs) on your browser, documentation of the application should be there if application is running correctly.
5. Run performance tests in the ide of your choice. 

* If you're using IntelliJ:
1. Go into the `src/test/java/org/ecse429` directory
2. Run `TodoPerformanceTests.java` or `ProjectPerformanceTests.java`
3. To view results, go to `src/test/resources/performance-test-results` directory. Output should be csv files named with the operation.

## Extra Info

Some important directories of this workspace:
- `src/test/java/org/ecse429`: 
  - code for the performance test suite as well as any helper methods to implement the performance tests
- `src/test/resources/performance-test-results`: 
  - all output data from the performance tests are written here
- `src/test/resources/performance-test-results/charts`: 
  - charts we had created with our results from the performance tests
- `pom.xml`: 
  - where are the project dependencies are configured
