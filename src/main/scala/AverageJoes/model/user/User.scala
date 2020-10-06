package AverageJoes.model.user

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object User {
  def apply(groupId: String, deviceId: String): Behavior[Command] =
    Behaviors.setup(context => new User(context, groupId, deviceId))

  sealed trait Command
  final case class NotifiedByMachine(requestId: Long, replyTo: ActorRef[NotifyWristband]) extends Command
  final case class NotifyWristband(requestId: Long) extends Command

  final case class UserAlive(requestId: Long, replyTo: ActorRef[UserAliveSignal]) extends Command
  final case class UserAliveSignal(requestId: Long) extends Command

  case object Passivate extends Command

}

class User(context: ActorContext[User.Command], groupId: String, userId: String)
  extends AbstractBehavior[User.Command](context) {
  import User._

  println("User actor {"+groupId+"}-{"+userId+"} started")

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case NotifiedByMachine(id, replyTo) =>
        replyTo ! NotifyWristband(id)
        this

      case UserAlive(id, replyTo) =>
        replyTo ! UserAliveSignal(id)
        this

      case Passivate =>
        Behaviors.stopped
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      println("Device actor {"+groupId+"}-{"+userId+"} stopped")
      this
  }

}

