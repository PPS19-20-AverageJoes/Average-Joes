package AverageJoes.model.hardware

import AverageJoes.common.{LogOnMessage, LoggableMsg, MachineTypes}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.workout.{MachineParameters, MachineParametersBySet, MachineParametersByTime}
import AverageJoes.utils.SafePropertyValue.NonNegative.NonNegInt
import AverageJoes.view.ViewToolActor
import AverageJoes.view.ViewToolActor.ViewPhysicalMachineActor
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}



sealed trait PhysicalMachine extends AbstractBehavior[PhysicalMachine.Msg] with LogOnMessage[PhysicalMachine.Msg]{
  import PhysicalMachine._
  val machineID: String
  val machineLabel: MachineLabel //To show on device
  val machineType: MachineTypes.MachineType

  //val ma: ActorRef[MachineActor.Msg]
  override val logName: String = "PM %s: %s".format(machineType, machineID)

  override def onMessageLogged(msg: Msg): Behavior[Msg] = {
    msg match{
      case m: Msg.MachineActorStarted => operative(m.refMA)
    }
  }

  private def operative(ma: ActorRef[MachineActor.Msg]): Behavior[Msg] = {
    Behaviors.receiveMessage {
      case m: Msg.Rfid => ma ! MachineActor.Msg.UserLogIn(m.customerID, machineLabel); Behaviors.same
      case m: Msg.Display => display(m.message); Behaviors.same
      case m: Msg.ConfigMachine => configure(m.machineParameters); inExercise(ma, m.customerID, m.machineParameters)
    }
  }

  private case object TimerKey
  private def inExercise(ma: ActorRef[MachineActor.Msg], customerID: String, machineParameters: MachineParameters): Behavior[Msg] = Behaviors.withTimers[Msg]{ timers =>
    timers.startSingleTimer(TimerKey, ExerciseEnds(), machineParameters.duration)
    Behaviors.receiveMessage {
      case ExerciseEnds() => exerciseEnds(ma, customerID, machineParameters)

      case m: Msg.Rfid => m.customerID match {
        case `customerID` => exerciseEnds(ma, customerID, machineParameters)
        case _ => Behaviors.same
      }
    }
  }

  def exerciseEnds(ma: ActorRef[MachineActor.Msg], customerID: String, machineParameters: MachineParameters): Behavior[Msg] = {
    ma ! MachineActor.Msg.UserMachineWorkout(customerID, machineParameters)
    operative(ma)
  }

  def display (s: String)
  def configure (machineParameters: MachineParameters)
  def formatConfiguration(machineParameters: MachineParameters): String
}

object PhysicalMachine {
  sealed trait Msg extends LoggableMsg
  object Msg{
    //From MachineActor
    final case class MachineActorStarted(machineID: String, refMA: ActorRef[MachineActor.Msg]) extends Msg
    final case class Display(message: String) extends Msg
    final case class ConfigMachine(customerID: String, machineParameters: MachineParameters) extends Msg
    //From Device
    final case class Rfid(customerID: String) extends Msg //Rfid fired
    final case class HeartRate(heartRate: Int) extends Msg
  }
  //Self messages
  private final case class ExerciseEnds() extends Msg

  type MachineLabel = String //ToDo: definire numero massimo caratteri (safe property value)

  import AverageJoes.common.MachineTypes._
  def apply(machineID: String, phMachineType: MachineType, machineLabel: MachineLabel): Behavior[Msg] = {
    Behaviors.setup(context =>
      phMachineType match{
        case MachineTypes.LEG_PRESS => new LegPress(context, machineID, machineLabel)
        case MachineTypes.CHEST_FLY => new ChestFly(context, machineID, machineLabel)
        case MachineTypes.CYCLING => new CyclingMachine(context, machineID, machineLabel)
      }
    )
  }

  //ToDO: Obsolete?
  private trait DefaultSimulatedPhysicalBehavior extends PhysicalMachine {
    override def display(s: String): Unit = {
      val _display: String = machineID + " " + s
    }

    override def configure(machineParameters: MachineParameters): Unit = {
      if(machineParameters.machineType != machineType) throw new IllegalArgumentException
      else formatConfiguration(machineParameters)
    }
  }

  object PhysicalMachineImpl{
    abstract class PhysicalMachineImpl(context: ActorContext[Msg], override val machineID: String, override val machineLabel: String)
      extends AbstractBehavior[Msg](context) with PhysicalMachine {

      override val loggingContext: ActorContext[Msg] = this.context

      val machineGui = context.spawn[ViewToolActor.Msg](ViewPhysicalMachineActor(machineID,context.self) , "M_GUI_"+machineID)

      override def display(s: String): Unit = {
        val _display: String = machineID + " " + s
      }

      override def configure(machineParameters: MachineParameters): Unit = {
        if(machineParameters.machineType != machineType) throw new IllegalArgumentException
        else formatConfiguration(machineParameters)
      }

      override def formatConfiguration(machineParameters: MachineParameters): String = {
        machineParameters match {
          case _ => throw new IllegalArgumentException
        }
      }

    }
  }

  /**
   * @param weight: load
   * @param length: adjustable length according to the customer
   */
  case class LegPressParameters(weight: NonNegInt, length: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt, override val secForSet: NonNegInt) extends MachineParametersBySet { override val machineType: MachineType = LEG_PRESS }

  private class LegPress(context: ActorContext[Msg], override val machineID: String, override val machineLabel: String) extends PhysicalMachineImpl.PhysicalMachineImpl(context, machineID, machineLabel){
    override val machineType: MachineType = LEG_PRESS

    override def formatConfiguration(machineParameters: MachineParameters): String = {
      machineParameters match {
        case p: LegPressParameters => p.length.toString //ToDo: inviare messaggio a view
          //p.sets
          //setwith(p.weight)
        case _ => throw new IllegalArgumentException
      }
    }
  }

  /**
   * @param weight: load
   * @param height: adjustable height of the sit according to the customer
   */
  case class ChestFlyParameters(weight: NonNegInt, height: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt, override val secForSet: NonNegInt) extends  MachineParametersBySet { override val machineType: MachineType = CHEST_FLY }

  private class ChestFly(context: ActorContext[Msg], override val machineID: String, override val machineLabel: String) extends PhysicalMachineImpl.PhysicalMachineImpl(context, machineID, machineLabel){
    override val machineType: MachineType = CHEST_FLY

    override def formatConfiguration(machineParameters: MachineParameters): String = {
      machineParameters match {
        case p: ChestFlyParameters => p.weight.toString //ToDo: inviare messaggio a view
        case _ => throw new IllegalArgumentException
      }
    }
  }


  case class CyclingMachineParameters(resistance: NonNegInt, override val minutes: NonNegInt) extends MachineParametersByTime { override val machineType: MachineType = CYCLING }

  private class CyclingMachine(context: ActorContext[Msg], override val machineID: String, override val machineLabel: String) extends PhysicalMachineImpl.PhysicalMachineImpl(context, machineID, machineLabel){
    override val machineType: MachineType = CYCLING

    override def formatConfiguration(machineParameters: MachineParameters): String = {
      machineParameters match {
        case p: CyclingMachineParameters => p.resistance.toString //ToDo: inviare messaggio a view
        //p.sets
        //setwith(p.weight)
        case _ => throw new IllegalArgumentException
      }
    }
  }

}