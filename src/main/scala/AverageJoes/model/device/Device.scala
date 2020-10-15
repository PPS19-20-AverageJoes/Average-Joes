package AverageJoes.model.device

import AverageJoes.common.MsgActorMessage.MsgDeviceInGym
import AverageJoes.common.ServerSearch
import AverageJoes.model.machine.{MachineActor, PhysicalMachine}
import AverageJoes.model.user.User
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.{ActorRef, Behavior}

/**
 * AC
 */
trait Device extends AbstractBehavior[Device.Msg] with ServerSearch {
  val deviceID: String

  //Search for the Gym Controller (the server) and send a message
  server ! MsgDeviceInGym(deviceID)

  def display (s: String): Unit

  def rfid(ref: ActorRef[PhysicalMachine.Msg]) : Unit //ToDo: insert PhysicalMachine message type.   ?convert rfid to machineCommunicationStrategy type rfid? Dovremmo utilizzare una funzione Currying?

}

object Device {
  sealed trait Msg
  case class MsgUserLoggedInMachine(refMachineActor: ActorRef[MachineActor]) extends Msg
  case class MsgNearDevice(refPM:ActorRef[Msg]) extends Msg
  //case class MsgDisplay(message: String) extends MsgDevice
}