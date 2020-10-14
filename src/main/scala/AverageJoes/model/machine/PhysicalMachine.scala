package AverageJoes.model.machine

import AverageJoes.common.MsgActorMessage._
import AverageJoes.common.{MsgActorMessage, ServerSearch}
import AverageJoes.controller.HardwareController
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, Behaviors}

sealed trait PhysicalMachine extends AbstractBehavior[PhysicalMachine.MsgPhyMachine]{
  val machineID: String //TODO: recuperare da configurazione su DB?
  val ma: ActorRef[MsgActorMessage] //MachineActor


  override def onMessage(msg: PhysicalMachine.MsgPhyMachine): Behavior[PhysicalMachine.MsgPhyMachine] = {
    msg match{
      case PhysicalMachine.MsgRfid(userID) => ma ! MsgUserLogin(userID); Behaviors.same
      case PhysicalMachine.MsgDisplay(message) => display(message); Behaviors.same
    }
  }

  def display (s: String): Unit

}

case class ChestFly(ma: ActorRef[MsgActorMessage], machineID: String) extends PhysicalMachine{
  override def display(s: String): Unit = {
    val _display: String = machineID + " " + s
  }
}
case class LegPress(ma: ActorRef[MsgActorMessage], machineID: String) extends PhysicalMachine{
  override def display(s: String): Unit = {
    val _display: String = machineID + " " + s
  }
}

object PhysicalMachine {
  sealed trait MsgPhyMachine
  case class MsgRfid(userID: String) extends MsgPhyMachine //Rfid fired
  case class MsgDisplay(message: String) extends MsgPhyMachine

  sealed trait MsgDaemon
  case class MsgMachineActorStarted(refMA: ActorRef[MsgActorMessage]) extends MsgDaemon

  //Every PhysicalMachine need a daemon that tell the server of the starting up and retreive the actorref of the virtual Machine
  def startDaemon(actorRefFactory: ActorRefFactory, machineID: String, machineType: Class[_ <: PhysicalMachine]): Unit ={
    actorRefFactory.actorOf(Props(classOf[PMDaemon], machineID, machineType), machineID)
  }

  case class PMDaemon(machineID: String, machineType: Class[_ <: PhysicalMachine]) extends AbstractBehavior[MsgDaemon] with ServerSearch{
    server ! MsgPhysicalMachineWakeUp(machineID)

    override def onMessage(msg: MsgDaemon): Behavior[MsgDaemon] = {
      msg match{
        case MsgMachineActorStarted(refMA) => {
          val pm = startPhysicalMachine(context, machineID,machineType ,refMA)
          context.spawn[](PhysicalMachine)
          refMA ! MsgPMActorStarted(machineID, pm)

          //ToDo: kill daemon
          Behaviors.same
        }
      }
    }


    override def receive: Receive = {
      case m: MsgMachineActorStarted => {
        val pm = startPhysicalMachine(context, machineID,machineType ,m.machine)
        m.machine ! MsgPMActorStarted(machineID, pm)
        context.parent ! MsgPMActorStarted(machineID, pm)
        //ToDo: kill daemon
      }

    }

    def startPhysicalMachine(actorRefFactory: ActorRefFactory, machineID: String, machineType: Class[_ <: PhysicalMachine], ma: ActorRef): ActorRef = {
      actorRefFactory.actorOf(Props(machineType, ma, machineID), machineID)
    }
  }

}