package AverageJoes.model.user

import AverageJoes.model.user
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object UserManager {
  def apply(): Behavior[Command] = Behaviors.setup(ctx => new UserManager(ctx))

  trait Command

  final case class RequestUserList(requestId: Long, groupId: String, replyTo: ActorRef[ReplyUserList]) extends UserManager.Command with UserGroup.Command
  final case class ReplyUserList(requestId: Long, ids: Set[String])

}

class UserManager(ctx: ActorContext[UserManager.Command])
  extends AbstractBehavior[UserManager.Command](ctx) {

  import UserManager._

  var groupIdToActor = Map.empty[String, ActorRef[user.UserGroup.Command]]

  //context.log.info("UserManager started")

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case req @ RequestUserList(requestId, groupId, replyTo) =>
      groupIdToActor.get(groupId) match {
        case Some(ref) =>
          ref ! req
        case None =>
          replyTo ! ReplyUserList(requestId, Set.empty)
      }
      this

    /**
     * TODO: other services to be added
      */
  }
}