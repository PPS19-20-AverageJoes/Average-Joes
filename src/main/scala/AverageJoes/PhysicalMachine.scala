package AverageJoes

import akka.actor.Actor

trait PhysicalMachine extends Actor{
  val ma: MachineActor

  override def receive: Receive = {
    case m: MsgRfid => ma.self ! MsgUserLogin(m.userID)
  }
}

case class ChestFlye(ma: MachineActor) extends PhysicalMachine{}

object PhysicalMachine {

}