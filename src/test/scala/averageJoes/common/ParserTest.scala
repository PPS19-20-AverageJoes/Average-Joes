package averageJoes.common

import averageJoes.common.database.{Customer, Workout}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ParserTest extends AnyFlatSpec with Matchers {

  "Storage" should "not be empty" in {
    val storage = Customer.customerStorage
    assert(storage.getCount > 0)
  }

  "Workout storage" should "not be empty" in {
    val storage = Workout.workoutStorage
    assert(storage.getCount > 0)
  }

  "Workout list for user" should "not be empty" in {
    val storage = Workout.workoutStorage
    assert(storage.getWorkoutForCustomer("Wristband1").nonEmpty)
  }
}

