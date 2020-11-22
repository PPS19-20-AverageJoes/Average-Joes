package AverageJoes.model.workout

import MachineTypes._
import AverageJoes.model.hardware.PhysicalMachine.LegPressParameters
import AverageJoes.utils.SafePropertyValue.NonNegative.{NonNegDuration, NonNegInt, toInt}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt

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

  override def duration: NonNegDuration = 20 seconds//(sets * rep * secForSet) seconds //ToDo: ripristinare

  override val hasSets = true
  override val hasRep = true
  override val hasSecForSet = true
  override def getSets: NonNegInt = sets
  override def getRep: NonNegInt = rep
  override def getSecForSet: NonNegInt = secForSet
}

sealed trait ExerciseMetricsByTime extends ExerciseMetrics {
  val minutes: NonNegInt

  override def duration: NonNegDuration = 20 seconds//(minutes * 60) seconds //20 sec to test //ToDo: ripristinare

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
  REPETITIONS, SETS, TIMER, SET_DURATION,
  DURATION = Value

  trait Weight extends ExerciseParameters { val weight: NonNegInt; override val hasWeight: Boolean = true; override def getWeight: NonNegInt = weight }
  trait Incline extends ExerciseParameters { val incline: NonNegInt; override val hasIncline: Boolean = true; override def getIncline: NonNegInt = incline}
  trait Speed extends ExerciseParameters { val speed: NonNegInt; override val hasSpeed: Boolean = true; override def getSpeed: NonNegInt = speed }
}


/***** Machine Parameters *****/
sealed trait MachineParameters extends ExerciseMetrics with ExerciseParameters { val machineType: MachineType }
trait MachineParametersBySet extends MachineParameters with ExerciseMetricsBySet
trait MachineParametersByTime extends MachineParameters with ExerciseMetricsByTime

object MachineParameters{
  import AverageJoes.model.workout.ExerciseParameters._

  def extractParameters[ExType,ValType](mp: MachineParameters)(f: (ExerciseParameter, NonNegInt) => (ExType,ValType)): List[(ExType,ValType)] = {
    var list: ListBuffer[(ExType,ValType)] = new ListBuffer[(ExType,ValType)]()

    if(mp.hasWeight) list += f(WEIGHT, mp.getWeight)
    if(mp.hasIncline) list += f(INCLINE, mp.getIncline)
    if(mp.hasSpeed) list += f(SPEED, mp.getSpeed)
    if(mp.hasMinutes) list += f(TIMER, mp.getMinutes)
    if(mp.hasSets) list += f(SETS, mp.getSets)
    if(mp.hasRep) list += f(REPETITIONS, mp.getRep)
    if(mp.hasSecForSet) list += f(SET_DURATION, mp.getSecForSet)

    list.toList
  }

  def extractParameterStd(mp: MachineParameters): List[(ExerciseParameter,NonNegInt)] = {
    extractParameters[ExerciseParameter,NonNegInt](mp)((ep,v) => {(ep,v)})
  }

  def extractParameterStrInt(mp: MachineParameters): List[(String,Int)] ={
    extractParameters[String,Int](mp)((ep,v) => {(ep.toString,v.toInt)})
  }
  /*
  def inoculateParameters[ExType,ValType](machineType: MachineType, lst: List[(ExType,ValType)])(f: (ExType,ValType) => (ExerciseParameter, NonNegInt)): MachineParameters{

  }*/

}

object Test extends App(){
  val lpp = LegPressParameters(50,1,10,2)
  println(MachineParameters.extractParameterStd(lpp), lpp.duration)
  println(lpp)
  println(MachineTypes.getEmptyConfiguration(lpp.machineType))
  //lpp.machineType

  val test = MachineParameters.extractParameterStd(lpp)
  println(test)
  println(test.map(t => t._1))

}