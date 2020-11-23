package AverageJoes.model.fitness

import AverageJoes.common.database.table.{Customer, CustomerImpl}
import AverageJoes.model.hardware.PhysicalMachine.{LiftingMachineParameters, RunningMachineParameters}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import AverageJoes.utils.SafePropertyValue.NonNegative._

import scala.collection.SortedSet

class TrainingProgramTest extends AnyFlatSpec with Matchers {
  import AverageJoes.model.fitness.ImplicitExercise.Ordering._

  val customer1: Customer = CustomerImpl("GEUS", "sokol", "guri", "27/08/1998", "1234")


  val trainingProgram1: TrainingProgram = TrainingProgram(customer1) (SortedSet.empty[Exercise])

  "Customers" should "have an instantiated training program" in {
    assert(trainingProgram1 != null)
    assert(customer1 === trainingProgram1.customer)
  }

  "Training program" should "have no exercises initially" in {
    assert(trainingProgram1.allExercises == SortedSet())
  }

  it should "add new exercises" in {
    trainingProgram1.addExercise(Exercise(1, RunningMachineParameters(speed = 10, incline = 20, minutes = 30)))

    assert(trainingProgram1.allExercises.size == 1)
    assert(trainingProgram1.allExercises == SortedSet(Exercise(1, RunningMachineParameters(speed = 10, incline = 20, minutes = 30))))

    trainingProgram1.addExercise(Exercise(2, LiftingMachineParameters(10, 11,10,10)))
    assert(trainingProgram1.allExercises.size == 2)
    assert(trainingProgram1.allExercises == SortedSet(Exercise(1, RunningMachineParameters(speed = 10, incline = 20, minutes = 30)), Exercise(2, LiftingMachineParameters(10, 11,10,10))))
  }

  it should "remove exercises" in {
    val customer2: Customer = CustomerImpl("ALBR", "alber", "guri", "11/04/2001", "ALBR")
    val trainingProgram2: TrainingProgram = TrainingProgram(customer2) (SortedSet.empty[Exercise])

    trainingProgram2.addExercise(Exercise(1, RunningMachineParameters(10, 10, 11)))
    assert(trainingProgram2.allExercises.size == 1)
    assert(trainingProgram2.allExercises == SortedSet(Exercise(1, RunningMachineParameters(10, 10, 11))))

    trainingProgram2.removeExercise(Exercise(1, RunningMachineParameters(10, 10, 11)))
    assert(trainingProgram2.allExercises.isEmpty)
    assert(trainingProgram2.allExercises == SortedSet())
  }

  it should "throw exception removing from empty program" in {
    val customer2: Customer = CustomerImpl("ALBR", "alber", "guri", "11/04/2001", "ALBR")
    val trainingProgram2: TrainingProgram = TrainingProgram(customer2) (SortedSet.empty[Exercise])

    assert(trainingProgram2.allExercises.isEmpty)
    assertThrows[NoExercisesFound](trainingProgram2.removeExercise(Exercise(1, RunningMachineParameters(10, 10, 11))))
  }

  "exercises" should "be ordered in base on priority" in {
    val c: Customer = CustomerImpl("ALBR", "alber", "guri", "11/04/2001", "ALBR")

    val ex1 = Exercise(order = 1, RunningMachineParameters(10, 10, 10))
    val ex2 = Exercise(order = 6, RunningMachineParameters(11, 11, 11))
    val ex3 = Exercise(order = 11, RunningMachineParameters(12, 12, 12))

    val tp = TrainingProgram(c)(SortedSet(ex1,ex3,ex2))

    assert(tp.allExercises.equals(SortedSet(ex1,ex2,ex3)))

    tp.removeExercise(tp.allExercises.head)
    assert(tp.allExercises.equals(SortedSet(ex2,ex3)))

    tp.removeExercise(tp.allExercises.head)
    assert(tp.allExercises.equals(SortedSet(ex3)))

    tp.removeExercise(tp.allExercises.head)
    assert(tp.allExercises.equals(SortedSet()))
  }

}
