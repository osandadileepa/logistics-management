# karate-automation

---

Before running any of these commands, make sure you are in the correct directory

`cd .\karate-automation\ `

Alternatively, you can run tests in IDEA by installing *Gherkin* and *Cucumber for Java* plugin

## Run all tests:

> mvn test "-Dkarate.env=local"

## Run individual tests:

### Weight Calculation

> mvn test "-Dkarate.options=--tags @WeightCalculation"

### Weight Calculation Rule

> mvn test "-Dkarate.options=--tags @WeightCalculationRuleCreate"

> mvn test "-Dkarate.options=--tags @WeightCalculationRuleDelete"

> mvn test "-Dkarate.options=--tags @WeightCalculationRuleGet"

> mvn test "-Dkarate.options=--tags @WeightCalculationRuleSearch"

> mvn test "-Dkarate.options=--tags @WeightCalculationRuleUpdate"

### Rate Calculation

> mvn test "-Dkarate.options=--tags @RateCalculation"

### Rate Card

> mvn test "-Dkarate.options=--tags @RateCardCreate"

> mvn test "-Dkarate.options=--tags @RateCardDelete"

> mvn test "-Dkarate.options=--tags @RateCardGet"

> mvn test "-Dkarate.options=--tags @RateCardUpdate"

### RoundingLogic

> mvn test "-Dkarate.options=--tags @RoundingLogic"