package AverageJoes.model.customer

import AverageJoes.model.customer.CustomerManager.{CustomerRegistered, RequestCustomerCreation}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

/**
 * Group actor will handle the requests that will be passed by User Manager Actor *
 * Groups provides other services too.
 * TODO: implement UserGroup services
 */

object CustomerGroup {
  def apply(groupID: String): Behavior[Command] = Behaviors.setup(ctx => new CustomerGroup(ctx, groupID))

  trait Command
  private final case class CustomerTerminated(device: ActorRef[Customer.Command], groupId: String, customerId: String) extends Command
}

class CustomerGroup(ctx: ActorContext[CustomerGroup.Command], groupId: String)
  extends AbstractBehavior[CustomerGroup.Command](ctx) {

  import CustomerGroup.{Command, CustomerTerminated}
  import CustomerManager._

  private var customerIdToActor = Map.empty[String, ActorRef[Customer.Command]]

  println("UserGroup {"+groupId+"} started")

  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case createUserMsg @ RequestCustomerCreation(`groupId`, customerId, replyTo) =>  // `groupId` is used to check if group param is the same
        customerIdToActor.get(customerId) match {
          case Some(userActor) => replyTo ! CustomerRegistered(userActor)
          case None =>
              println("Creating customer actor for {"+createUserMsg.customerId+"}")
              val customerActor = context.spawn(Customer(groupId, customerId), s"user-$customerId")
              context.watchWith(customerActor, CustomerTerminated(customerActor, groupId, customerId))
              customerIdToActor += customerId -> customerActor
              replyTo ! CustomerRegistered(customerActor)
        }
        this

      case RequestCustomerCreation(gId,_,_) =>
        println("Ignoring UserCreation request for {"+gId+"}. This actor is responsible for {"+groupId+"}.")
        this

      case RequestCustomerList(requestId, gId, replyTo) =>
        if(gId == groupId) {
          replyTo ! ReplyCustomerList(requestId, customerIdToActor.keySet)
          this
        }
        else Behaviors.unhandled

      case CustomerTerminated(_,_, userId) =>
          println("Customer actor for {"+userId+"} has been terminated")
          customerIdToActor -= userId
          this
    }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      println("Customer Group {} stopped", groupId)
      this
  }

}