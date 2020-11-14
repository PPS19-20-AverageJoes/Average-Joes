package AverageJoes.utils

object ExerciseUtils {

  object ExerciseParameters extends Enumeration {
    type ExerciseParameter = Value
    val SETS, TIMER, REPETITIONS, INCLINE, SPEED, WIGHT = Value
  }

  object MachineTypes extends Enumeration {
    type MachineType = Value
    val RUNNING, CYCLING, LEG_PRESS, CHEST_FLY, LIFTING = Value
  }
}

