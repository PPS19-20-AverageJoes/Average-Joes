package AverageJoes.model.wristband

import akka.actor.{Actor, ActorRef}

/**
 * AC
 */
trait UserDevice extends Actor{
  val userID: String
  val userActor: ActorRef

  def display (s: String): Unit

  def rfid(ref: ActorRef) : Unit

}
