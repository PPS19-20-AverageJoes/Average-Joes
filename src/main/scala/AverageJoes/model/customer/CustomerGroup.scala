package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.controller.GymController.Msg.CustomerRegistered
import AverageJoes.model.customer.CustomerActor.{CustomerMachineLogin, CustomerTrainingProgram, NextMachineBooking}
import AverageJoes.model.customer.CustomerGroup.{CustomerLogin, CustomerReady, UploadCustomerTrainingProgram}
import AverageJoes.model.fitness.MachineExecution.MACHINE_EQUIPMENT.RunningMachine
import AverageJoes.model.fitness.{Exercise, TrainingProgram}
import AverageJoes.model.hardware.Device
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.CustomerLogging
import AverageJoes.utils.DateUtils.stringToDate
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}


object CustomerGroup {
  def apply(groupID: String, manager: ActorRef[CustomerManager.Msg]): Behavior[Msg] = Behaviors.setup(ctx => new CustomerGroup(ctx, manager, groupID))

  trait Msg extends LoggableMsg

  final case class CustomerLogin(customerId: String, machineLabel: MachineLabel, machine: ActorRef[MachineActor.Msg], device: ActorRef[Device.Msg]) extends Msg
  private final case class UploadCustomerTrainingProgram(customerId: String, customer: ActorRef[CustomerActor.Msg]) extends Msg
  private final case class CustomerTerminated(device: ActorRef[CustomerActor.Msg], groupId: String, customerId: String) extends Msg

  final case class CustomerReady(ex:Exercise, customer:ActorRef[CustomerActor.Msg]) extends Msg

}


class CustomerGroup(ctx: ActorContext[CustomerGroup.Msg],
                    manager: ActorRef[CustomerManager.Msg],
                    groupId: String) extends AbstractBehavior[CustomerGroup.Msg](ctx) {
  import CustomerGroup.{CustomerTerminated, Msg}
  import CustomerManager._

  private var customerIdToActor = Map.empty[String, ActorRef[CustomerActor.Msg]]

  override def onMessage(msg: Msg): Behavior[Msg] = msg match {

    case RequestCustomerCreation(customerId, controller, _) =>
      customerIdToActor.get(customerId) match {

        case Some(customerActor) =>
          controller ! CustomerRegistered(customerId, customerActor)
          context.self ! UploadCustomerTrainingProgram(customerId, customerActor)

        case None =>
          if(isCustomerOnStorage(customerId)) {
            val customerActor = context.spawn(CustomerActor(manager, customerId), s"customer-$customerId")
            context.watchWith(customerActor, CustomerTerminated(customerActor, groupId, customerId))
            customerIdToActor += customerId -> customerActor

            controller !  CustomerRegistered(customerId, customerActor)
            context.self ! UploadCustomerTrainingProgram(customerId, customerActor)
          }
          else{
            /** Do something because customerId is not present on storage */
          }
      }
      this

    case CustomerLogin(customerId, machineLabel, machine, device) =>
      customerIdToActor.get(customerId) match {
        case Some(customerActor) =>
          customerActor ! CustomerMachineLogin(machineLabel, machine, device)
        case None =>
          machine ! CustomerLogging(customerId, null, isLogged = false)
      }
      this

    case CustomerReady(ex, customer) =>
      customer ! NextMachineBooking(ex)
      this

    case RequestCustomerList(replyTo) =>
      //replyTo ! GymController.Msg.CustomerList(customerIdToActor.values.toSet)
      this


    case UploadCustomerTrainingProgram(customerId, customer: ActorRef[CustomerActor.Msg]) =>
      customer ! CustomerTrainingProgram(trainingProgramOf(customerId), context.self)
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
    val c1: Customer =  Customer("customer-1", "sokol", "guri", stringToDate("20/05/2020"))
     TrainingProgram(c1)
        .addExercise(Exercise(RunningMachine(speed = 10.0, incline = 20.0, timer = 30)))
       /* .addExercise(Exercise(RunningMachine(speed = 11.0, incline = 21.0, timer = 30)))
        .addExercise(Exercise(RunningMachine(speed = 12.0, incline = 22.0, timer = 30)))
        .addExercise(Exercise(RunningMachine(speed = 13.0, incline = 23.0, timer = 30))) */

  }

}