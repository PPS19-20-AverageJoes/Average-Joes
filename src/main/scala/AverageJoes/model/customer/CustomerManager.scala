package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.controller.GymController.GymController
import AverageJoes.model.customer.CustomerGroup.CustomerLogin
import AverageJoes.model.device.Device
import AverageJoes.model.machine.MachineActor
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

/**
 * Customer Manager will handle the request for customer creation and will pass this request
 * to Customer Group. Other requests will be handled by Customer Manager.
 */

object CustomerManager {
  def apply(): Behavior[Msg] = Behaviors.setup(ctx => new CustomerManager(ctx))

  sealed trait Msg extends LoggableMsg
  /** Receive Messages */
  final case class RequestCustomerCreation(customerId: String, replyTo: ActorRef[GymController], device: ActorRef[Device.Msg])
    extends CustomerManager.Msg with CustomerGroup.Msg

  final case class RequestCustomerLogin(replyTo: ActorRef[MachineActor])
    extends CustomerManager.Msg with CustomerGroup.Msg

  final case class RequestCustomerList(replyTo: ActorRef[GymController])
    extends CustomerManager.Msg with CustomerGroup.Msg

  /** Reply Messages */
  // private final case class CustomerList(customerActors: Set[ActorRef[CustomerActor.Msg]])

  //final case class CustomerRegistered(customer: ActorRef[CustomerActor.Msg]) extends CustomerManager.Msg with CustomerGroup.Msg

  //final case class CustomerLogged(customer: ActorRef[CustomerActor.Msg]) extends CustomerManager.Msg with CustomerGroup.Msg

  private final case class CustomerGroupTerminated(groupId: String) extends CustomerManager.Msg

}

class CustomerManager(ctx: ActorContext[CustomerManager.Msg])
  extends AbstractBehavior[CustomerManager.Msg](ctx) {

  import CustomerManager._

  /** Memorize all the different groups of customer. In this case, we will have only one group. */
  var customerGroupsActors = Map.empty[String, ActorRef[CustomerGroup.Msg]]
  val groupId = "customers"
  var deviceRef: ActorRef[Device]
  override def onMessage(msg: Msg): Behavior[Msg] = msg match {

    case customerCreation @ RequestCustomerCreation(_,_,_) =>
      customerGroupsActors.get(groupId) match {
          case Some(ref) =>
            deviceRef = deviceRef
            ref ! customerCreation
          case None =>
            deviceRef = deviceRef
            val groupActor = context.spawn(CustomerGroup(groupId), "group-"+groupId)
            context.watchWith(groupActor, CustomerGroupTerminated(groupId))
            groupActor ! customerCreation
            customerGroupsActors += groupId -> groupActor
        }
      this

    case RequestCustomerLogin(machine) =>
      customerGroupsActors.get(groupId) match {
        case Some(ref) =>
          ref ! CustomerLogin(machine, deviceRef)
        case None =>

      }
      this

    case customerList @ RequestCustomerList(replyTo) =>
      customerGroupsActors.get(groupId) match {
        case Some(ref) =>
          ref ! customerList
        case None =>
          replyTo ! CustomerList(Set.empty)
      }
      this


    case CustomerGroupTerminated(groupId) =>
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