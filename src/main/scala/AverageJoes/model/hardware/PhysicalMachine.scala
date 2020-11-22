package AverageJoes.model.hardware

import AverageJoes.common.{LogManager, LoggableMsgFromTo, NonLoggableMsg}
import AverageJoes.model.fitness.{ExecutionValues, Exercise}
import AverageJoes.model.hardware.PhysicalMachine.Msg.HeartRate
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.workout.{ExerciseMetricsByTime, ExerciseParameters, MachineParameters, MachineParametersBySet, MachineParametersByTime, MachineTypes}
import AverageJoes.utils.SafePropertyValue.NonNegative.NonNegInt
import AverageJoes.view.ViewToolActor
import AverageJoes.view.ViewToolActor.ViewPhysicalMachineActor
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable.ListBuffer
import scala.util.Random
import java.util.Date

import AverageJoes.view.ViewToolActor.Msg.ClearViewConfiguration

sealed trait PhysicalMachine extends AbstractBehavior[PhysicalMachine.Msg]{
  import PhysicalMachine._
  def machineID: String
  def machineLabel: MachineLabel //To show on device
  def machineType: MachineTypes.MachineType

  private val logName: String = PhysicalMachine.logName+"_"+machineID //ToDo: mettere private anche nelle altre classi

  val machineGui: ActorRef[ViewToolActor.Msg]

  override def onMessage(msg: Msg): Behavior[Msg] = {
    LogManager.logBehaviourChange(logName,"onMessage")
    Behaviors.receiveMessagePartial {
      case m: Msg.MachineActorStarted =>
        m.refMA ! MachineActor.Msg.GoIdle(machineID)
        display(machineLabel+" OnLine")
        operative(m.refMA)
      case Msg.StartExercise(_) => Behaviors.same //Ignore in this behaviour
    }
  }

  private def operative(ma: ActorRef[MachineActor.Msg]): Behavior[Msg] = {
    LogManager.logBehaviourChange(logName,"operative")
    Behaviors.receiveMessagePartial {
      case m: Msg.Rfid => ma ! MachineActor.Msg.UserLogIn(m.customerID, machineLabel, machineType); Behaviors.same

      case m: Msg.Display => display(machineLabel + " " + m.message); Behaviors.same

      case m: Msg.ConfigMachine =>
        val machineParameters = m.exercise match{
          case Some(t) => t.parameters
          case _ => MachineParameters.getEmptyConfiguration(machineType)
        }
        configure(m.customerID, machineParameters);
        waitingForStart(ma, m.customerID)

      case Msg.StartExercise(_) => Behaviors.same //Ignore in this behaviour
    }
  }

  private def waitingForStart(ma: ActorRef[MachineActor.Msg], customerID: String): Behavior[Msg] = {
    Behaviors.receiveMessagePartial {
      case m: Msg.StartExercise => {
        val newMachineParameters = m.list match {
          case List.empty => MachineParameters.getEmptyConfiguration(machineType)
          case _ => MachineParameters.inoculateParametersFromList[String,Int](machineType, m.list, t => (t._1,t._2))
        }
        ma ! MachineActor.Msg.StartExercise(newMachineParameters.duration)
        inExercise(ma, customerID, newMachineParameters)
      }
    }
  }

  private case object TimerKey
  private def inExercise(ma: ActorRef[MachineActor.Msg], customerID: String, machineParameters: MachineParameters): Behavior[Msg] = Behaviors.withTimers[Msg]{ timers =>
    LogManager.logBehaviourChange(logName,"inExercise")
    timers.startSingleTimer(TimerKey, ExerciseEnds(), machineParameters.duration)

    if(checkDeterioration()){
      val detMessage = logName + " deterioration, please change consumable"
      LogManager.log(detMessage)
      display(detMessage)
    }

    var heartBeats = new ListBuffer[Int]()

    Behaviors.receiveMessagePartial {
      case m: HeartRate =>
        heartBeats += m.heartRate
        display(customerID+" HR: " + m.heartRate)
        Behaviors.same

      case ExerciseEnds() => exerciseEnds(ma, customerID, machineParameters, heartBeats)

      case m: Msg.Rfid =>
        m.customerID match {
          case `customerID` => exerciseEnds(ma, customerID, machineParameters, heartBeats)
          case _ => Behaviors.same
        }

      case Msg.StartExercise(_) => Behaviors.same //Ignore in this behaviour
    }
  }

  def exerciseEnds(ma: ActorRef[MachineActor.Msg], customerID: String, machineParameters: MachineParameters, heartBeats: ListBuffer[Int]): Behavior[Msg] = {
    LogManager.logBehaviourChange(logName,"exerciseEnds")
    val avg: Int = heartBeats.sum[Int] / (heartBeats.count(_ => true) match { case c: Int if c > 0 => c; case _ => 1})

    ma ! MachineActor.Msg.UserMachineWorkout(customerID, machineParameters, ExecutionValues(heartBeats.max, heartBeats.min, avg))

    machineGui ! ViewToolActor.Msg.ExerciseCompleted()
    display(machineLabel+" Free")

    operative(ma)
  }

  def display (s: String)
  def configure (customerID: String, machineParameters: MachineParameters)
  //def formatConfiguration(machineParameters: MachineParameters): String
  def checkDeterioration(): Boolean
}

object PhysicalMachine {
  val logName: String = "PM"
  sealed trait Msg extends LoggableMsgFromTo
  object Msg{
    //From MachineActor
    final case class MachineActorStarted(machineID: String, refMA: ActorRef[MachineActor.Msg]) extends Msg { override def From: String = "MA"; override def To: String = logName }
    final case class Display(message: String) extends Msg { override def From: String = "MA"; override def To: String = logName }
    final case class ConfigMachine(customerID: String, exercise: Option[Exercise]) extends Msg { override def From: String = "MA"; override def To: String = logName }
    //From Device
    final case class Rfid(customerID: String) extends Msg { override def From: String = "Device"; override def To: String = logName }
    final case class HeartRate(heartRate: Int) extends Msg with NonLoggableMsg { override def From: String = "Device"; override def To: String = logName }
    //From View
    final case class StartExercise(list: List[(String,Int)]) extends Msg { override def From: String = "View"; override def To: String = logName }
  }
  //Self messages
  private final case class ExerciseEnds() extends Msg with NonLoggableMsg { override def From: String = "PM"; override def To: String = "PM" }

  type MachineLabel = String //ToDo: definire numero massimo caratteri (safe property value)

  import AverageJoes.model.workout.MachineTypes._
  def apply(machineID: String, phMachineType: MachineType, machineLabel: MachineLabel): Behavior[Msg] = {
    Behaviors.setup(context =>{
        val machineGui = context.spawn[ViewToolActor.Msg](ViewPhysicalMachineActor(machineID, machineLabel, phMachineType, context.self) , "M_GUI_"+machineID)
        phMachineType match {
          case MachineTypes.LEG_PRESS => new LegPress(context, machineGui, machineID, machineLabel)
          case MachineTypes.CHEST_FLY => new ChestFly(context, machineGui, machineID, machineLabel)
          case MachineTypes.CYCLING => new CyclingMachine(context, machineGui, machineID, machineLabel)
          case MachineTypes.RUNNING => new RunningMachine(context, machineGui, machineID, machineLabel)
          case MachineTypes.LIFTING => new LiftingMachine(context, machineGui, machineID, machineLabel)
        }
      }
    )
  }

  object PhysicalMachineImpl{
    abstract class PhysicalMachineImpl(context: ActorContext[Msg], override val machineID: String, override val machineLabel: String)
      extends AbstractBehavior[Msg](context) with PhysicalMachine {

      override def display(s: String): Unit = {
        machineGui ! ViewToolActor.Msg.UpdateViewObject(s)
      }

      //Commented for test
      override def configure(customerID: String, machineParameters: MachineParameters): Unit = {
        if(machineParameters.machineType != machineType) throw new IllegalArgumentException
        else {
          display(customerID)
          machineGui ! ViewToolActor.Msg.SetMachineParameters(formatConfiguration(machineParameters))
        }
      }

      override def checkDeterioration(): Boolean = {
        val rnd : Int = new Random(new Date().getTime()).nextInt(100)
        println("*** Deterioration: " + rnd) //Todo: test
        rnd > 90
      }

      import AverageJoes.model.workout.MachineParameters._
      import AverageJoes.model.workout.ExerciseMetricsBySet
      def formatConfiguration(machineParameters: MachineParameters): List[(String,Int)] = {
        MachineParameters.extractParameters[String,Int](machineParameters)((ep,v) => {(ep.toString,v.toInt)})
        /*
        var list: ListBuffer[(String,Int)] = new ListBuffer[(String,Int)]()
        machineParameters match{
          case p: LegPressParameters =>
            list += ((ExerciseParameters.REPETITIONS.toString, p.rep))
            list += ((ExerciseParameters.SETS.toString, p.sets))
            list += ((ExerciseParameters.SET_DURATION.toString, p.secForSet))
            list += ((ExerciseParameters.WEIGHT.toString, p.weight))
          case p: CyclingMachineParameters =>
            list += ((ExerciseParameters.INCLINE.toString, p.incline))
            list += ((ExerciseParameters.TIMER.toString, p.minutes))
          case p: ChestFlyParameters =>
            list += ((ExerciseParameters.REPETITIONS.toString, p.rep))
            list += ((ExerciseParameters.SETS.toString, p.sets))
            list += ((ExerciseParameters.SET_DURATION.toString, p.secForSet))
            list += ((ExerciseParameters.WEIGHT.toString, p.weight))
        }

        list.toList
        */
      }


    }
  }

  import AverageJoes.model.workout.ExerciseParameters._
  /***** LEG PRESS *****/
  case class LegPressParameters(override val weight: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt, override val secForSet: NonNegInt)
    extends MachineParametersBySet with Weight
  { override val machineType: MachineType = LEG_PRESS }

  private class LegPress(context: ActorContext[Msg], override val machineGui: ActorRef[ViewToolActor.Msg], override val machineID: String, override val machineLabel: String)
    extends PhysicalMachineImpl.PhysicalMachineImpl(context, machineID, machineLabel){ override val machineType: MachineType = LEG_PRESS }

  /***** CHEST FLY *****/
  case class ChestFlyParameters(override val weight: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt, override val secForSet: NonNegInt)
    extends  MachineParametersBySet with Weight
  { override val machineType: MachineType = CHEST_FLY }

  private class ChestFly(context: ActorContext[Msg], override val machineGui: ActorRef[ViewToolActor.Msg], override val machineID: String, override val machineLabel: String)
    extends PhysicalMachineImpl.PhysicalMachineImpl(context, machineID, machineLabel){ override val machineType: MachineType = CHEST_FLY }

  /***** CYCLING MACHINE *****/
  case class CyclingMachineParameters(override val incline: NonNegInt, override val minutes: NonNegInt)
    extends MachineParametersByTime with Incline
  { override val machineType: MachineType = CYCLING }

  private class CyclingMachine(context: ActorContext[Msg], override val machineGui: ActorRef[ViewToolActor.Msg], override val machineID: String, override val machineLabel: String)
    extends PhysicalMachineImpl.PhysicalMachineImpl(context, machineID, machineLabel){ override val machineType: MachineType = CYCLING }

  /***** RUNNING MACHINE *****/
  case class RunningMachineParameters(override val incline: NonNegInt, speed: NonNegInt, override val minutes: NonNegInt)
    extends MachineParametersByTime with Incline with Speed
  { override val machineType: MachineType = RUNNING }

  private class RunningMachine(context: ActorContext[Msg], override val machineGui: ActorRef[ViewToolActor.Msg], override val machineID: String, override val machineLabel: String)
    extends PhysicalMachineImpl.PhysicalMachineImpl(context, machineID, machineLabel){ override val machineType: MachineType = RUNNING }

  /***** LIFTING MACHINE *****/
  case class LiftingMachineParameters(override val weight: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt, override val secForSet: NonNegInt)
    extends MachineParametersBySet with Weight
  { override val machineType: MachineType = LIFTING }

  private class LiftingMachine(context: ActorContext[Msg], override val machineGui: ActorRef[ViewToolActor.Msg], override val machineID: String, override val machineLabel: String)
    extends PhysicalMachineImpl.PhysicalMachineImpl(context, machineID, machineLabel){ override val machineType: MachineType = LIFTING }

}