package AverageJoes.model.workout

import AverageJoes.model.machine.PhysicalMachine
import AverageJoes.model.machine.PhysicalMachine.MachineType._
import AverageJoes.utils.SafePropertyValue.NonNegative.NonNegInt

sealed trait MachineParameters{ val machineType: PhysicalMachine.MachineType.Type}
sealed trait ExerciseMetrics
sealed trait ExerciseMetricsBySet extends ExerciseMetrics {val sets: NonNegInt; val rep: NonNegInt}
sealed trait ExerciseMetricsByTime extends ExerciseMetrics {val minutes: NonNegInt}
trait MachineParametersBySet extends MachineParameters with ExerciseMetricsBySet
trait MachineParametersByTime extends MachineParameters with ExerciseMetricsByTime


case class LiftMachineParameters(weight: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt) extends MachineParametersBySet { override val machineType: Type = liftMachine }
case class RunningMachineParameters(incline: NonNegInt, speed: NonNegInt, override val minutes: NonNegInt) extends MachineParametersByTime { override val machineType: Type = runningMachine }
case class CyclingMachineParameters(resistance: NonNegInt, override val minutes: NonNegInt) extends MachineParametersByTime { override val machineType: Type = cyclingMachine }
