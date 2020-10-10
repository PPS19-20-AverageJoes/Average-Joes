package AverageJoes.model.user

import AverageJoes.model.user.User.NotifyWristband
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit


class UserTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "User actor" must {

    "should notify wristband" in {
      val probe = createTestProbe[NotifyWristband]
      val deviceActor = spawn(User("group", "device"))

      deviceActor ! User.NotifiedByMachine(requestId = 42, probe.ref)
      val response = probe.receiveMessage()
      response.requestId should ===(42)
    }
  }
}