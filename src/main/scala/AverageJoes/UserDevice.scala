package AverageJoes

import akka.actor.{AbstractActor, ActorRef}

trait UserDevice extends AbstractActor{
  val userID: String
  val userActor: ActorRef

  def display (s: String): Unit

  def rfid : Unit

  override def createReceive(): AbstractActor.Receive = ???
}
