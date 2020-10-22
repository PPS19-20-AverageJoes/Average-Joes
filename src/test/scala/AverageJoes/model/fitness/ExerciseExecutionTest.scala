package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseExecutionEquipment._
import AverageJoes.model.fitness.ExerciseExecutionMetric.{ExecutionMetric, WithRepetitions, WithTimer}
import AverageJoes.model.fitness.IllegalEquipment.IllegalEquipmentException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ExerciseExecutionTest extends AnyFlatSpec with Matchers {

  val equipment: List[Equipment] = List(EQUIPMENT.BANDS, EQUIPMENT.BOSU, EQUIPMENT.DUMBBELLS)

  val metricRepetitions: ExecutionMetric = WithRepetitions(10, 10)
  val metricTimer: ExecutionMetric = WithTimer(3, 100)

  val basicExecution: ExerciseExecution =  BasicExerciseExecution(metricRepetitions, equipment)
  val machineExerciseExecution: ExerciseExecution = new MachineExerciseExecution(metricRepetitions, equipment)

  "BasicExerciseExecution" should "be correctly instantiated" in {
    assert(basicExecution !== null)
  }

  it should "have no smart machine" in {
    assert(basicExecution.withoutMachineExecution, true)
    assert(Seq(EQUIPMENT.BANDS, EQUIPMENT.BOSU, EQUIPMENT.DUMBBELLS).equals(basicExecution.equipments))
  }

  it should " have no configuration parameters for smart machines" in {
    assert(None === basicExecution.smartExecutionParam)
  }

  it should "throw exception if equipment's list contains machine" in {
    val illegalEquipmentList:List[Equipment] = List(EQUIPMENT.BOSU, MACHINE_EQUIPMENT.CyclingMachine(10,10))
    assertThrows[IllegalEquipmentException](BasicExerciseExecution(metricRepetitions, illegalEquipmentList))
  }

  "MachineExerciseExecution" should "be instantiated without smart machines in equipments list" in {
    assert(machineExerciseExecution !== null)
  }

  it must "include smart and non-smart equipments" in {
    val equipmentListIncludingMachines: List[Equipment] = List(EQUIPMENT.BANDS, EQUIPMENT.BOSU, MACHINE_EQUIPMENT.CyclingMachine(10,10))
    val exerciseExecution: ExerciseExecution = new MachineExerciseExecution(metricRepetitions, equipmentListIncludingMachines)

    assert(exerciseExecution !== null)
  }



}
