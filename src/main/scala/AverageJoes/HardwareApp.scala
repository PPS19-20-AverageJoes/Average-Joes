package AverageJoes

import AverageJoes.controller.HardwareController
import AverageJoes.model.device.Device
import AverageJoes.model.machine.PhysicalMachine
import akka.actor.typed.ActorSystem

object HardwareApp extends App{
  private val controller: ActorSystem[HardwareController.Msg] = ActorSystem(HardwareController(), "GymHardware")

  controller ! HardwareController.Msg.CreatePhysicalMachine("LegPress1",PhysicalMachine.MachineType.legPress)
  controller ! HardwareController.Msg.CreatePhysicalMachine("ChestFly1",PhysicalMachine.MachineType.chestFly)

  controller ! HardwareController.Msg.CreateDevice("Wristband1", Device.DeviceType.wristband)
  controller ! HardwareController.Msg.CreateDevice("Wristband2", Device.DeviceType.wristband)

}
