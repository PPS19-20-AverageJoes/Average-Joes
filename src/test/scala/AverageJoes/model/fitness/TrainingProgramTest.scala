package AverageJoes.model.fitness

import AverageJoes.common.database.table.{Customer, CustomerImpl}
import AverageJoes.model.hardware.PhysicalMachine.{LiftingMachineParameters, RunningMachineParameters}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import AverageJoes.utils.SafePropertyValue.NonNegative._ /** Using implicit NonNegDouble and NonNegInt */
class TrainingProgramTest extends AnyFlatSpec with Matchers {
/*
  val customer1: Customer = new CustomerImpl("GEUS", "sokol", "guri", "27/08/1998", "1234")


  val trainingProgram1: TrainingProgram = TrainingProgram(customer1) (Set.empty[Exercise])

  "Customers" should "have an instantiated training program" in {
    assert(trainingProgram1 != null)
    assert(customer1 === trainingProgram1.customer)
  }

  "Training program" should "have no exercises initially" in {
    assert(trainingProgram1.allExercises == Set())
  }

  it should "add new exercises" in {
    trainingProgram1.addExercise(Exercise(RunningMachineParameters(speed = 10, incline = 20, minutes = 30)))

    assert(trainingProgram1.allExercises.size == 1)
    assert(trainingProgram1.allExercises == Set(Exercise(RunningMachineParameters(speed = 10, incline = 20, minutes = 30))))

    trainingProgram1.addExercise(Exercise((LiftingMachineParameters(10, 11,10,10))))
    assert(trainingProgram1.allExercises.size == 2)
    assert(trainingProgram1.allExercises == Set(Exercise(RunningMachineParameters(speed = 10, incline = 20, minutes = 30)), Exercise(LiftingMachineParameters(10, 11,10,10))))
  }

  it should "remove exercises" in {
    val customer2: Customer = new CustomerImpl("ALBR", "alber", "guri", "11/04/2001", "ALBR")
    val trainingProgram2: TrainingProgram = TrainingProgram(customer2) (Set.empty[Exercise])

    trainingProgram2.addExercise(Exercise(RunningMachineParameters(10, 10, 11)))
    assert(trainingProgram2.allExercises.size == 1)
    assert(trainingProgram2.allExercises == Set(Exercise(RunningMachineParameters(10, 10, 11))))

    trainingProgram2.removeExercise(Exercise(RunningMachineParameters(10, 10, 11)))
    assert(trainingProgram2.allExercises.isEmpty)
    assert(trainingProgram2.allExercises == Set())
  }

  it should "throw exception removing from empty program" in {
    val customer2: Customer = new CustomerImpl("ALBR", "alber", "guri", "11/04/2001", "ALBR")
    val trainingProgram2: TrainingProgram = TrainingProgram(customer2) (Set.empty[Exercise])

    assert(trainingProgram2.allExercises.isEmpty)
    assertThrows[NoExercisesFound](trainingProgram2.removeExercise(Exercise(RunningMachineParameters(10, 10, 11))))
  }
*/

}
