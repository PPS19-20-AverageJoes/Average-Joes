package AverageJoes.model.fitness

import AverageJoes.common.MachineTypes
import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.model.hardware.PhysicalMachine.RunningMachineParameters
import AverageJoes.utils.SafePropertyValue.SafePropertyVal
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ExerciseTest  extends AnyFlatSpec with Matchers {

  val exerciseRunning: Exercise = Exercise(RunningMachineParameters(speed = 10, incline = 20, minutes = 30))

  "Exercise" should "not be null" in {
    assert(exerciseRunning !== null)
  }

  it should "have an machine equipment" in {
    assert(exerciseRunning.parameters !== null)
    assert(exerciseRunning.parameters.isInstanceOf[RunningMachineParameters])
  }

  it should "have a RUNNING machine as equipment" in {
    assert(exerciseRunning.parameters.machineType == MachineTypes.RUNNING)
  }

  it should "have an list of parameters configured" in {
    assert(exerciseRunning.executionParameters !== null)
    assert(exerciseRunning.executionParameters.isInstanceOf[Parameters[SafePropertyVal]])
  }

}