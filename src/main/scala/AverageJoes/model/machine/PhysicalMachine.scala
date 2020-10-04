package AverageJoes.model.machine

import AverageJoes.common.MsgActorMessage._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

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
  def startPhysicalMachine(actSystem: ActorSystem, machineID: String, ma: ActorRef, machineType: Class[_ <: PhysicalMachine]): ActorRef = {
    val machine = actSystem.actorOf(Props(machineType, ma, machineID), machineID)

    //system.actorOf(Props(classOf[MyActor], arg1, arg2), "name")
    //childPM = childPM + (machineID -> machine)

    machine
  }

}