package AverageJoes.model.hardware

import AverageJoes.common.ServerSearch
import AverageJoes.controller.GymController
import AverageJoes.model.customer.CustomerActor
import AverageJoes.model.hardware.HardwareController.Msg
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.workout.MachineTypes
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestInbox}
import akka.actor.typed.ActorSystem
import org.scalatest.wordspec.AnyWordSpecLike

class HardwareControllerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  val milliSleep: Long = 2000

  "Hardware controller" must{
    val controller: ActorSystem[Msg] = ActorSystem(HardwareController(), "GymHardware")
    val gymController = GetGymController().server

    "create device and customer" in {
      val deviceName = "Wristband1"
      val probeMachine = TestInbox[MachineActor.Msg]()
      val probePhMachine = TestInbox[PhysicalMachine.Msg]()

      controller ! Msg.CreateDevice(deviceName, Device.DeviceType.wristband, deviceName)
      Thread.sleep(milliSleep)
      assert(HardwareController.getChildDevice(deviceName).nonEmpty)

      gymController ! GymController.Msg.UserLogin(deviceName,"ph",MachineTypes.LEG_PRESS,probePhMachine.ref,probeMachine.ref)
      Thread.sleep(milliSleep)
      probeMachine.receiveMessage() should matchPattern {
        case MachineActor.Msg.CustomerLogging(`deviceName`, _, _, _) => }
    }

    "create physical machine and machine actor" in {
      val machineName = "LegPress1"
      val probeCustomer = TestInbox[CustomerActor.Msg]()

      controller ! Msg.CreatePhysicalMachine(machineName, MachineTypes.LEG_PRESS, machineName)
      Thread.sleep(milliSleep)
      assert(HardwareController.getChildPmByName(machineName).nonEmpty)

      gymController ! GymController.Msg.MachinesToBookmark(MachineTypes.LEG_PRESS, probeCustomer.ref)
      Thread.sleep(milliSleep)

      probeCustomer.receiveMessage() should matchPattern {
        case CustomerActor.MachineList(l) if l.nonEmpty => }
    }
  }
  //To access the same GymController of the HardwareController
  case class GetGymController() extends ServerSearch
}