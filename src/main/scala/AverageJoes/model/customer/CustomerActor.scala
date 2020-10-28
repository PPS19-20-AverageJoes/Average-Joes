package AverageJoes.model.customer

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object CustomerActor {
  def apply(groupId: String, deviceId: String): Behavior[Command] =
    Behaviors.setup(context => new CustomerActor(context, groupId, deviceId))

  sealed trait Command
  final case class NotifiedByMachine(requestId: Long, replyTo: ActorRef[NotifyWristband]) extends Command
  final case class NotifyWristband(requestId: Long) extends Command

  final case class CustomerAlive(requestId: Long, replyTo: ActorRef[CustomerAliveSignal]) extends Command
  final case class CustomerAliveSignal(requestId: Long) extends Command

  case object Passivate extends Command

}

class CustomerActor(context: ActorContext[CustomerActor.Command], groupId: String, customerId: String)
  extends AbstractBehavior[CustomerActor.Command](context) {
  import CustomerActor._

  /**
   * wristband login -> create customer actor -> device id : id wristband
   *
   * find Customer with id device : Entity [Customer]
   *
   * Instantiate Customer
   */

  /**
   * chiede al controller una prenotazione
   */

  println("Customer actor {"+groupId+"}-{"+customerId+"} started")

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case NotifiedByMachine(id, replyTo) =>
        replyTo ! NotifyWristband(id)
        this

      case CustomerAlive(id, replyTo) =>
        replyTo ! CustomerAliveSignal(id)
        this

      case Passivate =>
        Behaviors.stopped
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      println("Customer actor {"+groupId+"}-{"+customerId+"} stopped")
      this
  }

}

