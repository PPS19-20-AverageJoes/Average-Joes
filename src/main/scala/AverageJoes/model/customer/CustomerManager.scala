package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.controller.GymController
import AverageJoes.model.customer.CustomerGroup.CustomerLogin
import AverageJoes.model.device.Device
import AverageJoes.model.machine.MachineActor
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

/**
 * Customer Manager will handle the request for customer creation and will pass this request
 * to Customer Group. Other requests will be handled by Customer Manager.
 */

object CustomerManager {
  def apply(): Behavior[Msg] = Behaviors.setup(ctx => new CustomerManager(ctx))

  sealed trait Msg extends LoggableMsg
  /** Receive Messages */
  final case class RequestCustomerCreation(customerId: String, controller: ActorRef[GymController.Msg], device: ActorRef[Device.Msg])
    extends CustomerManager.Msg with CustomerGroup.Msg

  final case class RequestCustomerLogin(customerId: String, machine: ActorRef[MachineActor.Msg])
    extends CustomerManager.Msg

  final case class RequestCustomerList(controller: ActorRef[GymController.Msg])
    extends CustomerManager.Msg with CustomerGroup.Msg
}

class CustomerManager(ctx: ActorContext[CustomerManager.Msg])
  extends AbstractBehavior[CustomerManager.Msg](ctx) {

  import CustomerManager._

  val groupId = "customers"
  var customerGroup: ActorRef[CustomerGroup.Msg] = context.spawn(CustomerGroup(groupId), "group-"+groupId)

  var deviceRef: ActorRef[Device.Msg] = _

  override def onMessage(msg: Msg): Behavior[Msg] = msg match {

    case customerCreation @ RequestCustomerCreation(_,_,device) =>
        deviceRef = device
        customerGroup ! customerCreation
      this

    case RequestCustomerLogin(customerId, machine) =>
      customerGroup ! CustomerLogin(customerId, machine, deviceRef)
      this

    case customerList @ RequestCustomerList(_) =>
      customerGroup ! customerList
      this
  }

}