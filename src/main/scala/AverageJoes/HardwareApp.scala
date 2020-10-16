package AverageJoes

import AverageJoes.controller.{GymController, HardwareController}
import AverageJoes.model.device.Wristband
import AverageJoes.model.machine._
import akka.actor.typed.ActorSystem

object HardwareApp extends App{
  private val actSystem = ActorSystem("Gym")
  private val controller = actSystem. HardwareController.startHardwareController(actSystem)
  //HardwareController.gymController = GymController.startGymController(actSystem)

  val legPress1 = PhysicalMachine.startDaemon(actSystem,"LegPress1",classOf[LegPress])
  val chestFly1 = PhysicalMachine.startDaemon(actSystem,"ChestFly1",classOf[ChestFly])
  val wristband1= Wristband.startWristband(actSystem,"Wristband1")
  val wristband2= Wristband.startWristband(actSystem,"Wristband2")

}
