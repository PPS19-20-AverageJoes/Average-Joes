package AverageJoes.model.customer

import AverageJoes.controller.GymController
import AverageJoes.controller.GymController.Msg.CustomerRegistered
import AverageJoes.model.customer.CustomerGroup.CustomerLogin
import AverageJoes.model.customer.CustomerManager.RequestCustomerCreation
import AverageJoes.model.device.Device.Msg.CustomerLogged
import AverageJoes.model.device.{Device, Wristband}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.CustomerLogging
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import org.scalatest.wordspec.AnyWordSpecLike

/**
 * TODO: test the device notification scenario
 */

class CustomerActorGroupTest extends ScalaTestWithActorTestKit with AnyWordSpecLike{

  val gymProbe = createTestProbe[GymController.Msg]()
  val machineProbe = createTestProbe[MachineActor.Msg]()
  val deviceProbe = createTestProbe[Device.Msg]()



  val managerActor = spawn(CustomerManager())

  "Customer group" should {

    "create one customer" in {
      val managerActor: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())

      managerActor ! RequestCustomerCreation("customer", gymProbe.ref, deviceProbe.ref)
      val registered = gymProbe.receiveMessage()

      assert(registered.isInstanceOf[CustomerRegistered])
    }

    "return the same customer actor for the same customer" in {
      val groupActor = spawn(CustomerGroup("group", managerActor))

      groupActor ! RequestCustomerCreation("customer1", gymProbe.ref, deviceProbe.ref)
      val registered1 = gymProbe.receiveMessage()

      /* Registering the same customer actor again */
      groupActor ! RequestCustomerCreation("customer1", gymProbe.ref, deviceProbe.ref)
      val registered2 = gymProbe.receiveMessage()

      managerActor ! RequestCustomerCreation("customer-other", gymProbe.ref, deviceProbe.ref)
      val registered3 = gymProbe.receiveMessage()

      assert(registered1 == registered2)
      assert(registered2 !== registered3)
      assert(registered1 !== registered3)
    }

    "review logging request and notify machine and device" in {
      /* No customer registered */
      val groupActor = spawn(CustomerGroup("group", managerActor))

      groupActor ! CustomerLogin("customer-no", machineProbe.ref, deviceProbe.ref)

      val negativeRespMachine = machineProbe.receiveMessage()

      assert(negativeRespMachine.isInstanceOf[CustomerLogging])

      negativeRespMachine match {
        case CustomerLogging("customer-no", isLogged) => assert(isLogged === false)
        case _ => assert(false)
      }

      /* Register customer-yes */
      groupActor ! RequestCustomerCreation("customer-yes", gymProbe.ref, deviceProbe.ref)
      groupActor ! CustomerLogin("customer-yes", machineProbe.ref, deviceProbe.ref)

      val positiveRespMachine = machineProbe.receiveMessage()

      assert(positiveRespMachine.isInstanceOf[CustomerLogging])

      positiveRespMachine match {
        case CustomerLogging("customer-yes", isLogged) => assert(isLogged === true)
        case _ => assert(false)
      }

      val deviceResp = deviceProbe.receiveMessage()
      assert(deviceResp.isInstanceOf[CustomerLogged])

    }

  }
}
