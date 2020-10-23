package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseExecutionEquipment._
import AverageJoes.model.fitness.ExerciseExecutionMetric._
import AverageJoes.model.fitness.IllegalEquipment.IllegalEquipmentException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ExerciseExecutionTest extends AnyFlatSpec with Matchers {

  val equipment: List[Equipment] = List(EQUIPMENT.BANDS, EQUIPMENT.BOSU, EQUIPMENT.DUMBBELLS)
  val metricRepetitions: ExecutionMetric = WithRepetitions(10, 10)
  val metricTimer: ExecutionMetric = WithTimer(3, 100)

  val basicExecution: ExerciseExecution = BasicExerciseExecution(metricRepetitions, equipment)

  "BasicExerciseExecution" should "be correctly instantiated" in {
    assert(basicExecution !== null)
  }

  it should "have no smart machine" in {
    assert(basicExecution.withoutMachineExecution, true)
    assert(Seq(EQUIPMENT.BANDS, EQUIPMENT.BOSU, EQUIPMENT.DUMBBELLS).equals(basicExecution.equipments))
  }

  it should "throw exception if equipment's list contains machine" in {
    val illegalEquipmentList:List[Equipment] = List(EQUIPMENT.BOSU, MACHINE_EQUIPMENT.CyclingMachine(10,10))
    assertThrows[IllegalEquipmentException](BasicExerciseExecution(metricRepetitions, illegalEquipmentList))
  }

  "MachineExerciseExecution" should "throw exception if no machine is on equipments list" in {
    assertThrows[IllegalEquipmentException](new MachineExerciseExecution(metricRepetitions, equipment))
  }

  it must "include smart and non-smart equipments" in {
    val equipmentListIncludingMachines: List[Equipment] = List( EQUIPMENT.BOSU, MACHINE_EQUIPMENT.CyclingMachine(10,10))
    val exExecution: ExerciseExecution = new MachineExerciseExecution(metricRepetitions, equipmentListIncludingMachines)

    assert(exExecution !== null)
  }

  it must "throw exception for than one more smart machines" in {
    val moreThanOneSmartMachine: List[Equipment] = List(EQUIPMENT.BANDS, MACHINE_EQUIPMENT.LiftMachine(10,10),  MACHINE_EQUIPMENT.CyclingMachine(10,10))
    assertThrows[IllegalEquipmentException](new MachineExerciseExecution(metricRepetitions, moreThanOneSmartMachine))
  }

  it must "must return the only smart machine" in {
    val eqList: List[Equipment] = List(EQUIPMENT.BANDS, MACHINE_EQUIPMENT.CyclingMachine(10,10))
    val machineExecution = new MachineExerciseExecution(metricRepetitions, eqList)
    assert(MACHINE_EQUIPMENT.CyclingMachine(10,10).equals(machineExecution.smartMachine()))
    assert(!MACHINE_EQUIPMENT.CyclingMachine(10,11).equals(machineExecution.smartMachine()))

  }

  "ExecutionMetric" should "throw exception for negative parameter values" in {
    assertThrows[IllegalArgumentException](WithTimer(-3, 100))
  }

}
