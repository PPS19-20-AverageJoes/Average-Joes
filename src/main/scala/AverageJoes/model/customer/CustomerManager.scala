package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.controller.GymController
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.machine.MachineActor
import AverageJoes.common.MachineTypes.MachineType
import AverageJoes.controller.GymController.Msg.MachinesToBookmark
import AverageJoes.model.customer.CustomerGroup.CustomerLogin
import AverageJoes.model.customer.CustomerManager.{MachineListOf, Msg, RequestCustomerCreation, RequestCustomerList, RequestCustomerLogin}
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
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
    extends Msg with CustomerGroup.Msg

  final case class RequestCustomerLogin(customerId: String, machineLabel: MachineLabel, machine: ActorRef[MachineActor.Msg], physicalMachine: ActorRef[PhysicalMachine.Msg])
    extends Msg

  final case class RequestCustomerList(controller: ActorRef[GymController.Msg]) extends Msg with CustomerGroup.Msg

  final case class MachineListOf(machineType: MachineType, customer: ActorRef[CustomerActor.Msg]) extends Msg

  final case class MachineList(machines: Set[ActorRef[MachineActor.Msg]]) extends Msg with CustomerActor.Msg

}


class CustomerManager(ctx: ActorContext[CustomerManager.Msg]) extends AbstractBehavior[CustomerManager.Msg](ctx) {

  /**
   * TODO: to optional ref
   */
  var controllerRef: ActorRef[GymController.Msg] = _
  var deviceRef: ActorRef[Device.Msg] = _
  val groupId = "customers"
  val customerGroup: ActorRef[CustomerGroup.Msg] = context.spawn(CustomerGroup(groupId, context.self), "group-"+groupId)


  override def onMessage(msg: Msg): Behavior[Msg] = msg match {

    case customerCreation @ RequestCustomerCreation(_, controller, device) =>
      controllerRef = controller
      deviceRef = device
      customerGroup ! customerCreation
      Behaviors.same

    case RequestCustomerLogin(customerId, machineLabel, machine, phMachine) =>
      customerGroup ! CustomerLogin(customerId, machineLabel, machine, phMachine, deviceRef)
      Behaviors.same

    case customerList @ RequestCustomerList(_) =>
      customerGroup ! customerList
      Behaviors.same


    case MachineListOf(machineType, customer) =>
      println("Customer requesting machines list")
      controllerRef ! MachinesToBookmark(machineType, customer)
      Behaviors.same

  }

}