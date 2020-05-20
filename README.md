# SmallRye version checker

Script for checking SmallRye and MP API dependency versions in the Quarkus repo.

When executed, it prints these pieces of information for each SmallRye project:
1. The SmallRye project version being used by Quarkus at runtime
2. The TCK version that Quarkus test suite uses to verify MP API compatibility of this SmallRye project
3. The MP API version that the SmallRye project itself depends on
 * If this version is different from (2) that is marked as a potential issue

Currently, it obtains data from looking at the `master` branch of the official Quarkus repo - this will be configurable in the future.

## Running the checker
```
mvn clean package && java -jar target/smallrye-version-checker-runner.jar
```    

Or, to run within the IDE, create a run configuration that executes the `CheckerMain.kt` script.

## TODOs
- Check whether the MP API used by Quarkus runtime is the same as the one declared by the SmallRye project
- List potential upgrades to newer versions
- Ability to run against a particular repository/branch/tag (this will be useful for the product side verification)
- Support doing the same for WildFly/EAP or Thorntail apart from just Quarkus