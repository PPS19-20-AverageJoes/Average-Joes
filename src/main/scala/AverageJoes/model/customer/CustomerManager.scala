package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

/**
 * Customer Manager will handle the request for customer creation and will pass this request
 * to Customer Group. Other requests will be handled by Customer Manager.
 */

object CustomerManager {
  def apply(): Behavior[Msg] = Behaviors.setup(ctx => new CustomerManager(ctx))

  sealed trait Msg extends LoggableMsg
  final case class RequestCustomerLogin(customerId: String, replyTo: ActorRef[CustomerRegistered], device: ActorRef[CustomerRegistered])
    extends CustomerManager.Msg with CustomerGroup.Msg

  final case class RequestCustomerList(replyTo: ActorRef[ReplyCustomerList])
    extends CustomerManager.Msg with CustomerGroup.Msg

  final case class ReplyCustomerList(customerActors: Set[ActorRef[CustomerActor.Msg]])

  final case class CustomerRegistered(customer: ActorRef[CustomerActor.Msg])
    extends CustomerManager.Msg with CustomerGroup.Msg

  private final case class CustomerGroupTerminated(groupId: String) extends CustomerManager.Msg

}

class CustomerManager(ctx: ActorContext[CustomerManager.Msg])
  extends AbstractBehavior[CustomerManager.Msg](ctx) {

  import CustomerManager._

  /** Memorize all the different groups of customer. In this case, we will have only one group. */
  var customerGroupsActors = Map.empty[String, ActorRef[CustomerGroup.Msg]]
  val groupId = "customers"

  override def onMessage(msg: Msg): Behavior[Msg] = msg match {

    case customerCreation @ RequestCustomerLogin(_,_,_) =>
      customerGroupsActors.get(groupId) match {
          case Some(ref) => ref ! customerCreation
          case None =>
            //println("Creating customer group actor for {"+groupId+"}")
            val groupActor = context.spawn(CustomerGroup(groupId), "group-"+groupId)
            context.watchWith(groupActor, CustomerGroupTerminated(groupId))
            groupActor ! customerCreation
            customerGroupsActors += groupId -> groupActor
        }
      this

    case customerList @ RequestCustomerList(replyTo) =>
      customerGroupsActors.get(groupId) match {
        case Some(ref) =>
          ref ! customerList
        case None =>
          replyTo ! ReplyCustomerList(Set.empty)
      }
      this


    case CustomerGroupTerminated(groupId) =>
      //println("Customer group actor for {"+groupId+"} has been terminated")
      customerGroupsActors -= groupId
      this

    case _ =>
      //print("To be handled")
      this
  }


  override def onSignal: PartialFunction[Signal, Behavior[Msg]] = {
    case PostStop =>
     //println("Customer manager stopped")
     this
  }

}