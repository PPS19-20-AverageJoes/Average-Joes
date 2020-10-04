package AverageJoes.model.device

import akka.actor.{Actor, ActorRef}

/**
 * AC
 */
trait UserDevice extends Actor{
  val deviceID: String
  //val userActor: ActorRef

  def display (s: String): Unit

  def rfid(ref: ActorRef) : Unit //ToDo: convert rfid to machineComunicationStategy type rfid? Dovremmo utilizzare una funzione Currying?

}
