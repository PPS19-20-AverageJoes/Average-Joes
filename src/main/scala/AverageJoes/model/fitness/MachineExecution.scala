package AverageJoes.model.fitness

import AverageJoes.utils.ExerciseUtils.{MACHINE_TYPE, MachineType}
import AverageJoes.utils.SafePropertyValue.NonNegative._

object MachineExecution {
  trait MachineEquipment {
    val machineType: MachineType
    val exEquipment: String = "MACHINE"
  }

  object MACHINE_EQUIPMENT {
    type IntVal = NonNegInt
    type DoubleVal = NonNegDouble

    case class LiftMachine(wight: IntVal, set: IntVal) extends MachineEquipment
    { override val machineType: MachineType = MACHINE_TYPE.LIFTING }

    case class RunningMachine(incline: DoubleVal, speed: DoubleVal, timer: IntVal) extends MachineEquipment
    { override val machineType: MachineType = MACHINE_TYPE.RUNNING }

    case class CyclingMachine(resistance: DoubleVal, timer: IntVal) extends MachineEquipment
    { override val machineType: MachineType = MACHINE_TYPE.CYCLING }
  }
}
