package AverageJoes

import AverageJoes.controller.HardwareController
import AverageJoes.model.device._
import AverageJoes.model.machine._
import akka.actor.ActorSystem

object HardwareApp {
  private val actSystem = ActorSystem("Gym")
  private val controller = HardwareController(actSystem)

  controller.startDevice("Wristband1",Class[Wristband])
  controller.startPhysicalMachine("LegPress1",Class[LegPress])
}
