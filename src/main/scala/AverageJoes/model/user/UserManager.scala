package AverageJoes.model.user

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}


object UserManager {
  def apply(): Behavior[Command] = Behaviors.setup(ctx => new UserManager(ctx))

  sealed  trait Command

  final case class RequestUserCreation(groupId: String, userId: String, replyTo: ActorRef[UserRegistered]) extends UserManager.Command with UserGroup.Command
  final case class RequestUserList(requestId: Long, groupId: String, replyTo: ActorRef[ReplyUserList]) extends UserManager.Command with UserGroup.Command
  final case class ReplyUserList(requestId: Long, ids: Set[String])

  final case class UserRegistered(user: ActorRef[User.Command]) extends UserManager.Command with UserGroup.Command
  private final case class UserGroupTerminated(groupId: String) extends UserManager.Command
}

class UserManager(ctx: ActorContext[UserManager.Command])
  extends AbstractBehavior[UserManager.Command](ctx) {

  import UserManager._

  var groupIdToActor = Map.empty[String, ActorRef[UserGroup.Command]]

  println("UserManager started")

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case userCreation @ RequestUserCreation(groupId,_, replyTo) =>
        groupIdToActor.get(groupId) match {
          case Some(ref) => ref ! userCreation
          case None =>
            println("Creating device group actor for {"+groupId+"}")
            val groupActor = context.spawn(UserGroup(groupId), "group-"+groupId)
            context.watchWith(groupActor, UserGroupTerminated(groupId))
            groupActor ! userCreation
            groupIdToActor += groupId -> groupActor
        }
      this

    case userList @ RequestUserList(requestId, groupId, replyTo) =>
      groupIdToActor.get(groupId) match {
        case Some(ref) =>
          ref ! userList
        case None =>
          replyTo ! ReplyUserList(requestId, Set.empty)
      }
      this

    case UserGroupTerminated(groupId) =>
      println("User group actor for {"+groupId+"} has been terminated")
      groupIdToActor -= groupId
      this

    case _ => print("To be handled")
      this
  }


  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
     println("User manager stopped")
      this
  }
}