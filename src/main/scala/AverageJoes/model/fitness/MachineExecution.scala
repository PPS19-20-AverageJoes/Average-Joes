package AverageJoes.model.fitness

import AverageJoes.common.MachineTypes._

object MachineExecution {
  trait MachineEquipment {
    val machineType: MachineType
    val exEquipment: String = "MACHINE"
  }

  object MACHINE_EQUIPMENT {
    import AverageJoes.utils.SafePropertyValue.NonNegative._

    case class LiftMachine(wight: NonNegInt, set: NonNegInt) extends MachineEquipment
    { override val machineType: MachineType =  LIFTING }

    case class RunningMachine(incline: NonNegDouble, speed: NonNegDouble, timer: NonNegInt) extends MachineEquipment
    { override val machineType: MachineType = RUNNING }

    case class CyclingMachine(resistance: NonNegDouble, timer: NonNegInt) extends MachineEquipment
    { override val machineType: MachineType = CYCLING }
  }
}
