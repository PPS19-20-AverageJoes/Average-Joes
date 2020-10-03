package AverageJoes.controller

import AverageJoes.common.MsgUserInGym
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object GymController {
  class GymController extends Actor{
    override def receive: Receive = {
      case MsgUserInGym => val userDeviceActor = sender()
    }
  }

  private var _controller: Option[ActorRef] = None //TODO: implementare meglio

  def controller(actSystem: ActorSystem): ActorRef = {
    if (_controller.isEmpty)
      _controller = Some(actSystem.actorOf(Props[GymController]))

    _controller.get
  }

}