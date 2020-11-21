package AverageJoes.model.workout

import AverageJoes.common.MachineTypes._
import AverageJoes.utils.SafePropertyValue.NonNegative.{NonNegDuration, NonNegInt, toInt}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

sealed trait ExerciseMetrics { def duration: NonNegDuration }
sealed trait MachineParameters extends ExerciseMetrics{ val machineType: MachineType }

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

trait MachineParametersBySet extends MachineParameters with ExerciseMetricsBySet
trait MachineParametersByTime extends MachineParameters with ExerciseMetricsByTime
