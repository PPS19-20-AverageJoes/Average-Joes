package AverageJoes.model.user

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FeatureSpec, GivenWhenThen}

@RunWith(classOf[JUnitRunner])
class UserTest extends FeatureSpec with GivenWhenThen {

    info("As a authorization mechanism")
    info("I want to be able to allow a user to log to machine")
    info("So he can workout")

    feature("Authentication button"){
      scenario("User presses authentication button") {
        Given("a machine that is available")
        val user = SmartGymUser("sokol", "guri", "GRUSL", "0001")
        assert(!user.isLogged)

        When("authentication button is pressed")
        user.logIn()

        Then("the user should be authorized to log in")
        assert(user.isLogged)
      }
    }

    scenario("user press authentication button with no available machine"){pending}

}
