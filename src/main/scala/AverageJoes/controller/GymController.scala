package AverageJoes.controller

import AverageJoes.common.MsgUserInGym
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class GymController extends Actor{
  override def receive: Receive = {
    case MsgUserInGym => val userDeviceActor = sender()
  }
}

object GymController {
  private var _controller: ActorRef = null //TODO: implementare meglio

  def controller(actSystem: ActorSystem): ActorRef = {
    if (_controller == null)
        _controller = actSystem.actorOf(Props[GymController])

    _controller
  }
}