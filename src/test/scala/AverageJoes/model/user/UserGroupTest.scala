package AverageJoes.model.user

import scala.concurrent.duration._
import AverageJoes.model.user.User.{Passivate, UserAlive, UserAliveSignal}
import AverageJoes.model.user.UserManager.{ReplyUserList, RequestUserCreation, RequestUserList, UserRegistered}
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class UserGroupTest extends ScalaTestWithActorTestKit with AnyWordSpecLike{

  "User group" must {

    "register a user actor" in {
      val probe = createTestProbe[UserRegistered]()
      val groupActor = spawn(UserGroup("group"))

      /* User 1 */
      groupActor ! RequestUserCreation("group", "user1", probe.ref)
      val registered1 = probe.receiveMessage()
      val userActor1 = registered1.user

      /* User 2 */
      groupActor ! RequestUserCreation("group", "user2", probe.ref)
      val registered2 = probe.receiveMessage()
      val userActor2 = registered2.user
      userActor1 should !== (userActor2)


      /* Check if the user actors are running */
      val recordProbe = createTestProbe[UserAliveSignal]()
      userActor1 ! UserAlive(requestId = 10L, recordProbe.ref)
      recordProbe.expectMessage(UserAliveSignal(10L))
      userActor2 ! UserAlive(requestId = 11L, recordProbe.ref)
      recordProbe.expectMessage(UserAliveSignal(11L))
    }

    "ignore request for wrong groupId" in {
      val probe = createTestProbe[UserRegistered]()
      val groupActor = spawn(UserGroup("group"))

      groupActor ! RequestUserCreation("wrongGroup", "user1", probe.ref)
      probe.expectNoMessage(500.milliseconds)
    }

    "return the same user actor for the same user" in {
      val probe = createTestProbe[UserRegistered]()
      val groupActor = spawn(UserGroup("group"))

      groupActor ! RequestUserCreation("group", "user1", probe.ref)
      val registered1 = probe.receiveMessage()

      /* Registering the same user actor again */
      groupActor ! RequestUserCreation("group", "user1", probe.ref)
      val registered2 = probe.receiveMessage()

      registered1.user should === (registered2.user)
    }

    "list active users" in {
      val registeredProbe = createTestProbe[UserRegistered]()
      val groupActor = spawn(UserGroup("group"))

      groupActor ! RequestUserCreation("group", "user1", registeredProbe.ref)
      registeredProbe.receiveMessage()

      groupActor ! RequestUserCreation("group", "user2", registeredProbe.ref)
      registeredProbe.receiveMessage()

      val usersListProbe = createTestProbe[ReplyUserList]()
      groupActor ! RequestUserList(10L, "group", usersListProbe.ref)
      usersListProbe.expectMessage(ReplyUserList(10L, Set("user1", "user2")))
    }

    "list active users after one shuts down" in {
      val registeredProbe = createTestProbe[UserRegistered]()
      val groupActor = spawn(UserGroup("group"))

      groupActor ! RequestUserCreation("group", "user1", registeredProbe.ref)
      val registered1 = registeredProbe.receiveMessage()
      val userToShutDown = registered1.user

      groupActor ! RequestUserCreation("group", "user2", registeredProbe.ref)
      registeredProbe.receiveMessage()

      val usersListProbe = createTestProbe[ReplyUserList]()
      groupActor ! RequestUserList(10L, "group", usersListProbe.ref)
      usersListProbe.expectMessage(ReplyUserList(10L, Set("user1", "user2")))

      userToShutDown ! Passivate
      registeredProbe.expectTerminated(userToShutDown, registeredProbe.remainingOrDefault)

      /* using awaitAssert to retry because it might take longer for the group actor
      to notice the terminated user actor */
      registeredProbe.awaitAssert{
        groupActor ! RequestUserList(10L, "group", usersListProbe.ref)
        usersListProbe.expectMessage(ReplyUserList(10L, Set("user2")))
      }
    }

  }

}
