package AverageJoes.controller

import AverageJoes.common.ServerSearch
import AverageJoes.model.customer.CustomerActor
import AverageJoes.model.hardware.{Device, HardwareController, PhysicalMachine}
import AverageJoes.model.hardware.HardwareController.Msg
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.workout.MachineTypes
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestInbox}
import akka.actor.typed.{ActorRef, ActorSystem}
import org.scalatest.wordspec.AnyWordSpecLike

class GymControllerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  val milliSleep: Long = 2000

  "Gym controller" must{
    val controller: ActorSystem[Msg] = ActorSystem(HardwareController(), "GymHardware")
    val gymController = GetGymController().server

    "create customer" in {
      val deviceName = "Wristband1"
      val probeMachine = TestInbox[MachineActor.Msg]()
      val probePhMachine = TestInbox[PhysicalMachine.Msg]()

      controller ! Msg.CreateDevice(deviceName, Device.DeviceType.wristband, deviceName)
      Thread.sleep(milliSleep)

      gymController ! GymController.Msg.UserLogin(deviceName,"ph",MachineTypes.LEG_PRESS,probePhMachine.ref,probeMachine.ref)

      Thread.sleep(milliSleep)

      probeMachine.receiveMessage() should matchPattern {
        case MachineActor.Msg.CustomerLogging(`deviceName`, _, _, _) => }
    }

    "create machine" in {
      val machineName = "LegPress1"
      val probeCustomer = TestInbox[CustomerActor.Msg]()

      controller ! Msg.CreatePhysicalMachine(machineName, MachineTypes.LEG_PRESS, machineName)
      Thread.sleep(milliSleep)

      gymController ! GymController.Msg.MachinesToBookmark(MachineTypes.LEG_PRESS, probeCustomer.ref)
      Thread.sleep(milliSleep)

      probeCustomer.receiveMessage() should matchPattern {
        case CustomerActor.MachineList(l) if l.nonEmpty => }
    }
  }

  //To access the same GymController of the HardwareController
  case class GetGymController() extends ServerSearch
}
