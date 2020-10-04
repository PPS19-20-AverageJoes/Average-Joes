package AverageJoes.model.device

import AverageJoes.common.MsgActorMessage._
import akka.actor.ActorRef

/**
 * AC
 * @param deviceID: ID of the device. In physical devices, is stored on config files
 */
class Wristband(val deviceID: String) extends UserDevice {

  def display (s: String): Unit ={
    println(s)
  }

  def rfid(ref: ActorRef) : Unit ={
    ref ! MsgRfid(deviceID)
  }

  //noinspection SpellCheckingInspection
  //ToDo: è possibile uilizzare sia la receive della classe che quella della superclasse?
  override def receive: Receive = {
    case m: MsgUserLoggedInMachine => display(m.refMachineActor.toString()) //ToDo: va passato un id o similari
    case m: MsgNearDevice => rfid(m.device)
  }
}
