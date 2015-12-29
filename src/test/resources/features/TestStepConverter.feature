Feature: Test step exploration

  In order to find all relevant test steps that I want to convert to some other format
  I as a programmer
  want to have a list of all Java classes with certain properties that classify the class
  as a test step class.

  Scenario: List Java 8 test step classes within a Maven project
    Given the base directory of a Maven project
    When a list of Java 8 test step classes is requested
    Then all classes are returned that are located in the test folder
    And implement the interface cucumber.api.java8.En.
