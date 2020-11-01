package AverageJoes.model.machine

import AverageJoes.common.{LogOnMessage, LoggableMsg}
import AverageJoes.model.workout.{ChestFlyParameters, LegPressParameters, MachineParameters}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

sealed trait PhysicalMachine extends AbstractBehavior[PhysicalMachine.Msg] with LogOnMessage[PhysicalMachine.Msg]{
  val machineID: String
  val machineLabel: String //To show on device
  val machineType: PhysicalMachine.MachineType.Type
  val ma: ActorRef[MachineActor.Msg]
  override val logName: String = "PM %s: %s".format(machineType, machineID)

  override def onMessageLogged(msg: PhysicalMachine.Msg): Behavior[PhysicalMachine.Msg] = {
    msg match{
      case m: PhysicalMachine.Msg.Rfid => ma ! MachineActor.Msg.UserLogIn(m.userID); Behaviors.same
      case m: PhysicalMachine.Msg.Display => display(m.message); Behaviors.same
      case m: PhysicalMachine.Msg.ConfigMachine => configure(m.machineParameters); Behaviors.same
    }
  }

  def display (s: String)
  def configure (machineParameters: MachineParameters)
  def formatConfiguration (machineParameters: MachineParameters): String
}

object PhysicalMachine {
  sealed trait Msg extends LoggableMsg
  object Msg{
    final case class Rfid(userID: String) extends Msg //Rfid fired
    final case class Display(message: String) extends Msg
    final case class ConfigMachine(machineParameters: MachineParameters) extends Msg
  }

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
  def apply(phMachineType: Type, ma: ActorRef[MachineActor.Msg], machineID: String, machineLabel: String): Behavior[Msg] = {
    phMachineType match{
        case MachineType.legPress => LegPress(ma, machineID, machineLabel)
        case MachineType.chestFly => ChestFly(ma, machineID, machineLabel)

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
    def apply(ma: ActorRef[MachineActor.Msg], machineID: String, machineLabel: String): Behavior[Msg] = Behaviors.setup(context => new LegPress(context, ma, machineID, machineLabel))

    private class LegPress(context: ActorContext[Msg], override val ma: ActorRef[MachineActor.Msg], override val machineID: String, override val machineLabel: String)
      extends AbstractBehavior[Msg](context) with PhysicalMachine with DefaultSimulatedPhysicalBehavior {

      override val machineType: Type = legPress
      override val loggingContext: ActorContext[Msg] = this.context

      override def formatConfiguration(machineParameters: MachineParameters): String = {
        machineParameters match {
          case p: LegPressParameters => p.length.toString //ToDo: inviare messaggio a view
          case _ => throw new IllegalArgumentException
        }
      }

    }
  }

  object ChestFly{
    def apply(ma: ActorRef[MachineActor.Msg], machineID: String, machineLabel: String): Behavior[Msg] = Behaviors.setup(context => new ChestFly(context, ma, machineID, machineLabel))

    private class ChestFly(context: ActorContext[Msg], override val ma: ActorRef[MachineActor.Msg], override val machineID: String, override val machineLabel: String)
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
  }

}