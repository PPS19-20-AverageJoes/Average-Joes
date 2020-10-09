package AverageJoes.model.machine

import AverageJoes.common.MsgActorMessage._
import akka.actor.{Actor, ActorRef}
/**
 * Machine actor class
 * controller: controller ActorRef
 */
class MachineActor(controller: ActorRef /*, machineType: Class[_ <: PhysicalMachine]*/) extends Actor{
  var booked: (Boolean, String) = (false, "")
  var phMachineAct: ActorRef = _

  def receive: Receive = {
    case m: MsgPMActorStarted => phMachineAct = sender()
    case m: MsgUserLogin =>   availabilityCheck(m.userID)
    case m: MsgMachineBooking => if(!booked._1){
                              booked = (true, m.userID) }
                              sender() ! MsgBookingStatus(booked._1)
                              phMachineAct ! MsgBookingStatus(booked._1)
    case _ => print("ERROR_MACHINE")
  }

  def availabilityCheck(userId: String): Unit = {
    if (!booked._1 || (booked._1 && booked._2.equals(userId))) {
      booked = (false,"")
      controller ! MsgUserLogin(userId)
      context.become(connecting())
    } else {
      controller ! MsgUnableToLogIn(userId)
      context.become(receive)
    }
  }

  def connecting(): Receive = {
    case m: MsgUserRef => m.user ! MsgUserLoggedInMachine(self)
      context.become(updateAndLogOut()) // workout data received
                            // send setting data to machine
  }

  def updateAndLogOut(): Receive = {
    case m: MsgDisplay => { } //get user workout data and send it to server  //send log out msg to user
  }
}


