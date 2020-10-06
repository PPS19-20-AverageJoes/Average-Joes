package AverageJoes.model.user

import AverageJoes.model.user.UserManager.{Command, RequestUserCreation}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object UserObjectTester {

  def apply(): Behavior[Command] = Behaviors.setup[Command](context => {

    val userManager: ActorRef[Command] = context.spawn(UserManager(), "user-manager")

    userManager ! RequestUserCreation("group1", "user1", userManager)


    UserManager()
  });
}