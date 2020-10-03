package AverageJoes.model.wristband

import AverageJoes.common.{MsgRfid, MsgUserLogged}
import akka.actor.ActorRef

/**
 * AC
 * @param userID: User ID
 * @param userActor: Actor of the user
 */
class Wristband(val userID: String, val userActor: ActorRef) extends UserDevice {

  def display (s: String): Unit ={
    println(s)
  }
/* */
  def rfid(ref: ActorRef) : Unit ={
    ref ! MsgRfid(userID)
  }

  override def receive: Receive = {
    case m: MsgUserLogged => display(m.machineID)
  }
}
