package AverageJoes.model.user

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}


/**
 * Group actor will handle the requests that will be passed by User Manager Actor *
 * Groups provides other services too.
 * TODO: implement UserGroup services
 */

object UserGroup {
  def apply(groupID: String): Behavior[Command] = Behaviors.setup(ctx => new UserGroup(ctx, groupID))

  trait Command
  private final case class UserTerminated(device: ActorRef[User.Command], groupId: String, userId: String) extends Command
}

class UserGroup(ctx: ActorContext[UserGroup.Command], groupId: String)
  extends AbstractBehavior[UserGroup.Command](ctx) {

  import UserGroup.{Command, UserTerminated}
  import UserManager._

  private var userIdToActor = Map.empty[String, ActorRef[User.Command]]

  println("UserGroup {"+groupId+"} started")

  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case createUserMsg @ RequestUserCreation(`groupId`, userId, replyTo) =>  // `groupId` is used to check if group param is the same
        userIdToActor.get(userId) match {
          case Some(userActor) => replyTo ! UserRegistered(userActor)
          case None =>
              println("Creating user actor for {"+createUserMsg.userId+"}")
              val userActor = context.spawn(User(groupId, userId), s"user-$userId")
              context.watchWith(userActor, UserTerminated(userActor, groupId, userId))
              userIdToActor += userId -> userActor
              replyTo ! UserRegistered(userActor)
        }
        this

      case RequestUserCreation(gId,_,_) =>
        println("Ignoring UserCreation request for {"+gId+"}. This actor is responsible for {"+groupId+"}.")
        this

      case RequestUserList(requestId, gId, replyTo) =>
        if(gId == groupId) {
          replyTo ! ReplyUserList(requestId, userIdToActor.keySet)
          this
        }
        else Behaviors.unhandled

      case UserTerminated(_,_, userId) =>
          println("User actor for {"+userId+"} has been terminated")
          userIdToActor -= userId
          this
    }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      println("UserGroup {} stopped", groupId)
      this
  }

}