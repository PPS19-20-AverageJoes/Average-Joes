package AverageJoes.common

import AverageJoes.controller.GymController
import akka.actor.typed.{ActorRef, ActorSystem}

trait ServerSearch {
  def server: ActorRef[GymController.Msg] =
  {
    ServerSearch.serverDummy
  }
}

object ServerSearch{
  private val serverDummy: ActorSystem[GymController.Msg] = ActorSystem(GymController(), "GymController")
}