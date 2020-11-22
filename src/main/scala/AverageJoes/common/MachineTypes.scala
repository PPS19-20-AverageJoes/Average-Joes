package AverageJoes.common

import AverageJoes.model.hardware.PhysicalMachine.CyclingMachineParameters
import AverageJoes.model.workout.{LiftMachineParameters, RunningMachineParameters}


object MachineTypes extends Enumeration {
  type MachineType = Value
  val RUNNING, CYCLING, LEG_PRESS, CHEST_FLY, LIFTING = Value
}


object MachineTypeConverters {
  import AverageJoes.common.MachineTypes._

  def stringOf(m: MachineType): String = m match {
    case RUNNING   => "RUNNING"
    case CYCLING   => "CYCLING"
    case LEG_PRESS => "LEG_PRESS"
    case CHEST_FLY => "CHEST_FLY"
    case LIFTING   => "LIFTING"
  }

  def machineTypeOf(s: String): MachineType = s match {
    case "RUNNING"   => RUNNING
    case "CYCLING"   => CYCLING
    case "LEG_PRESS" => LEG_PRESS
    case "CHEST_FLY" => CHEST_FLY
    case "LIFTING"   => LIFTING
  }

  def setParametersView(m: MachineType): List[String] = m match {
    case RUNNING => List("incline","speed", "timer")
    case LIFTING => List("weight", "sets", "repetitions", "setForSec")
    case CYCLING => List("incline", "timer")
    case LEG_PRESS => List("incline", "sets", "repetitions", "setForSec")
  }


}
