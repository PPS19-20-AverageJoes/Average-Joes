package AverageJoes.model.machine

import AverageJoes.common.MsgActorMessage._
import AverageJoes.common.ServerSearch
import AverageJoes.controller.HardwareController
import akka.actor.{Actor, ActorContext, ActorRef, ActorRefFactory, ActorSystem, Props}

sealed trait PhysicalMachine extends Actor{
  val machineID: String //TODO: recuperare da configurazione su DB?
  val ma: ActorRef //MachineActor

  def display (s: String): Unit

  //MsgPhysicalMachineWakeUp to Controller

  override def receive: Receive = {
    case m: MsgRfid => ma ! MsgUserLogin(m.userID)
    case m: MsgDisplay => display(m.message)
  }
}

case class ChestFly(ma: ActorRef, machineID: String) extends PhysicalMachine{
  override def display(s: String): Unit = {
    val _display: String = machineID + " " + s
  }
}
case class LegPress(ma: ActorRef, machineID: String) extends PhysicalMachine{
  override def display(s: String): Unit = {
    val _display: String = machineID + " " + s
  }
}

object PhysicalMachine {

  //Every PhysicalMachine need a daemon that tell the server of the starting up and retreive the actorref of the virtual Machine
  def startDaemon(actorRefFactory: ActorRefFactory, machineID: String, machineType: Class[_ <: PhysicalMachine]): Unit ={
    actorRefFactory.actorOf(Props(classOf[PMDaemon], machineID, machineType), machineID)
  }

  case class PMDaemon(machineID: String, machineType: Class[_ <: PhysicalMachine]) extends Actor with ServerSearch{
    server ! MsgPhysicalMachineWakeUp(machineID)

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