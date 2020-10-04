package AverageJoes.model.machine

import AverageJoes.common.MsgActorMessage._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
 * Message structure class
 * actorRef:ActorRef message sender
 * msg:String message
 */
case class MsgConstructor(actorRef: ActorRef, msg: String)
/**
 * Machine actor class
 * userActorRef:ActorRef user actor ref (till server is down)
 */
//serve il riferimento del controller, non dell'attore
class MachineActor(userActorRef: ActorRef) extends Actor{
  def receive: Receive = {
    case MsgConstructor(actorRef,"USER_LOG_IN") => userActorRef ! MsgConstructor(self,"USER_LOGGED")
    case m: MsgUserLogin => m.userID //gira m al controller
    case m: MsgUserRef => m.user ! MsgUserLoggedInMachine(self)
    case _ => print("ERROR_MACHINE")
  }

  def availabilityCheck(): Unit = ???

  def connecting(): Unit = ???

  def ready(): Unit = ???

  def running(): Unit = ???

  def updatingData(): Unit = ???

  def resetting(): Unit = ???

  def idle(): Unit = ???
}

trait Machine {
  def isBooked: Boolean
  def getHeartbeat(): Int
  def getID(): Int
}
/**
 * Class to verify if the system  works
 */
class ActorVerifier() extends Actor {
  override def receive: Receive = {
    case MsgConstructor(actorRef,"USER_LOGGED") => print("SUCCESS")
    case _ => print("ERROR_ACTOR")
  }
}

object ActorTest extends App {
  val system = ActorSystem("mySystem")
  val actor = system.actorOf(Props[ActorVerifier](), "actorVerify")
  val machine = system.actorOf(Props(classOf[MachineActor], actor), "machineActor")
  machine ! MsgConstructor(actor, "USER_LOG_IN")
}

