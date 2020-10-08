package AverageJoes

import AverageJoes.controller.{GymController, HardwareController}
import AverageJoes.model.device._
import AverageJoes.model.machine._
import AverageJoes.model.user.SmartGymUserImpl
import akka.actor.{ActorSystem, Props}

object HardwareApp extends App{
  private val actSystem = ActorSystem("Gym")
  private val controller = HardwareController.startHardwareController(actSystem)
  HardwareController.gymController = GymController.startGymController(actSystem)



}
