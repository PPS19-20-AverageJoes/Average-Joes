package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseExecutionEquipment.Equipment
import AverageJoes.model.fitness.ExerciseExecutionMetric.ExecutionMetric
import AverageJoes.model.fitness.IllegalEquipment.IllegalEquipmentException


/** Exercise execution metric : sets & timer/repetitions */
object ExerciseExecutionMetric {
  import AverageJoes.utils.SafePropertyValue.NonNegative.NonNegInt

  type PropValue = NonNegInt
  trait ExecutionMetric {def sets: PropValue}
  case class WithRepetitions(sets: PropValue, repetitions: PropValue) extends ExecutionMetric
  case class WithTimer(sets: PropValue, timer: PropValue) extends ExecutionMetric
}

/** Equipment for exercise execution  */
object ExerciseExecutionEquipment {
  /** Exercise execution equipment */
  sealed trait Equipment {def exEquipment: String}
  object EQUIPMENT {
    case object BOSU extends Equipment {val exEquipment = "BOSU"}
    case object BANDS extends Equipment {val exEquipment = "BANDS"}
    case object DUMBBELLS extends Equipment {val exEquipment = "DUMBBELLS"}
    case object KETTLEBELL extends Equipment {val exEquipment = "KETTLEBELL"}
  }

  /** Machine equipment type */
  trait MachineEquipment extends Equipment {val exEquipment = "MACHINE"}
  object MACHINE_EQUIPMENT {
    case class LiftMachine(wight: Int, set: Int) extends MachineEquipment
    case class RunningMachine(incline: Int, speed: Int, time: Int) extends MachineEquipment
    case class CyclingMachine(resistance: Int, time: Int) extends MachineEquipment
  }
}


trait ExerciseExecution {
  /** Firstly no parameters are used because withoutMachineExecution is initialized to true */
  def equipments: List[Equipment]
  def metric: ExecutionMetric
  def withoutMachineExecution: Boolean = true
}

/** Static object to control the correctness of the equipment list passed to parameter constructor
 * If BasicExerciseExecution has a smart machine in equipment list, throw exception, otherwise continue
 * */
object EquipmentChecker {
  val MACHINE_EQUIPMENT = "MACHINE"
  def checkEquipmentCorrectness(withoutSmartMachine: Boolean, equipments: List[Equipment]): Unit =
    if (withoutSmartMachine && equipments.exists(e => e.exEquipment.equals(MACHINE_EQUIPMENT)))
      throw new IllegalEquipmentException;
    else if (!withoutSmartMachine && (!equipments.exists(e => e.exEquipment.equals(MACHINE_EQUIPMENT)) ||
            equipments.count(e => e.exEquipment.equals(MACHINE_EQUIPMENT)) > 1))
      throw new IllegalEquipmentException
}


/** Without smart machine exercise execution */
case class BasicExerciseExecution(metric: ExecutionMetric, equipments: List[Equipment]) extends ExerciseExecution {
  import EquipmentChecker._
  checkEquipmentCorrectness(withoutMachineExecution, equipments)
}

/** Todo Test onlyOneSmartMachine */
/** Decorator adding smart machines as an execution type */
trait MachineExecution extends ExerciseExecution {
  abstract override def withoutMachineExecution = false
  def smartMachine(): Equipment = equipments.find(eq => eq.exEquipment.equals("MACHINE")).get
}
class MachineExerciseExecution(override val metric: ExecutionMetric, override val equipments: List[Equipment])
  extends BasicExerciseExecution(metric, equipments) with MachineExecution


/** Not allowed equipment as argument  */
object IllegalEquipment{
  class IllegalEquipmentException extends IllegalArgumentException
}
