package AverageJoes.model.machine

import AverageJoes.common.{MsgActorMessage, ServerSearch}
import AverageJoes.controller.HardwareController
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, Behaviors}

sealed trait PhysicalMachine extends AbstractBehavior[PhysicalMachine.Msg]{
  val machineID: String //TODO: recuperare da configurazione su DB?
  val ma: ActorRef[MachineActor.Msg] //MachineActor

  override def onMessage(msg: PhysicalMachine.Msg): Behavior[PhysicalMachine.Msg] = {
    msg match{
      case PhysicalMachine.Msg.Rfid(userID) => ma ! MachineActor.UserLogIn(userID); Behaviors.same
      case PhysicalMachine.Msg.Display(message) => display(message); Behaviors.same
    }
  }

  def display (s: String): Unit
}

object PhysicalMachine {
  sealed trait Msg
  object Msg{
    case class Rfid(userID: String) extends Msg //Rfid fired
    case class Display(message: String) extends Msg
  }


  sealed trait MsgDaemon
  case class MsgMachineActorStarted(refMA: ActorRef[MsgActorMessage]) extends MsgDaemon


  object MachineType extends Enumeration {
    type Type = Value
    val legPress, chestFly = Value
  }

  import MachineType._
  def apply(phMachineType: Type, ma: ActorRef[MachineActor.Msg], machineID: String): PhysicalMachine = {
    phMachineType match{
        case MachineType.legPress => LegPress(ma, machineID)
        case MachineType.chestFly => ChestFly(ma, machineID)

    }
  }

  private case class ChestFly(ma: ActorRef[MachineActor.Msg], machineID: String) extends PhysicalMachine{
    override def display(s: String): Unit = {
      val _display: String = machineID + " " + s
    }
  }
  private  case class LegPress(ma: ActorRef[MachineActor.Msg], machineID: String) extends PhysicalMachine{
    override def display(s: String): Unit = {
      val _display: String = machineID + " " + s
    }
  }
}