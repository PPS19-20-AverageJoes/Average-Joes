package AverageJoes.model.customer

import AverageJoes.model.customer.CustomerManager._
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerActorManagerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Customer manager actor" should {
    "reply to registration request" in {
      val probe = createTestProbe[CustomerRegistered]()
      val managerActor = spawn(CustomerManager())

      /** Suppose that replyTo and device have the same ref, only for testing */
      managerActor ! RequestCustomerCreation("customer", probe.ref, probe.ref)
      val registered1 = probe.receiveMessage()

      managerActor ! RequestCustomerCreation("customer", probe.ref, probe.ref)
      val registered2 = probe.receiveMessage()

      registered1.customer should === (registered2.customer)
    }
  }

}
