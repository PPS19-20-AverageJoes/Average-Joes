package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.controller.GymController.Msg.{CustomerList, CustomerRegistered}
import AverageJoes.model.customer.CustomerActor.CustomerTrainingProgram
import AverageJoes.model.customer.CustomerGroup.{CustomerLogin, UploadCustomerTrainingProgram}
import AverageJoes.model.device.Device
import AverageJoes.model.device.Device.Msg.CustomerLogged
import AverageJoes.model.fitness.TrainingProgram
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.CustomerLogging
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

object CustomerGroup {
  def apply(groupID: String, manager: ActorRef[CustomerManager.Msg]): Behavior[Msg] = Behaviors.setup(ctx => new CustomerGroup(ctx, manager, groupID))

  trait Msg extends LoggableMsg

  final case class CustomerLogin(customerId: String, machine: ActorRef[MachineActor.Msg], device: ActorRef[Device.Msg]) extends Msg
  private final case class UploadCustomerTrainingProgram(customerId: String, customer: ActorRef[CustomerActor.Msg]) extends Msg
  private final case class CustomerTerminated(device: ActorRef[CustomerActor.Msg], groupId: String, customerId: String) extends Msg
}


class CustomerGroup(ctx: ActorContext[CustomerGroup.Msg],
                    manager: ActorRef[CustomerManager.Msg],
                    groupId: String) extends AbstractBehavior[CustomerGroup.Msg](ctx) {
  import CustomerGroup.{CustomerTerminated, Msg}
  import CustomerManager._

  private var customerIdToActor = Map.empty[String, ActorRef[CustomerActor.Msg]]

  override def onMessage(msg: Msg): Behavior[Msg] = msg match {

    case RequestCustomerCreation(customerId, replyTo, _) =>
      customerIdToActor.get(customerId) match {

        case Some(customerActor) =>
          replyTo ! CustomerRegistered(customerId, customerActor)
          context.self ! UploadCustomerTrainingProgram(customerId, customerActor)
        case None =>
          if(isCustomerOnStorage(customerId)) {
            val customerActor = context.spawn(CustomerActor(manager, customerId), s"customer-$customerId")
            context.watchWith(customerActor, CustomerTerminated(customerActor, groupId, customerId))
            customerIdToActor += customerId -> customerActor
            replyTo ! CustomerRegistered(customerId, customerActor)
            context.self ! UploadCustomerTrainingProgram(customerId, customerActor)
          }
          else{
            /** Do something because customerId is not present on storage */
          }
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

    case RequestCustomerList(replyTo) =>
      replyTo ! CustomerList(customerIdToActor.values.toSet)
      this

    case UploadCustomerTrainingProgram(customerId, customer: ActorRef[CustomerActor.Msg]) =>
      customer ! CustomerTrainingProgram(trainingProgramOf(customerId) )
    this

    case CustomerTerminated(_, _, customerId) =>
      customerIdToActor -= customerId
      this
  }

  private def isCustomerOnStorage(customerId: String): Boolean = {
    /** TODO: Search for customer on database */
    true
  }

  private def trainingProgramOf(customerId: String): TrainingProgram = {
    /** TODO: Search for customer training program on database */
    null
  }

}