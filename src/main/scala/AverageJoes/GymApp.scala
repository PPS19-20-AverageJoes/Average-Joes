package AverageJoes

import akka.actor.ActorSystem
import AverageJoes.controller.GymController

object GymApp extends App{
  val actSystem = ActorSystem("Gym")
  val controller = GymController.controller(actSystem)

}
