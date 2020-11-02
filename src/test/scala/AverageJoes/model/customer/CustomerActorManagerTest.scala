package AverageJoes.model.customer

import AverageJoes.model.customer.CustomerManager._
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerActorManagerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Customer manager actor" should {
    "reply to registration request" in {
      val probe = createTestProbe[CustomerRegistered]()
      val managerActor = spawn(CustomerManager())

      managerActor ! RequestCustomerLogin("customer", probe.ref)
      val registered1 = probe.receiveMessage()

      managerActor ! RequestCustomerLogin("customer", probe.ref)
      val registered2 = probe.receiveMessage()

      registered1.customer should === (registered2.customer)
    }
  }

}
