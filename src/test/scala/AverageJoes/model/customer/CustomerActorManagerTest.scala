package AverageJoes.model.customer

import AverageJoes.controller.GymController
import AverageJoes.controller.GymController.Msg.CustomerRegistered
import AverageJoes.model.customer.CustomerManager.RequestCustomerCreation
import AverageJoes.model.hardware.Device
import AverageJoes.model.hardware.Device.Wristband
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerActorManagerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  /*
  val deviceActor: ActorRef[Device.Msg] = spawn(Wristband("device-01", "device-01"))
  val probe: TestProbe[GymController.Msg] = createTestProbe[GymController.Msg]()

   "Customer manager actor" should {
    /** TODO: GymActor doesn't use customer registered */
    "create one customer" in {
      val managerActor: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())
      managerActor ! RequestCustomerCreation("customer", probe.ref, deviceActor)
      val registered = probe.receiveMessage()
      assert(registered.isInstanceOf[CustomerRegistered])
    }
  }

  "return the same customer, for the same customerId creation request" in {
    val managerActor: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())
    managerActor ! RequestCustomerCreation("customer-same", probe.ref, deviceActor)
    val registered1 = probe.receiveMessage()

    managerActor ! RequestCustomerCreation("customer-same", probe.ref, deviceActor)
    val registered2 = probe.receiveMessage()
    assert(registered1 == registered2)

    managerActor ! RequestCustomerCreation("customer-other", probe.ref, deviceActor)
    val registered3 = probe.receiveMessage()

    assert(registered1 == registered2)
    assert(registered2 !== registered3)
    assert(registered1 !== registered3)
  }



 */

}