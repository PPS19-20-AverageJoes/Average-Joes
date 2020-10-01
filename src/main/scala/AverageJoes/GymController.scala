package AverageJoes

import akka.actor.Actor

class GymController extends Actor{
  override def receive: Receive = {
    case MsgUserInGym => val userDeviceActor = sender()
  }
}
