package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.utils.ExerciseUtils.ExerciseParameters._
import AverageJoes.utils.SafePropertyValue.NonNegative._
import AverageJoes.utils.SafePropertyValue.SafePropertyVal
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ExerciseExecutionConfigTest extends AnyFlatSpec with Matchers {
  import AverageJoes.model.fitness.MachineExecution.MACHINE_EQUIPMENT._
  import AverageJoes.utils.ExerciseUtils.MachineTypes._

  val exerciseRunning: Exercise = Exercise(RunningMachine(speed = 10.0, incline = 20.0, timer = 30))
  var params: Parameters[SafePropertyVal] = exerciseRunning.executionParameters

  "Execution parameters of exercise" should "not be empty" in {
    assert(exerciseRunning.executionParameters !== null)
  }

  it should "return an accurate list of parameters" in {
    assert(params.valueOf(SPEED).isDefined)
    assert(params.valueOf(INCLINE).isDefined)
    assert(params.valueOf(TIMER).isDefined)
    assert(params.valueOf(SETS).isEmpty)
    assert(params.valueOf(REPETITIONS).isEmpty)

    assert(params.valueOf(SPEED).get.equals(NonNegDouble(10.0)))
    assert(params.valueOf(INCLINE).get.equals(NonNegDouble(20.0)))
    assert(params.valueOf(TIMER).get.equals(NonNegInt(30)))
    assert(params.valueOf(SETS).isEmpty)
    assert(params.valueOf(REPETITIONS).isEmpty)
  }

  it should("add new parameter values") in {
    params = params.addValueOf((REPETITIONS, NonNegInt(10)))
    assert(params.valueOf(REPETITIONS).isDefined)
    assert(params.valueOf(REPETITIONS).get.equals(NonNegInt(10)))
  }

  it should "override old parameter values" in {
    assert(params.valueOf(SPEED).get.equals(NonNegDouble(10.0)))
    params = params.addValueOf((SPEED, NonNegDouble(11.0)))
    assert(!params.valueOf(SPEED).get.equals(NonNegDouble(10.0)))
    assert(params.valueOf(SPEED).get.equals(NonNegDouble(11.0)))

    assert(params.valueOf(TIMER).get.equals(NonNegInt(30)))
    params = params.addValueOf((TIMER, NonNegInt(45)))
    assert(!params.valueOf(TIMER).get.equals(NonNegInt(30)))
    assert(params.valueOf(TIMER).get.equals(NonNegInt(45)))
  }

  it should "return the machine type of the parameters" in {
    assert(params.typeParams.equals(RUNNING))
  }

}
