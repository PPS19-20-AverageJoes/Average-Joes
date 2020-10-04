package AverageJoes.controller

import AverageJoes.common.MsgActorMessage._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object GymController {
  class GymController extends Actor{
    private var childUserActor = Map.empty[String, ActorRef] //Child User
    private var childMachineActor = Map.empty[String, ActorRef] //Child Machines

    override def receive: Receive = {
      case m: MsgUserInGym => val userDeviceActor = sender()
      case m: MsgUserLogin => {
        val user = childUserActor(m.userID) //ToDo: optional, gestire
        sender() ! MsgUserRef(user)
      }
    }
  }

  private var _controller: Option[ActorRef] = None //TODO: implementare meglio

  def controller(actSystem: ActorSystem): ActorRef = {
    if (_controller.isEmpty)
      _controller = Some(actSystem.actorOf(Props[GymController]))

    _controller.get
  }

}