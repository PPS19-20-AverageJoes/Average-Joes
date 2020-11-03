package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

/**
 * Group actor will handle the requests that will be passed by Customer Manager Actor
 */

object CustomerGroup {
  def apply(groupID: String): Behavior[Msg] = Behaviors.setup(ctx => new CustomerGroup(ctx, groupID))

  trait Msg extends LoggableMsg
  private final case class CustomerTerminated(device: ActorRef[CustomerActor.Msg], groupId: String, customerId: String) extends Msg
}

class CustomerGroup(ctx: ActorContext[CustomerGroup.Msg], groupId: String)
  extends AbstractBehavior[CustomerGroup.Msg](ctx) {

  import CustomerGroup.{Msg, CustomerTerminated}
  import CustomerManager._

  private var customerIdToActor = Map.empty[String, ActorRef[CustomerActor.Msg]]

  //println("UserGroup {"+groupId+"} started")

  override def onMessage(msg: Msg): Behavior[Msg] = msg match {

      case RequestCustomerLogin(customerId, replyTo, device) =>
        customerIdToActor.get(customerId) match {
          case Some(userActor) => replyTo ! CustomerRegistered(userActor)
          case None =>
              //println("Creating customer actor for {"+createUserMsg.customerId+"}")
              val customerActor = context.spawn(CustomerActor(groupId, customerId), s"user-$customerId")
              context.watchWith(customerActor, CustomerTerminated(customerActor, groupId, customerId))
              customerIdToActor += customerId -> customerActor
              replyTo ! CustomerRegistered(customerActor)
              device ! CustomerRegistered(customerActor)
        }
        this

      case RequestCustomerLogin(_,_,_) =>
        //println("Ignoring UserCreation request. This actor is responsible for {"+groupId+"}.")
        this

      case RequestCustomerList(replyTo) =>
          replyTo ! ReplyCustomerList(customerIdToActor.values.toSet)
          this


      case CustomerTerminated(_,_, userId) =>
          //println("Customer actor for {"+userId+"} has been terminated")
          customerIdToActor -= userId
          this
    }

  override def onSignal: PartialFunction[Signal, Behavior[Msg]] = {
    case PostStop =>
      //println("Customer Group {} stopped", groupId)
      this
  }

}