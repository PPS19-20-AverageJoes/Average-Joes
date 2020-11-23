package averageJoes.model.customer

import averageJoes.controller.GymController
import averageJoes.model.customer.CustomerManager.RequestCustomerCreation
import averageJoes.model.hardware.Device
import averageJoes.model.hardware.Device.Wristband
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerActorManagerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val deviceActor: ActorRef[Device.Msg] = spawn(Wristband("customer-1", "customer-1"))
  val gymProbe: TestProbe[GymController.Msg] = createTestProbe[GymController.Msg]()
  val managerProbe: TestProbe[CustomerManager.Msg] = createTestProbe[CustomerManager.Msg]()

   "Customer manager actor" should {

    "create one customer" in {
      managerProbe.ref ! RequestCustomerCreation("Wristband1", gymProbe.ref, deviceActor)
      assert(managerProbe.receiveMessage.isInstanceOf[RequestCustomerCreation])
    }
  }


}