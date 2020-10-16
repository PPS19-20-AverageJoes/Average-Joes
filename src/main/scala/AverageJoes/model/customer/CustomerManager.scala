package AverageJoes.model.customer

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

/**
 * Customer Manager will handle the request for customer creation and will pass this request
 * to Customer Group. Other requests will be handled by Customer Manager.
 */

object CustomerManager {
  def apply(): Behavior[Command] = Behaviors.setup(ctx => new CustomerManager(ctx))

  sealed  trait Command

  final case class RequestCustomerCreation(groupId: String, customerId: String, replyTo: ActorRef[CustomerRegistered]) extends CustomerManager.Command with CustomerGroup.Command
  final case class RequestCustomerList(requestId: Long, groupId: String, replyTo: ActorRef[ReplyCustomerList]) extends CustomerManager.Command with CustomerGroup.Command
  final case class ReplyCustomerList(requestId: Long, ids: Set[String])

  final case class CustomerRegistered(customer: ActorRef[Customer.Command]) extends CustomerManager.Command with CustomerGroup.Command
  private final case class CustomerGroupTerminated(groupId: String) extends CustomerManager.Command
}

class CustomerManager(ctx: ActorContext[CustomerManager.Command])
  extends AbstractBehavior[CustomerManager.Command](ctx) {

  import CustomerManager._

  var groupIdToActor = Map.empty[String, ActorRef[CustomerGroup.Command]]

  println("UserManager started")

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case customerCreation @ RequestCustomerCreation(groupId,_, replyTo) =>
        groupIdToActor.get(groupId) match {
          case Some(ref) => ref ! customerCreation
          case None =>
            println("Creating customer group actor for {"+groupId+"}")
            val groupActor = context.spawn(CustomerGroup(groupId), "group-"+groupId)
            context.watchWith(groupActor, CustomerGroupTerminated(groupId))
            groupActor ! customerCreation
            groupIdToActor += groupId -> groupActor
        }
      this

    case customerList @ RequestCustomerList(requestId, groupId, replyTo) =>
      groupIdToActor.get(groupId) match {
        case Some(ref) =>
          ref ! customerList
        case None =>
          replyTo ! ReplyCustomerList(requestId, Set.empty)
      }
      this

    case CustomerGroupTerminated(groupId) =>
      println("Customer group actor for {"+groupId+"} has been terminated")
      groupIdToActor -= groupId
      this

    case _ => print("To be handled")
      this
  }


  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
     println("Customer manager stopped")
      this
  }
}