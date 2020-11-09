package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.controller.GymController.Msg.{CustomerList, CustomerRegistered}
import AverageJoes.model.customer.CustomerGroup.CustomerLogin
import AverageJoes.model.device.Device
import AverageJoes.model.device.Device.Msg.CustomerLogged
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.CustomerLogging
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

/**
 * Group actor will handle the requests that will be passed by Customer Manager Actor
 */

object CustomerGroup {
  def apply(groupID: String): Behavior[Msg] = Behaviors.setup(ctx => new CustomerGroup(ctx, groupID))

  trait Msg extends LoggableMsg
  final case class CustomerLogin(customerId: String, machine: ActorRef[MachineActor.Msg], device: ActorRef[Device.Msg]) extends Msg

  private final case class CustomerTerminated(device: ActorRef[CustomerActor.Msg], groupId: String, customerId: String) extends Msg
}

class CustomerGroup(ctx: ActorContext[CustomerGroup.Msg], groupId: String)
  extends AbstractBehavior[CustomerGroup.Msg](ctx) {

  import CustomerGroup.{CustomerTerminated, Msg}
  import CustomerManager._

  private var customerIdToActor = Map.empty[String, ActorRef[CustomerActor.Msg]]

  override def onMessage(msg: Msg): Behavior[Msg] = msg match {

    case RequestCustomerCreation(customerId, replyTo, _) =>
      customerIdToActor.get(customerId) match {
        case Some(userActor) => replyTo ! CustomerRegistered(userActor)
        case None =>
          val customerActor = context.spawn(CustomerActor(groupId, customerId), s"user-$customerId")
          context.watchWith(customerActor, CustomerTerminated(customerActor, groupId, customerId))
          customerIdToActor += customerId -> customerActor
          replyTo ! CustomerRegistered(customerActor)
      }
      this

    case CustomerLogin(customerId, machine, device) =>
      customerIdToActor.get(customerId) match {
        case Some(customerActor) =>
          /** Check if can log in and than notify machine and device */
          machine ! CustomerLogging(customerId, isLogged = true)
          device ! CustomerLogged(customerActor)
        case None =>
          machine ! CustomerLogging(customerId, isLogged = false)
      }
      this

    case RequestCustomerCreation(_, _, _) =>
      this

    case RequestCustomerList(replyTo) =>
      replyTo ! CustomerList(customerIdToActor.values.toSet)
      this


    case CustomerTerminated(_, _, userId) =>
      customerIdToActor -= userId
      this
  }
}