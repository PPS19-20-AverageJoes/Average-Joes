package AverageJoes.model.machine

import AverageJoes.common.{LogOnMessage, LoggableMsg}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

sealed trait PhysicalMachine extends AbstractBehavior[PhysicalMachine.Msg] with LogOnMessage[PhysicalMachine.Msg]{
  val machineID: String
  val ma: ActorRef[MachineActor.Msg] //MachineActor

  override def onMessageLogged(msg: PhysicalMachine.Msg): Behavior[PhysicalMachine.Msg] = {
    msg match{
      case m: PhysicalMachine.Msg.Rfid => ma ! MachineActor.Msg.UserLogIn(m.userID); Behaviors.same
      case m: PhysicalMachine.Msg.Display => display(m.message); Behaviors.same
    }
  }

  def display (s: String): Unit
}

object PhysicalMachine {
  sealed trait Msg extends LoggableMsg
  object Msg{
    final case class Rfid(userID: String) extends Msg //Rfid fired
    final case class Display(message: String) extends Msg
  }

  object MachineType extends Enumeration {
    type Type = Value
    val legPress, chestFly = Value
  }

  import MachineType._
  def apply(phMachineType: Type, ma: ActorRef[MachineActor.Msg], machineID: String): Behavior[Msg] = {
    phMachineType match{
        case MachineType.legPress => LegPress(ma, machineID)
        case MachineType.chestFly => ChestFly(ma, machineID)

    }
  }

  private class ChestFly(context: ActorContext[Msg], override val ma: ActorRef[MachineActor.Msg], override val machineID: String)
    extends AbstractBehavior[Msg](context) with PhysicalMachine{

    override def display(s: String): Unit = {
      val _display: String = machineID + " " + s
    }

    override val logName: String = "PM ChestFly: "+machineID
    override val loggingContext: ActorContext[Msg] = this.context
  }
  object ChestFly{
    def apply(ma: ActorRef[MachineActor.Msg], machineID: String): Behavior[Msg] = Behaviors.setup(context => new ChestFly(context, ma, machineID))
  }

  private class LegPress(override val context: ActorContext[Msg], override val ma: ActorRef[MachineActor.Msg], override val machineID: String)
    extends AbstractBehavior[Msg](context) with PhysicalMachine{

    override def display(s: String): Unit = {
      val _display: String = machineID + " " + s
    }

    override val logName: String = "PM LegPress: "+machineID
    override val loggingContext: ActorContext[Msg] = this.context
  }
  object LegPress{
    def apply(ma: ActorRef[MachineActor.Msg], machineID: String): Behavior[Msg] = Behaviors.setup(context => new LegPress(context, ma, machineID))
  }

}