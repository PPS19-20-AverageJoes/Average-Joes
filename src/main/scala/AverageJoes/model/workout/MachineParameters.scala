package AverageJoes.model.workout

import AverageJoes.model.machine.PhysicalMachine
import AverageJoes.model.machine.PhysicalMachine.MachineType._
import AverageJoes.utils.SafePropertyValue.NonNegative.NonNegInt

sealed trait MachineParameters{ val machineType: PhysicalMachine.MachineType.Type}
sealed trait MachineParametersBySet extends MachineParameters {val sets: NonNegInt}
sealed trait MachineParametersByTime extends MachineParameters {val minutes: NonNegInt}

case class LiftMachineParameters(weight: NonNegInt, override val sets: NonNegInt) extends MachineParametersBySet { override val machineType: Type = liftMachine }
case class RunningMachineParameters(incline: NonNegInt, speed: NonNegInt, override val minutes: NonNegInt) extends MachineParametersByTime { override val machineType: Type = runningMachine }
case class CyclingMachineParameters(resistance: NonNegInt, override val minutes: NonNegInt) extends MachineParametersByTime { override val machineType: Type = cyclingMachine }

/**
 * @param weight: load
 * @param length: adjustable length according to the customer
 */
case class LegPressParameters(weight: NonNegInt, length: NonNegInt, override val sets: NonNegInt) extends MachineParametersBySet { override val machineType: Type = legPress }

/**
 * @param weight: load
 * @param height: adjustable height of the sit according to the customer
 */
case class ChestFlyParameters(weight: NonNegInt, height: NonNegInt, override val sets: NonNegInt) extends  MachineParametersBySet { override val machineType: Type = chestFly }

/*
sealed trait ExerciseMetrics
sealed trait BySet extends ExerciseMetrics {val sets: NonNegInt}
sealed trait ByTime extends ExerciseMetrics {val minutes: NonNegInt}
*/
