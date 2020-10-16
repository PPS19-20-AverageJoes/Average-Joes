package AverageJoes.model.customer

import AverageJoes.model.customer.CustomerManager.{CustomerRegistered, RequestCustomerCreation, RequestUserCreation, UserRegistered}
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerManagerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Customer manager actor" must {
    "reply to registration request" in {
      val probe = createTestProbe[CustomerRegistered]()
      val managerActor = spawn(CustomerManager())

      managerActor ! RequestCustomerCreation("group1", "customer", probe.ref)
      val registered1 = probe.receiveMessage()

      /* Adding another group */
      managerActor ! RequestCustomerCreation("group2", "customer", probe.ref)
      val registered2 = probe.receiveMessage()

      registered1.customer should !== (registered2.customer)
    }
  }

}
