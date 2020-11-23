package averageJoes.model.workout

import MachineTypes._
import averageJoes.utils.SafePropertyValue.NonNegative.{NonNegDuration, NonNegInt, toInt}
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/***** Exercise Metrics *****/

sealed trait ExerciseMetrics {
  def duration: NonNegDuration

  val hasSets: Boolean = false
  val hasRep: Boolean = false
  val hasSecForSet: Boolean = false
  val hasMinutes: Boolean = false
  def getSets: NonNegInt = 0
  def getRep: NonNegInt = 0
  def getSecForSet: NonNegInt = 0
  def getMinutes: NonNegInt = 0
}

sealed trait ExerciseMetricsBySet extends ExerciseMetrics {
  val sets: NonNegInt
  val rep: NonNegInt
  val secForSet: NonNegInt

  override def duration: NonNegDuration = (sets * rep * secForSet) seconds

  override val hasSets = true
  override val hasRep = true
  override val hasSecForSet = true
  override def getSets: NonNegInt = sets
  override def getRep: NonNegInt = rep
  override def getSecForSet: NonNegInt = secForSet
}

sealed trait ExerciseMetricsByTime extends ExerciseMetrics {
  val minutes: NonNegInt

  override def duration: NonNegDuration = (minutes * 60) seconds

  override val hasMinutes = true
  override def getMinutes: NonNegInt = minutes
}

/***** Exercise Parameters *****/
sealed trait ExerciseParameters{
  val hasWeight: Boolean = false
  val hasIncline: Boolean = false
  val hasSpeed: Boolean = false
  def getWeight: NonNegInt = 0
  def getIncline: NonNegInt = 0
  def getSpeed: NonNegInt = 0
}

object ExerciseParameters extends Enumeration {
  type ExerciseParameter = Value
  val WEIGHT, INCLINE, SPEED,
  REPETITIONS, SETS, SET_DURATION, TIMER,
  DURATION = Value

  def stringOf(m: ExerciseParameter): String = m match {
    case WEIGHT   => "WEIGHT"
    case INCLINE   => "INCLINE"
    case SPEED => "SPEED"
    case REPETITIONS => "REPETITIONS"
    case SETS   => "SETS"
    case SET_DURATION   => "SET_DURATION"
    case TIMER   => "TIMER"
    case DURATION   => "DURATION"
  }

  implicit def exerciseParameterOf(s: String): ExerciseParameter = s match {
    case "WEIGHT"   => WEIGHT
    case "INCLINE"   => INCLINE
    case "SPEED" => SPEED
    case "REPETITIONS" => REPETITIONS
    case "SETS"   => SETS
    case "SET_DURATION"   => SET_DURATION
    case "TIMER"   => TIMER
    case "DURATION"   => DURATION
  }

  trait Weight extends ExerciseParameters { val weight: NonNegInt; override val hasWeight: Boolean = true; override def getWeight: NonNegInt = weight }
  trait Incline extends ExerciseParameters { val incline: NonNegInt; override val hasIncline: Boolean = true; override def getIncline: NonNegInt = incline}
  trait Speed extends ExerciseParameters { val speed: NonNegInt; override val hasSpeed: Boolean = true; override def getSpeed: NonNegInt = speed }
}


/***** Machine Parameters *****/
sealed trait MachineParameters extends ExerciseMetrics with ExerciseParameters { val machineType: MachineType }
trait MachineParametersBySet extends MachineParameters with ExerciseMetricsBySet
trait MachineParametersByTime extends MachineParameters with ExerciseMetricsByTime

object MachineParameters{
  import averageJoes.model.workout.ExerciseParameters._

  def extractParameters[ExType,ValType](mp: MachineParameters)(f: (ExerciseParameter, NonNegInt) => (ExType,ValType)): List[(ExType,ValType)] = {
    var list: ListBuffer[(ExType,ValType)] = new ListBuffer[(ExType,ValType)]()

    if(mp.hasSets) list += f(SETS, mp.getSets)
    if(mp.hasRep) list += f(REPETITIONS, mp.getRep)
    if(mp.hasSecForSet) list += f(SET_DURATION, mp.getSecForSet)
    if(mp.hasMinutes) list += f(TIMER, mp.getMinutes)
    if(mp.hasWeight) list += f(WEIGHT, mp.getWeight)
    if(mp.hasIncline) list += f(INCLINE, mp.getIncline)
    if(mp.hasSpeed) list += f(SPEED, mp.getSpeed)

    list.toList
  }

  def extractParameterStd(mp: MachineParameters): List[(ExerciseParameter,NonNegInt)] = {
    extractParameters[ExerciseParameter,NonNegInt](mp)((ep,v) => {(ep,v)})
  }

  def extractParameterStrInt(mp: MachineParameters): List[(String,Int)] ={
    extractParameters[String,Int](mp)((ep,v) => {(ep.toString,v.toInt)})
  }

  import averageJoes.model.hardware.PhysicalMachine._
  def getEmptyConfiguration(t: MachineType): MachineParameters = t match {
    case RUNNING   => RunningMachineParameters(0,0,0)
    case CYCLING   => CyclingMachineParameters(0,0)
    case LEG_PRESS => LegPressParameters(0,0,0,0)
    case CHEST_FLY => ChestFlyParameters(0,0,0,0)
    case LIFTING   => LiftingMachineParameters(0,0,0,0)
  }

  def inoculateParametersFromList[ExType, ValType](machineType: MachineType, list: List[(ExType, ValType)], f: ((ExType, ValType)) => (ExerciseParameter, NonNegInt)): MachineParameters = {
    val lst: List[(ExerciseParameter,NonNegInt)] = list.map(t => f(t))

    val func: ExerciseParameter => NonNegInt = ex=>lst.filter(e => e._1 == ex) match { case Nil => 0 case t => t.head._2 }

    inoculateParameters(
      machineType,
      sets = func(SETS),
      rep = func(REPETITIONS),
      secForSet = func(SET_DURATION),
      minutes = func(TIMER),
      weight = func(WEIGHT),
      incline = func(INCLINE),
      speed = func(SPEED)
    )

  }

  def inoculateParameters(machineType: MachineType, sets: NonNegInt, rep: NonNegInt, secForSet: NonNegInt, minutes: NonNegInt, weight: NonNegInt, incline: NonNegInt, speed: NonNegInt): MachineParameters ={
    machineType match {
      case RUNNING   => RunningMachineParameters(incline,speed,minutes)
      case CYCLING   => CyclingMachineParameters(incline,minutes)
      case LEG_PRESS => LegPressParameters(weight,sets,rep,secForSet)
      case CHEST_FLY => ChestFlyParameters(weight,sets,rep,secForSet)
      case LIFTING   => LiftingMachineParameters(weight,sets,rep,secForSet)
    }

  }

}
