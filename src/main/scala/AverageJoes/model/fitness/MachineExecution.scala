package AverageJoes.model.fitness

import AverageJoes.utils.ExerciseUtils.{MACHINE_TYPE, MachineType}
import AverageJoes.utils.SafePropertyValue.NonNegative._

object MachineExecution {
  trait MachineEquipment {
    val machineType: MachineType
    val exEquipment: String = "MACHINE"
  }

  object MACHINE_EQUIPMENT {

    case class LiftMachine(wight: Int, set: Int) extends MachineEquipment
    { override val machineType: MachineType = MACHINE_TYPE.LIFTING }

    case class RunningMachine(incline: Double, speed: Double, timer: Int) extends MachineEquipment
    { override val machineType: MachineType = MACHINE_TYPE.RUNNING }

    case class CyclingMachine(resistance: Double, timer: Int) extends MachineEquipment
    { override val machineType: MachineType = MACHINE_TYPE.CYCLING }
  }
}
