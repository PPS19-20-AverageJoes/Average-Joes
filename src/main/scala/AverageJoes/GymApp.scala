package AverageJoes

import akka.actor.ActorSystem

object GymApp extends App{
  val actSystem = ActorSystem("Gym")
  val controller = GymController.controller(actSystem)


}
