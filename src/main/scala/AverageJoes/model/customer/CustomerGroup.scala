package AverageJoes.model.customer

import java.util

import AverageJoes.common.LoggableMsg
import AverageJoes.common.MachineTypes.MachineType
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



object CustomerGroup {
  def apply(groupID: String, manager: ActorRef[CustomerManager.Msg]): Behavior[Msg] = Behaviors.setup(ctx => new CustomerGroup(ctx, manager, groupID))

  trait Msg extends LoggableMsg

  final case class CustomerLogin(customerId: String, machineLabel: MachineLabel, machineType: MachineType,  machine: ActorRef[MachineActor.Msg], phMachine: ActorRef[PhysicalMachine.Msg]) extends Msg
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

    case RequestCustomerCreation(customerId, controller, device) =>
      customerIdToActor.get(customerId) match {

        case Some(customerActor) =>
          controller ! CustomerRegistered(customerId, customerActor)
          //customerActor ! CustomerTrainingProgram(trainingProgramOf(customerId), context.self)

        case None =>
          if(isCustomerOnStorage(customerId)) {
            val customerActor = context.spawn(CustomerActor(manager, customerId, device), s"customer-$customerId")
            //context.watchWith(customerActor, CustomerTerminated(customerActor, groupId, customerId))
            customerIdToActor += customerId -> customerActor

            controller !  CustomerRegistered(customerId, customerActor)
            customerActor ! CustomerTrainingProgram(trainingProgramOf(customerId), context.self)
          }
          else{
            /** TODO: Do something because customerId is not present on storage */
          }
      }
      this

    case CustomerLogin(customerId, machineLabel,machineType,  machine, phMachine) =>
      customerIdToActor.get(customerId) match {
        case Some(customerActor) =>
          customerActor ! CustomerMachineLogin(machineLabel, machineType, phMachine, machine)
        case None =>
          /** TODO be refactored */
          machine ! CustomerLogging(customerId, null,  null, isLogged = false)
      }
      this

    //case CustomerReady(ex, customer) =>
      //customer ! NextMachineBooking(ex) //ToDo: riattivare?
      //this


    /*case UploadCustomerTrainingProgram(customerId, customer: ActorRef[CustomerActor.Msg]) =>
      customer ! CustomerTrainingProgram(trainingProgramOf(customerId), context.self)
      this*/

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
