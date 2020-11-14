package AverageJoes.model.workout

import AverageJoes.model.machine.PhysicalMachine
import AverageJoes.model.machine.PhysicalMachine.MachineType._
import AverageJoes.utils.SafePropertyValue.NonNegative.{NonNegInt, toInt}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

sealed trait ExerciseMetrics { def duration: FiniteDuration }
sealed trait MachineParameters extends ExerciseMetrics{ val machineType: PhysicalMachine.MachineType.Type }

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
case class LiftMachineParameters(weight: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt, override val secForSet: NonNegInt) extends MachineParametersBySet { override val machineType: Type = liftMachine }
case class RunningMachineParameters(incline: NonNegInt, speed: NonNegInt, override val minutes: NonNegInt) extends MachineParametersByTime { override val machineType: Type = runningMachine }
case class CyclingMachineParameters(resistance: NonNegInt, override val minutes: NonNegInt) extends MachineParametersByTime { override val machineType: Type = cyclingMachine }
/*
metrica esercizi: set, ripetizioni/tempo
configurazione macchine: peso, inclinazione, ...
dati di esecuzione esercizi: battito cardiaco (max, min, avg)
* */