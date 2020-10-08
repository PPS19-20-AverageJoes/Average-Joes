package AverageJoes.common

import akka.actor.ActorRef

sealed trait MsgActorMessage

object MsgActorMessage {
  //TODO: definire un tipo per userID?
  case class MsgRfid(userID: String) extends MsgActorMessage //Rfid fired
  case class MsgUserInGym(userID: String) extends MsgActorMessage //User enter in Gym
  case class MsgUserLogin(userID: String) extends MsgActorMessage //User logged
  case class MsgUserLoggedInMachine(refMachineActor: ActorRef) extends MsgActorMessage //User logged
  case class MsgUserRef(user: ActorRef) extends MsgActorMessage
  case class MsgPhysicalMachineWakeUp() extends MsgActorMessage //Login to the controller
  case class MsgDisplay(message: String) extends MsgActorMessage
  case class MsgNearDevice(device:ActorRef) extends MsgActorMessage
  case class MsgMachineBooking(userID: String, physicalMachineRef: ActorRef) extends  MsgActorMessage
  case class MsgBookingStatus(status: Boolean) extends  MsgActorMessage
  case class MsgUnableToLogIn(userID: String) extends  MsgActorMessage
}
