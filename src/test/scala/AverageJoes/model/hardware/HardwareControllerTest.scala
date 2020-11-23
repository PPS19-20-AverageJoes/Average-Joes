package AverageJoes.model.hardware

import AverageJoes.model.hardware.HardwareController.Msg
import AverageJoes.model.workout.MachineTypes
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorSystem
import org.scalatest.wordspec.AnyWordSpecLike

class HardwareControllerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "Hardware controller" must{
    val controller: ActorSystem[Msg] = ActorSystem(HardwareController(), "GymHardware")

    "create device" in {
      val deviceName = "Wristband1"
      controller ! Msg.CreateDevice(deviceName, Device.DeviceType.wristband, deviceName)
      Thread.sleep(2000)
      assert(HardwareController.getChildDevice(deviceName).nonEmpty)
    }

    "create physical machine" in {
      val machineName = "LegPress1"
      controller ! Msg.CreatePhysicalMachine(machineName, MachineTypes.LEG_PRESS, machineName)
      Thread.sleep(2000)
      assert(HardwareController.getChildPmByName(machineName).nonEmpty)
    }
  }
}