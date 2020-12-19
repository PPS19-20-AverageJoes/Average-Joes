package averageJoes.model.workout

object MachineTypes extends Enumeration {
  type MachineType = Value
  val RUNNING, CYCLING, LEG_PRESS, CHEST_FLY, LIFTING = Value
}


object MachineTypeConverters {
  import MachineTypes._

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

  import averageJoes.model.workout.MachineParameters._
  import averageJoes.model.workout.ExerciseParameters.ExerciseParameter
  import averageJoes.utils.SafePropertyValue.NonNegative.NonNegInt
  def setParametersView(m: MachineType): List[String] = extractParameters[ExerciseParameter,NonNegInt](getEmptyConfiguration(m)).map(t => ExerciseParameters.stringOf(t._1))

}