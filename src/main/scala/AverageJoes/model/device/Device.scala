package AverageJoes.model.device

import AverageJoes.common.{LogOnMessage, LoggableMsg, ServerSearch}
import AverageJoes.controller.GymController
import AverageJoes.model.machine.{MachineActor, PhysicalMachine}
import akka.actor.typed.scaladsl.{AbstractBehavior, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

/**
 * AC
 */
trait Device extends AbstractBehavior[Device.Msg] with ServerSearch with LogOnMessage[Device.Msg]{
  val customerID: String

  //Search for the Gym Controller (the server) and send a message
  server ! GymController.Msg.DeviceInGym(customerID, context.self)

  //noinspection SpellCheckingInspection
  //ToDo: è possibile uilizzare sia la receive della classe che quella della superclasse?
  override def onMessageLogged(msg: Device.Msg): Behavior[Device.Msg] = {
    msg match{
      case m: Device.Msg.UserLoggedInMachine => display(m.machineLabel); Behaviors.same
      case m: Device.Msg.NearDevice => rfid(m.refPM); Behaviors.same
    }
  }


  def display (s: String): Unit

  def rfid(ref: ActorRef[PhysicalMachine.Msg]) : Unit //ToDo: convert rfid to machineCommunicationStrategy type rfid? Dovremmo utilizzare una funzione Currying?

}

object Device {

  sealed trait Msg extends LoggableMsg
  object Msg {
    final case class UserLoggedInMachine(machineLabel: String) extends Msg //ToDo: SafePropertyValue sulla lunghezza
    final case class NearDevice(refPM: ActorRef[PhysicalMachine.Msg]) extends Msg
    //case class MsgDisplay(message: String) extends MsgDevice
  }

  object DeviceType extends Enumeration {
    type Type = Value
    val wristband = Value
  }

  import DeviceType._
  def apply(phMachineType: Type, deviceID: String): Behavior[Msg] = {
    phMachineType match{
      case DeviceType.wristband => Wristband(deviceID)
    }
  }


}