package AverageJoes.model.customer

import AverageJoes.model.customer.Customer.NotifyWristband
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit


class CustomerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Customer actor" must {

    "notify wristband" in {
      val probe = createTestProbe[NotifyWristband]
      val customerActor = spawn(Customer("group", "customer"))

      customerActor ! Customer.NotifiedByMachine(requestId = 42, probe.ref)
      val response = probe.receiveMessage()
      response.requestId should ===(42)
    }
  }
}