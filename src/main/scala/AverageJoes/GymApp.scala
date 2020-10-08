package AverageJoes

import akka.actor.ActorSystem
import AverageJoes.controller.GymController

object GymApp extends App{
  private val actSystem = ActorSystem("Gym")
  private val controller = GymController.startGymController(actSystem)

}
