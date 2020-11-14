package AverageJoes.model.workout

import AverageJoes.common.MachineTypes._
import AverageJoes.utils.SafePropertyValue.NonNegative.{NonNegInt, toInt}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

sealed trait ExerciseMetrics { def duration: FiniteDuration }
sealed trait MachineParameters extends ExerciseMetrics{ val machineType: MachineType }

sealed trait ExerciseMetricsBySet extends ExerciseMetrics {
  val sets: NonNegInt
  val rep: NonNegInt
  val secForSet: NonNegInt
  override def duration: FiniteDuration = (sets * rep * secForSet) seconds
}

sealed trait ExerciseMetricsByTime extends ExerciseMetrics {
  val minutes: NonNegInt
  override def duration: FiniteDuration = (minutes * 60) seconds
}

trait MachineParametersBySet extends MachineParameters with ExerciseMetricsBySet
trait MachineParametersByTime extends MachineParameters with ExerciseMetricsByTime

//ToDo: spostare nella dichiarazione della macchina in Physical Machine
case class LiftMachineParameters(weight: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt, override val secForSet: NonNegInt) extends MachineParametersBySet { override val machineType: MachineType = LIFTING }
case class RunningMachineParameters(incline: NonNegInt, speed: NonNegInt, override val minutes: NonNegInt) extends MachineParametersByTime { override val machineType: MachineType = RUNNING }

/*
metrica esercizi: set, ripetizioni/tempo
configurazione macchine: peso, inclinazione, ...
dati di esecuzione esercizi: battito cardiaco (max, min, avg)
* */