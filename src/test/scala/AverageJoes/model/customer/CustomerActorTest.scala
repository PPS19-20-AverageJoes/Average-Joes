package AverageJoes.model.customer

import AverageJoes.model.customer.CustomerActor.NotifyWristband
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit


class CustomerActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Customer actor" must {

    "notify wristband" in {
      val probe = createTestProbe[NotifyWristband]
      val customerActor = spawn(CustomerActor("group", "customer"))

      customerActor ! CustomerActor.NotifiedByMachine(requestId = 42, probe.ref)
      val response = probe.receiveMessage()
      response.requestId should ===(42)
    }
  }
}