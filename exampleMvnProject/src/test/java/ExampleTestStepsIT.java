package examples;

import cucumber.api.java8.En;

/**
 * @author Erik
 */
public class ExampleTestStepsIT implements En {

    private int state = 0;

    public ExampleTestStepsIT() {

        Given("^the price of a \"([^\"]*)\" is (\\d+)c$", (String name, int price) -> {
            System.out.println("first step");
            state = 3;
        });

        myMethod();

        When("^I checkout (\\d+) \"([^\"]*)\"$", (int itemCount, String itemName) -> {
            System.out.println("second step");
            System.out.println("State: " + state);
        });

        Then("^the total price should be <total>c$", () -> {
            System.out.println("third step");
            assert state == 3;
        });
    }

    public void myMethod() {
        System.out.println("Hello World");
    }

}
