package averageJoes.model.fitness

import averageJoes.common.database.table.WorkoutImpl
import averageJoes.model.hardware.PhysicalMachine.RunningMachineParameters
import averageJoes.model.workout.MachineTypes
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ExerciseTest  extends AnyFlatSpec with Matchers {

  val exerciseRunning: Exercise = Exercise(1, RunningMachineParameters(speed = 10, incline = 20, minutes = 30))

  "Exercise" should "not be null" in {
    assert(exerciseRunning !== null)
  }

  it should "have an order" in {
    assert(exerciseRunning.order.equals(1))
  }

  it should "have machine parameters" in {
    assert(exerciseRunning.parameters !== null)
    assert(exerciseRunning.parameters.isInstanceOf[RunningMachineParameters])
  }

  it should "have a RUNNING machine as equipment" in {
    assert(exerciseRunning.parameters.machineType == MachineTypes.RUNNING)
  }

  it should "have an list of parameters configured" in {
    assert(exerciseRunning.parameters !== null)
    assert(exerciseRunning.parameters.isInstanceOf[RunningMachineParameters])
  }

  it can "be instantiated with Workout" in {
    import averageJoes.model.workout.MachineTypes._
    import averageJoes.model.workout.MachineTypeConverters._
    import averageJoes.model.fitness.ImplicitExercise.Converters._

    val workout = WorkoutImpl("123", 10,10, 10, 10, 10, 10, 10, stringOf(RUNNING), 10, "10")

    val exFromWorkout = Exercise(workout)

    assert(exFromWorkout != null)
    assert(exFromWorkout.order.equals(10))
    assert(exFromWorkout.parameters.machineType.equals(RUNNING))
  }

}