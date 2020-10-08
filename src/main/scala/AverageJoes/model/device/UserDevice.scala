package AverageJoes.model.device

import AverageJoes.common.MsgActorMessage.MsgDeviceInGym
import AverageJoes.controller.GymController
import akka.actor.{Actor, ActorRef}

/**
 * AC
 */
trait UserDevice extends Actor{
  val deviceID: String

  //Search for the Gym Controller and send a message
  GymController.controller ! MsgDeviceInGym(deviceID) //ToDo: dinamic search

  def display (s: String): Unit

  def rfid(ref: ActorRef) : Unit //ToDo: convert rfid to machineCommunicationStrategy type rfid? Dovremmo utilizzare una funzione Currying?

}

object UserDevice {

}