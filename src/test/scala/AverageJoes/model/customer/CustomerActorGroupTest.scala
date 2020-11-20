package AverageJoes.model.customer

import AverageJoes.controller.GymController
import AverageJoes.controller.GymController.Msg.CustomerRegistered
import AverageJoes.model.customer.CustomerGroup.CustomerLogin
import AverageJoes.model.customer.CustomerManager.RequestCustomerCreation
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.CustomerLogging
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import org.scalatest.wordspec.AnyWordSpecLike


class CustomerActorGroupTest extends ScalaTestWithActorTestKit with AnyWordSpecLike{

  val deviceProbe: TestProbe[Device.Msg] = createTestProbe[Device.Msg]()
  val gymProbe: TestProbe[GymController.Msg] = createTestProbe[GymController.Msg]()
  val machineProbe: TestProbe[MachineActor.Msg] = createTestProbe[MachineActor.Msg]()
  val physicalMachineProbe: TestProbe[PhysicalMachine.Msg] = createTestProbe[PhysicalMachine.Msg]()

  val managerActor: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())

/*
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

    "review logging request" in {
      /* No customer registered */
      val groupActor = spawn(CustomerGroup("group", managerActor))
      groupActor ! CustomerLogin("customer-no","machine-label", physicalMachineProbe.ref, machineProbe.ref, deviceProbe.ref)

      val negativeRespMachine = machineProbe.receiveMessage()

      assert(negativeRespMachine.isInstanceOf[CustomerLogging])

      negativeRespMachine match {
        case CustomerLogging("customer-no", _, isLogged) => assert(isLogged === false)
        case _ => assert(false)
      }
    }

  } */
}