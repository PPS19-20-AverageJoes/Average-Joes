package AverageJoes.model.workout

import AverageJoes.common.MachineTypes._
import AverageJoes.utils.SafePropertyValue.NonNegative.{NonNegDuration, NonNegInt, toInt}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/***** Exercise Metrics *****/

sealed trait ExerciseMetrics { def duration: NonNegDuration }

sealed trait ExerciseMetricsBySet extends ExerciseMetrics {
  val sets: NonNegInt
  val rep: NonNegInt
  val secForSet: NonNegInt
  override def duration: NonNegDuration = 20 seconds//(sets * rep * secForSet) seconds //ToDo: ripristinare
}

sealed trait ExerciseMetricsByTime extends ExerciseMetrics {
  val minutes: NonNegInt
  override def duration: NonNegDuration = 20 seconds//(minutes * 60) seconds //20 sec to test //ToDo: ripristinare
}

/***** Machine Parameters *****/

sealed trait MachineParameters extends ExerciseMetrics{ val machineType: MachineType }
trait MachineParametersBySet extends MachineParameters with ExerciseMetricsBySet
trait MachineParametersByTime extends MachineParameters with ExerciseMetricsByTime

object ExerciseParameters extends Enumeration {
  type ExerciseParameter = Value
  val REPETITIONS, INCLINE, SPEED, WEIGHT
  ,SETS, TIMER,  SET_DURATION,
  DURATION = Value
}

object MachineParameters{
  trait Repetitions extends MachineParameters { val repetitions: NonNegInt }
  trait Incline extends MachineParameters { val incline: NonNegInt }
  trait Speed extends MachineParameters { val speed: NonNegInt }
  trait Weight extends MachineParameters { val weight: NonNegInt }


}


