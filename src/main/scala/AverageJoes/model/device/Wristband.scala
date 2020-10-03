package AverageJoes.model.device

import AverageJoes.common.{MsgRfid, MsgUserLogged}
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

  override def receive: Receive = {
    case m: MsgUserLogged => display(m.machineID)
  }
}
