package AverageJoes.model.user

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}


/**
 * UserGroup supervises the user actors for one group. For example GymUsers or StaffUsers.
 * Groups provides other services too.
 * TODO: implement UserGroup services
 */

object UserGroup {
  def apply(groupID: String): Behavior[Command] = Behaviors.setup(ctx => new UserGroup(ctx, groupID))

  trait Command
  private final case class DeviceTerminated(device: ActorRef[User.Command], groupId: String, userId: String) extends Command
}

class UserGroup(ctx: ActorContext[UserGroup.Command], groupId: String)
  extends AbstractBehavior[UserGroup.Command](ctx) {

  import UserGroup._
  import UserManager._

  private var userIdToActor = Map.empty[String, ActorRef[User.Command]]

  //context.log.info("UserGroup {} started", groupId)

  override def onMessage(msg: UserGroup.Command): Behavior[UserGroup.Command] =
    msg match {
      case RequestUserList(requestId, gId, replyTo) =>
        if(gId == groupId) {
          replyTo ! ReplyUserList(requestId, userIdToActor.keySet)
          this
        }else Behaviors.unhandled
    }

}