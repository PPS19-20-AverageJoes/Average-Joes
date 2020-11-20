package AverageJoes.common


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

}
