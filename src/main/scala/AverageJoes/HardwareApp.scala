package AverageJoes

import AverageJoes.controller.HardwareController
import AverageJoes.model.device._
import AverageJoes.model.machine._
import AverageJoes.model.user.SmartGymUserImpl
import akka.actor.{ActorSystem, Props}

object HardwareApp extends App{
  private val actSystem = ActorSystem("Gym")
  //private val controller = HardwareController(actSystem)

  val userActor = actSystem.actorOf(Props(new SmartGymUserImpl("","","","")), "actorUser")
  val machine = actSystem.actorOf(Props(classOf[MachineActor], userActor), "machineActor")

  private val legPress = PhysicalMachine.startPhysicalMachine(actSystem, "1", machine, classOf[LegPress])
  //private val wristband = controller.startDevice("Wristband1",Class[Wristband])
  //private val legPress = controller.startPhysicalMachine("LegPress1",Class[LegPress])

}
