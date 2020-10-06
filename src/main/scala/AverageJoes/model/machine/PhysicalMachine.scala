package AverageJoes.model.machine

import AverageJoes.common.MsgActorMessage._
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
  def startPhysicalMachine(actorRefFactory: ActorRefFactory, machineID: String, machineType: Class[_ <: PhysicalMachine], ma: ActorRef): ActorRef = {
    actorRefFactory.actorOf(Props(machineType, ma, machineID), machineID)
  }

  //noinspection SpellCheckingInspection
  //ogni macchina fisica deve avere un demone che pinga il server e si fa restituire l'actor ref del server e della macchina virtuale
  def startDemon(actorRefFactory: ActorRefFactory, machineID: String, machineType: Class[_ <: PhysicalMachine]): Unit ={
    actorRefFactory.actorOf(Props(classOf[PMDemon], machineID, machineType), machineID)
  }

  case class PMDemon(machineID: String, machineType: Class[_ <: PhysicalMachine]) extends Actor{

    HardwareController.gymController ! MsgPhysicalMachineWakeUp(machineID)

    override def receive: Receive = {
      case m: MsgMachineActorStarted => {
        val pm = startPhysicalMachine(context, machineID,machineType ,m.machine)
        sender() ! MsgPMActorStarted(machineID, pm)
        context.parent ! MsgPMActorStarted(machineID, pm)
      }

    }
  }

}