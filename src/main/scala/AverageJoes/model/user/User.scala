package AverageJoes.model.user

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object User {
  def apply(groupId: String, deviceId: String): Behavior[Command] =
    Behaviors.setup(context => new User(context, groupId, deviceId))

  sealed trait Command
  final case class NotifiedByMachine(requestId: Long, replyTo: ActorRef[NotifyWristband]) extends Command
  final case class NotifyWristband(requestId: Long) extends Command

}

class User(context: ActorContext[User.Command], groupId: String, userId: String)
  extends AbstractBehavior[User.Command](context) {
  import User._

  //context.log.info("User actor {}-{} started", groupId, userId)

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case NotifiedByMachine(id, replyTo) =>
        replyTo ! NotifyWristband(id)
        this
      case _ => println("This message was not tracked")
        this
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      //context.log.info("Device actor {}-{} stopped", groupId, userId)
      this
  }

}

