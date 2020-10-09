package AverageJoes.model.user

import AverageJoes.model.user.UserManager.{RequestUserCreation, UserRegistered}
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class UserManagerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "User manager actor" must {
    "reply to registration request" in {
      val probe = createTestProbe[UserRegistered]()
      val managerActor = spawn(UserManager())

      managerActor ! RequestUserCreation("group1", "user", probe.ref)
      val registered1 = probe.receiveMessage()

      /* Adding another group */
      managerActor ! RequestUserCreation("group2", "user", probe.ref)
      val registered2 = probe.receiveMessage()

      registered1.user should !== (registered2.user)
    }
  }

}
