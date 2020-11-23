package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.model.workout.MachineTypes.MachineType
import AverageJoes.common.database._
import AverageJoes.common.database.table.Customer
import AverageJoes.controller.GymController.Msg.CustomerRegistered
import AverageJoes.model.customer.CustomerActor.{CustomerMachineLogin, CustomerTrainingProgram}
import AverageJoes.model.customer.CustomerGroup.CustomerLogin
import AverageJoes.model.fitness.{Exercise, TrainingProgram}
import AverageJoes.model.hardware.PhysicalMachine
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.CustomerLogging
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.SortedSet

/**
 * CustomerGroup actor will handle all the request of CustomerManager. It will instantiate a CustomerActor
 * and will forward to CustomerActor the logging request, if it fulfills the necessary conditions.
 */
object CustomerGroup {
  def apply(groupID: String, manager: ActorRef[CustomerManager.Msg]): Behavior[Msg] = Behaviors.setup(ctx => new CustomerGroup(ctx, manager, groupID))

  trait Msg extends LoggableMsg
  final case class CustomerLogin(customerId: String,
                                 machineLabel: MachineLabel,
                                 machineType: MachineType,
                                 machine: ActorRef[MachineActor.Msg],
                                 phMachine: ActorRef[PhysicalMachine.Msg]) extends Msg
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

    /**
     * On a customer actor creation request, it will check if the customer actor already exists,
     * otherwise it will query the database to check if the customer id is correct and will
     * instantiate a new customer actor.
     */
    case RequestCustomerCreation(customerId, controller, device) =>
      customerIdToActor.get(customerId) match {

        case Some(customerActor) =>
          controller ! CustomerRegistered(customerId, customerActor)

        case None =>
          if(isCustomerOnStorage(customerId)) {
            val customerActor = context.spawn(CustomerActor(manager, customerId, device), s"customer-$customerId")
            customerIdToActor += customerId -> customerActor

            controller !  CustomerRegistered(customerId, customerActor)
            customerActor ! CustomerTrainingProgram(trainingProgramOf(customerId), context.self)
          }
      }
      this

    /**
     * On a login request it will control if the customer actor was already instantiated and will
     * forward the message to the respective customer actor.
     */
    case CustomerLogin(customerId, machineLabel, machineType, machine, phMachine) =>
      customerIdToActor.get(customerId) match {
        case Some(customerActor) =>
          customerActor ! CustomerMachineLogin(machineLabel, machineType, phMachine, machine)
        case None =>
          machine ! CustomerLogging(customerId, null,  Option.empty, isLogged = false)
      }
      this

    case CustomerTerminated(_, _, customerId) =>
      customerIdToActor -= customerId
      this

  }

  private def isCustomerOnStorage(customerId: String): Boolean = Customer.customerStorage.get(customerId).isDefined

  private def customerOf(customerId: String): Customer = {
    import  AverageJoes.common.database.Customer

    if ( !isCustomerOnStorage(customerId) ) throw  NoCustomerFoundException()
    else Customer.customerStorage.get(customerId).get
  }

  /** Extracting the exercises from workout storage and creating a training program
   * of the customer with an ordered set of exercises. */
  private def trainingProgramOf(customerId: String): TrainingProgram = {
    import AverageJoes.model.fitness.ImplicitExercise.Converters._
    import AverageJoes.model.fitness.ImplicitExercise.Ordering._

    val workoutSet: SortedSet[Exercise] = collection.SortedSet(Workout.workoutStorage
                  .getWorkoutForCustomer(customerId)
                  .map(w => Exercise(w)): _*) // using implicit Ordering[Exercise]

    if (workoutSet.isEmpty) throw new NoExercisesFoundException
    else TrainingProgram(customerOf(customerId)) (workoutSet)
  }
}


case class NoExercisesFoundException() extends NoSuchElementException
case class NoCustomerFoundException() extends NoSuchElementException
