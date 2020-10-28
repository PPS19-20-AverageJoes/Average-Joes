package AverageJoes.model.fitness

import AverageJoes.model.customer.Customer
import AverageJoes.model.fitness.MachineExecution.MACHINE_EQUIPMENT.{LiftMachine, RunningMachine}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import AverageJoes.utils.DateUtils._
import AverageJoes.utils.SafePropertyValue.NonNegative._ /** Using implicit NonNegDouble and NonNegInt */
class TrainingProgramTest extends AnyFlatSpec with Matchers {

  val customer1: Customer = Customer("GEUS", "sokol", "guri", "27/08/1998")


  val trainingProgram1: TrainingProgram = TrainingProgram(customer1)

  "Customers" should "have an instantiated training program" in {
    assert(trainingProgram1 != null)
    assert(customer1 === trainingProgram1.customer)
  }

  "Training program" should "have no exercises initially" in {
    assert(trainingProgram1.allExercises == Set())
  }

  it should "add new exercises" in {
    trainingProgram1.addExercise(Exercise(RunningMachine(10.0, 10.0, 11)))
    assert(trainingProgram1.allExercises.size == 1)
    assert(trainingProgram1.allExercises == Set(Exercise(RunningMachine(10.0, 10.0, 11))))

    trainingProgram1.addExercise(Exercise(LiftMachine(10, 11)))
    assert(trainingProgram1.allExercises.size == 2)
    assert(trainingProgram1.allExercises == Set(Exercise(RunningMachine(10.0, 10.0, 11)), Exercise(LiftMachine(10, 11))))
  }

  it should "remove exercises" in {
    val customer2: Customer = Customer("ALBR", "alber", "guri", "11/04/2001")
    val trainingProgram2: TrainingProgram = TrainingProgram(customer2)

    trainingProgram2.addExercise(Exercise(RunningMachine(10.0, 10.0, 11)))
    assert(trainingProgram2.allExercises.size == 1)
    assert(trainingProgram2.allExercises == Set(Exercise(RunningMachine(10.0, 10.0, 11))))

    trainingProgram2.removeExercise(Exercise(RunningMachine(10.0, 10.0, 11)))
    assert(trainingProgram2.allExercises.isEmpty)
    assert(trainingProgram2.allExercises == Set())
  }

  it should "throw exception removing from empty program" in {
    val customer2: Customer = Customer("ALBR", "alber", "guri", "11/04/2001")
    val trainingProgram2: TrainingProgram = TrainingProgram(customer2)

    assert(trainingProgram2.allExercises.isEmpty)
    assertThrows[NoExercisesFound](trainingProgram2.removeExercise(Exercise(RunningMachine(10.0, 10.0, 11))))
  }


}
