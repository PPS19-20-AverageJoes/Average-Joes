package AverageJoes.model.machine

import AverageJoes.common.{LogOnMessage, LoggableMsg}
import AverageJoes.model.workout.{MachineParameters, MachineParametersBySet}
import AverageJoes.utils.SafePropertyValue.NonNegative.NonNegInt
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

sealed trait PhysicalMachine extends AbstractBehavior[PhysicalMachine.Msg] with LogOnMessage[PhysicalMachine.Msg]{
  val machineID: String
  val machineLabel: PhysicalMachine.MachineLabel //To show on device
  val machineType: PhysicalMachine.MachineType.Type
  //val ma: ActorRef[MachineActor.Msg]
  override val logName: String = "PM %s: %s".format(machineType, machineID)

  override def onMessageLogged(msg: PhysicalMachine.Msg): Behavior[PhysicalMachine.Msg] = {
    msg match{
      case m: PhysicalMachine.Msg.MachineActorStarted => operative(m.refMA)
    }
  }

  private def operative(ma: ActorRef[MachineActor.Msg]): Behavior[PhysicalMachine.Msg] = {
    Behaviors.receiveMessage {
      case m: PhysicalMachine.Msg.Rfid => ma ! MachineActor.Msg.UserLogIn(m.userID); Behaviors.same
      case m: PhysicalMachine.Msg.Display => display(m.message); Behaviors.same
      case m: PhysicalMachine.Msg.ConfigMachine => configure(m.machineParameters); Behaviors.same
    }
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
    final case class ConfigMachine(machineParameters: MachineParameters) extends Msg
    //From Device
    final case class Rfid(userID: String) extends Msg //Rfid fired
  }
  type MachineLabel = String //ToDo: definire numero massimo caratteri (safe property value)

  object MachineType extends Enumeration {
    type Type = Value
    val legPress
      , chestFly
      , liftMachine
      , runningMachine
      , cyclingMachine
      = Value
  }

  import MachineType._
  def apply(machineID: String, phMachineType: Type, machineLabel: MachineLabel): Behavior[Msg] = {
    phMachineType match{
        case MachineType.legPress => LegPress(machineID, machineLabel)
        case MachineType.chestFly => ChestFly(machineID, machineLabel)

    }
  }

  private trait DefaultSimulatedPhysicalBehavior extends PhysicalMachine {
    override def display(s: String): Unit = {
      val _display: String = machineID + " " + s
    }

    override def configure(machineParameters: MachineParameters): Unit = {
      if(machineParameters.machineType != machineType) throw new IllegalArgumentException
      else formatConfiguration(machineParameters)
    }
  }

  object LegPress{
    def apply(machineID: String, machineLabel: MachineLabel): Behavior[Msg] = Behaviors.setup(context => new LegPress(context, machineID, machineLabel))

    private class LegPress(context: ActorContext[Msg], override val machineID: String, override val machineLabel: String)
      extends AbstractBehavior[Msg](context) with PhysicalMachine with DefaultSimulatedPhysicalBehavior {

      override val machineType: Type = legPress
      override val loggingContext: ActorContext[Msg] = this.context

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
     * @param length: adjustable length according to the customer
     */
    case class LegPressParameters(weight: NonNegInt, length: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt) extends MachineParametersBySet { override val machineType: Type = legPress }

  }

  object ChestFly{
    def apply(machineID: String, machineLabel: MachineLabel): Behavior[Msg] = Behaviors.setup(context => new ChestFly(context, machineID, machineLabel))

    private class ChestFly(context: ActorContext[Msg], override val machineID: String, override val machineLabel: String)
      extends AbstractBehavior[Msg](context) with PhysicalMachine with DefaultSimulatedPhysicalBehavior {

      override val machineType: Type = chestFly
      override val loggingContext: ActorContext[Msg] = this.context

      override def formatConfiguration(machineParameters: MachineParameters): String = {
        machineParameters match {
          case p: ChestFlyParameters => p.weight.toString //ToDo: inviare messaggio a view
          case _ => throw new IllegalArgumentException
        }
      }
    }

    /**
     * @param weight: load
     * @param height: adjustable height of the sit according to the customer
     */
    case class ChestFlyParameters(weight: NonNegInt, height: NonNegInt, override val sets: NonNegInt, override val rep: NonNegInt) extends  MachineParametersBySet { override val machineType: Type = chestFly }
  }

}