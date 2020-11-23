package averageJoes.model.customer

import averageJoes.common.LoggableMsg
import averageJoes.controller.GymController
import averageJoes.model.hardware.{Device, PhysicalMachine}
import averageJoes.model.machine.MachineActor
import averageJoes.model.workout.MachineTypes.MachineType
import averageJoes.controller.GymController.Msg.MachinesToBookmark
import averageJoes.model.customer.CustomerGroup.CustomerLogin
import averageJoes.model.customer.CustomerManager.{MachineListOf, Msg, RequestCustomerCreation, RequestCustomerList, RequestCustomerLogin}
import averageJoes.model.hardware.PhysicalMachine.MachineLabel
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

/**
 * Customer Manager will handle the request for customer creation and will pass this request
 * to Customer Group. It is the entry point to create a customer and authenticate it on
 * a smart machine. It will keep track of group actors, in this case only one, the customer's group.
 */
object CustomerManager {
  def apply(): Behavior[Msg] = Behaviors.setup(ctx => new CustomerManager(ctx))

  sealed trait Msg extends LoggableMsg

  /** Receive Messages */
  final case class RequestCustomerCreation(customerId: String,
                                           controller: ActorRef[GymController.Msg],
                                           device: ActorRef[Device.Msg]) extends Msg with CustomerGroup.Msg

  final case class RequestCustomerLogin(customerId: String,
                                        machineLabel: MachineLabel,
                                        machineType: MachineType,
                                        machine: ActorRef[MachineActor.Msg],
                                        physicalMachine: ActorRef[PhysicalMachine.Msg]) extends Msg

  final case class RequestCustomerList(controller: ActorRef[GymController.Msg]) extends Msg with CustomerGroup.Msg

  final case class MachineListOf(machineType: MachineType, customer: ActorRef[CustomerActor.Msg]) extends Msg



}


class CustomerManager(ctx: ActorContext[CustomerManager.Msg]) extends AbstractBehavior[CustomerManager.Msg](ctx) {

  var controller: Option[ActorRef[GymController.Msg]] = Option.empty[ActorRef[GymController.Msg]]
  val groupId = "customers"
  val customerGroup: ActorRef[CustomerGroup.Msg] = context.spawn(CustomerGroup(groupId, context.self), "group-"+groupId)


  override def onMessage(msg: Msg): Behavior[Msg] = msg match {

    case customerCreation @ RequestCustomerCreation(_, gymController, _) =>
      controller = Option.apply(gymController)
      customerGroup ! customerCreation
      Behaviors.same

    case RequestCustomerLogin(customerId, machineLabel, machineType, machine, phMachine) =>
      customerGroup ! CustomerLogin(customerId, machineLabel, machineType, machine, phMachine)
      Behaviors.same

    case customerList @ RequestCustomerList(_) =>
      customerGroup ! customerList
      Behaviors.same

    case MachineListOf(machineType, customer) =>
      if(controller.isDefined) controller.get ! MachinesToBookmark(machineType, customer)
      Behaviors.same

  }

}