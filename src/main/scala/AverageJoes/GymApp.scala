package AverageJoes

import AverageJoes.controller.GymController
import AverageJoes.model.user.UserObjectTester
import AverageJoes.view.View
import akka.actor.typed.ActorSystem

object GymApp extends App{
 // private val actSystem = ActorSystem("Gym")
 // private val controller = GymController.startGymController(actSystem)

  ActorSystem(UserObjectTester(), "user-actor-system")
  private val view = new View
}
