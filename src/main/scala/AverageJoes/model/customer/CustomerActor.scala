package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}


/**
 * wristband login -> create customer actor -> device id : id wristband
 *
 * find Customer with id device : Entity [Customer]
 *
 * Instantiate Customer
 *
 * chiede al controller una prenotazione
 */


object CustomerActor {
  def apply(groupId: String, deviceId: String): Behavior[Msg] =
    Behaviors.setup(context => new CustomerActor(context, groupId, deviceId))

  sealed trait Msg extends LoggableMsg

  final case class CustomerAlive(requestId: Long, replyTo: ActorRef[CustomerAliveSignal]) extends Msg
  final case class CustomerAliveSignal(requestId: Long) extends Msg

  case object Passivate extends Msg

}

class CustomerActor(context: ActorContext[CustomerActor.Msg], groupId: String, customerId: String)
  extends AbstractBehavior[CustomerActor.Msg](context) {
  import CustomerActor._


  override def onMessage(msg: Msg): Behavior[Msg] = msg match {
      case CustomerAlive(id, replyTo) =>
        replyTo ! CustomerAliveSignal(id)
        this

      case Passivate =>
        Behaviors.stopped
    }
}

