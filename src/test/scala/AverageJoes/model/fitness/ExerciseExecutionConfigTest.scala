package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.utils.ExerciseUtils.CONFIGURABLE_PARAMETERS._
import AverageJoes.utils.SafePropertyValue.NonNegative.{NonNegDouble, NonNegInt}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ExerciseExecutionConfigTest extends AnyFlatSpec with Matchers {
  import AverageJoes.model.fitness.MachineExecution.MACHINE_EQUIPMENT._

  val exerciseRunning: Exercise = Exercise(RunningMachine(speed = 10.0, incline = 20.0, timer = 30))
  val params: Parameters = exerciseRunning.executionParameters

  "Execution parameters of exercise" should "not be empty" in {
    assert(exerciseRunning.executionParameters !== null)
  }

  it should "return an accurate set of parameters" in {
    assert(params.valueOf(SPEED).isDefined)
    assert(params.valueOf(INCLINE).isDefined)
    assert(params.valueOf(TIMER).isDefined)
    assert(params.valueOf(SETS).isEmpty)
    assert(params.valueOf(REPETITIONS).isEmpty)

    assert(params.valueOf(SPEED).get._2.equals(NonNegDouble(10.0)))
    assert(params.valueOf(INCLINE).get._2.equals(NonNegDouble(20.0)))
    assert(params.valueOf(TIMER).get._2.equals(NonNegInt(30)))
    assert(params.valueOf(SETS).isEmpty)
    assert(params.valueOf(REPETITIONS).isEmpty)
  }

  it should "override old parameter values" in {
    assert(params.valueOf(SPEED).get._2.equals(NonNegDouble(10.0)))
    params.addValueOf((SPEED, NonNegDouble(11.0)))
    assert(!params.valueOf(SPEED).get._2.equals(NonNegDouble(10.0)))
    assert(params.valueOf(SPEED).get._2.equals(NonNegDouble(11.0)))

    assert(params.valueOf(TIMER).get._2.equals(NonNegInt(30)))
    params.addValueOf((TIMER, NonNegInt(45)))
    assert(!params.valueOf(TIMER).get._2.equals(NonNegInt(30)))
    assert(params.valueOf(TIMER).get._2.equals(NonNegInt(45)))
  }

}
