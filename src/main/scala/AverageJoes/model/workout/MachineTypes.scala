package AverageJoes.model.workout

import AverageJoes.model.workout.MachineParameters.extractParameterStd

object MachineTypes extends Enumeration {
  type MachineType = Value
  val RUNNING, CYCLING, LEG_PRESS, CHEST_FLY, LIFTING = Value

  import AverageJoes.model.hardware.PhysicalMachine._
  def getEmptyConfiguration(t: MachineType): MachineParameters = t match {
    case RUNNING   => RunningMachineParameters(0,0,0)
    case CYCLING   => CyclingMachineParameters(0,0)
    case LEG_PRESS => LegPressParameters(0,0,0,0)
    case CHEST_FLY => ChestFlyParameters(0,0,0,0)
    case LIFTING   => LiftingMachineParameters(0,0,0,0)
  }

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

  def setParametersView(m: MachineType): List[String] = extractParameterStd(MachineTypes.getEmptyConfiguration(m)).map(t => t._1.toString)

  /*m match {

    case RUNNING => List(INCLINE.toString,SPEED.toString,TIMER.toString)
    case LIFTING => List(WEIGHT.toString, SETS.toString, REPETITIONS.toString, SET_DURATION.toString)
    case CYCLING => List(INCLINE.toString, TIMER.toString)
    case LEG_PRESS => List(WEIGHT.toString, SETS.toString, REPETITIONS.toString, SET_DURATION.toString)
    case CHEST_FLY => List(WEIGHT.toString, SETS.toString, REPETITIONS.toString, SET_DURATION.toString)
  }*/

}