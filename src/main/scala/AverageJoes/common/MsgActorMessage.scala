package AverageJoes.common

import akka.actor.ActorRef

sealed trait MsgActorMessage

object MsgActorMessage {
  //TODO: definire un tipo per userID?
  case class MsgRfid(userID: String) extends MsgActorMessage //Rfid fired
  case class MsgDeviceInGym(deviceID: String) extends MsgActorMessage //Device enter in Gym
  //case class MsgUserInGym(user: ActorRef) extends MsgActorMessage //User enter in Gym
  case class MsgPhysicalMachineWakeUp(machineID: String) extends MsgActorMessage //Login to the controller
  case class MsgMachineActorStarted(machine: ActorRef) extends MsgActorMessage
  case class MsgPMActorStarted(machineID: String, phMachine: ActorRef) extends MsgActorMessage
  case class MsgUserLogin(userID: String) extends MsgActorMessage //User logged
  case class MsgUserLoggedInMachine(refMachineActor: ActorRef) extends MsgActorMessage //User logged
  case class MsgUserRef(user: ActorRef) extends MsgActorMessage
  case class MsgDisplay(message: String) extends MsgActorMessage
  case class MsgNearDevice(device:ActorRef) extends MsgActorMessage

}
