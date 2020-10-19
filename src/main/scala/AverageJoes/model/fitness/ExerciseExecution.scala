package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseExecution.Equipment

object ExerciseExecution {
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


trait ExerciseExecution[E] {
  def withoutMachineExecution: Boolean = true
  def correctExecution(): Boolean
  def equipments: List[E]
}

/** Without smart machine exercise execution */
case class BasicExerciseExecution(equipments: List[Equipment]) extends ExerciseExecution[Equipment] {
  val NOT_ALLOWED_EQUIPMENT = "MACHINE"
  override def correctExecution(): Boolean = withoutMachineExecution && !equipments.exists(e => e.exEquipment.equals(NOT_ALLOWED_EQUIPMENT))
}

trait MachineExecution[E] extends ExerciseExecution[Equipment] {
  abstract override def withoutMachineExecution = false
  abstract override def correctExecution(): Boolean = true
}

/** Decorator adding smart machines as an execution type */
class WithMachineExecution(equipments: List[Equipment]) extends BasicExerciseExecution(equipments) with MachineExecution[Equipment]


