package AverageJoes.model.fitness

import AverageJoes.common.MachineTypes
import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.model.fitness.MachineExecution.MACHINE_EQUIPMENT.RunningMachine
import AverageJoes.utils.SafePropertyValue.SafePropertyVal
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ExerciseTest  extends AnyFlatSpec with Matchers {

  val exerciseRunning: Exercise = Exercise(RunningMachine(speed = 10.0, incline = 20.0, timer = 30))

  "Exercise" should "not be null" in {
    assert(exerciseRunning !== null)
  }

  it should "have an machine equipment" in {
    assert(exerciseRunning.equipment !== null)
    assert(exerciseRunning.equipment.isInstanceOf[RunningMachine])
  }

  it should "have a RUNNING machine as equipment" in {
    assert(exerciseRunning.equipment.machineType == MachineTypes.RUNNING)
  }

  it should "have an list of parameters configured" in {
    assert(exerciseRunning.executionParameters !== null)
    assert(exerciseRunning.executionParameters.isInstanceOf[Parameters[SafePropertyVal]])
  }

}