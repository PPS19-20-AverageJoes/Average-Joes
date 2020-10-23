package AverageJoes.utils

object ExerciseUtils {

  /** Exercise type enum */
  sealed trait Type {def exType: String}
  object Type{
    case object CARDIO extends Type {val exType = "CARDIO"}
    case object FULL_BODY extends Type {val exType = "FULL_BODY"}
    case object WEIGHT extends Type {val exType = "WEIGHT"}
  }

  /** Exercise force enum */
  sealed trait Force {def exForce: String}
  object FORCE{
    case object PULL extends Force {val exForce = "PULL"}
    case object STATIC  extends Force {val exForce = "STATIC"}
    case object DEFAULT  extends Force {val exForce = "DEFAULT"}
  }

  /** Exercise level enum */
  sealed trait Level {def exLevel: String}
  object LEVEL{
    case object BEGINNER extends Level {val exLevel = "BEGINNER"} //<20
    case object INTERMEDIATE extends Level {val exLevel = "INTERMEDIATE"} // 20 - 40
    case object ADVANCED extends Level {val exLevel = "ADVANCED"} // >40
    case object DEFAULT extends Level {val exLevel = "DEFAULT"} // >40

  }

  /** Exercise worked muscle enum */
  sealed trait Muscle {def muscleWorked: String}
  object MUSCLE{
    case object ABDOMINAL extends Muscle {val muscleWorked =  "ABDOMINAL"}
    case object BICEPS extends Muscle {val muscleWorked =  "BICEPS"}
    case object CHEST extends Muscle {val muscleWorked =  "CHEST"}
    case object FORE_ARM extends Muscle {val muscleWorked =  "FORE_ARM"}
    case object LOWER_BACK extends Muscle {val muscleWorked =  "LOWER_BACK"}
    case object MIDDLE_BACK extends Muscle {val muscleWorked =  "MIDDLE_BACK"}
    case object QUADRICEPS extends Muscle {val muscleWorked =  "QUADRICEPS"}
    case object SHOULDERS extends Muscle {val muscleWorked =  "SHOULDERS"}
    case object TRICEPS extends Muscle {val muscleWorked =  "TRICEPS"}
  }

  /** Exercise parameters */
  sealed trait ExerciseParameter
  object CONFIGURABLE_PARAMETERS {
    case object SETS extends ExerciseParameter
    case object TIMER extends ExerciseParameter
    case object REPETITIONS extends ExerciseParameter
    case object INCLINE extends ExerciseParameter
    case object SPEED extends ExerciseParameter
    case object WIGHT extends ExerciseParameter

  }

}
