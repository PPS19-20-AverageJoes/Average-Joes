package AverageJoes.model.machine

import AverageJoes.MsgDisplay
import AverageJoes.common.{MsgDisplay, MsgRfid, MsgUserLogin}
import akka.actor.Actor

sealed trait PhysicalMachine extends Actor{
  val machineID: String //TODO: recuperare da configurazione su DB
  val ma: MachineActor

  def display (s: String): Unit

  //MsgPhysicalMachineWakeUp to Controller

  override def receive: Receive = {
    case m: MsgRfid => ma.self ! MsgUserLogin(m.userID)
    case m: MsgDisplay => display(m.message)
  }
}

case class ChestFly(ma: MachineActor, machineID: String) extends PhysicalMachine{
  override def display(s: String): Unit = {
    val _display: String = machineID + " " + s
  }
}
case class LegPress(ma: MachineActor, machineID: String) extends PhysicalMachine{
  override def display(s: String): Unit = {
    val _display: String = machineID + " " + s
  }
}

object PhysicalMachine {

}