package AverageJoes.model.device

import AverageJoes.common.MsgActorMessage.MsgDeviceInGym
import AverageJoes.common.ServerSearch
import AverageJoes.controller.GymController
import akka.actor.{Actor, ActorRef}

/**
 * AC
 */
trait UserDevice extends Actor with ServerSearch {
  val deviceID: String

  //Search for the Gym Controller (the server) and send a message
  server ! MsgDeviceInGym(deviceID)

  def display (s: String): Unit

  def rfid(ref: ActorRef) : Unit //ToDo: convert rfid to machineCommunicationStrategy type rfid? Dovremmo utilizzare una funzione Currying?

}

object UserDevice {

}