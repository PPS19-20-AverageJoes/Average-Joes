package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.model.customer.CustomerManager.MachineListOf
import AverageJoes.model.customer.MachineBooker.BookMachine
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.fitness.{BookWhileExercising, CustomerExercising, Exercise, TrainingProgram}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.{BookingRequest, CustomerLogging}
import AverageJoes.model.workout.MachineTypes.MachineType
import AverageJoes.model.fitness.BookWhileExercising.BookTiming
import AverageJoes.model.fitness.CustomerExercising.ExerciseTiming
import AverageJoes.model.hardware.Device.Msg.{CustomerLogOut, CustomerLogged, StartExercise}
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * CustomerActor is the actor that represents an customer in the gym. This actor handle customer machine authentication
 * and keep track of his training program. In base of exercise duration it will create two sub-child actors to handle
 * exercising session and the next smart machine to be booked.
 */
object CustomerActor {
  def apply(manager: ActorRef[CustomerManager.Msg], customerId: String, device: ActorRef[Device.Msg]): Behavior[Msg] =
    Behaviors.setup(context => new CustomerActor(context, manager, customerId, device))

  trait Msg extends LoggableMsg

  final case class TrainingCompleted() extends Msg
  final case class NextMachineBooking(ex: Exercise) extends Msg
  final case class ExerciseCompleted(tp: TrainingProgram) extends Msg
  final case class UpdateTrainingProgram(tp: TrainingProgram) extends Msg
  final case class BookedMachine(machineLabel: Option[MachineLabel]) extends Msg
  final case class MachineList(machines: Set[ActorRef[MachineActor.Msg]]) extends Msg
  final case class StartExercising(ex: (Option[Exercise], FiniteDuration)) extends Msg
  final case class CustomerTrainingProgram(tp: TrainingProgram, group: ActorRef[CustomerGroup.Msg]) extends Msg
  final case class CustomerMachineLogin(machineLabel: MachineLabel, machineType: MachineType, phMachine: ActorRef[PhysicalMachine.Msg], machine: ActorRef[MachineActor.Msg]) extends Msg
}


class CustomerActor(ctx: ActorContext[CustomerActor.Msg], manager: ActorRef[CustomerManager.Msg], customerId: String, device: ActorRef[Device.Msg])
  extends AbstractBehavior[CustomerActor.Msg](ctx) {
  import CustomerActor._

  val managerRef: ActorRef[CustomerManager.Msg] = manager
  var exercisesWithMachines: Map[Exercise, Option[Set[ActorRef[MachineActor.Msg]]]] = Map.empty
  var isLogged: Boolean = false

  override def onMessage(msg: Msg): Behavior[Msg] = {
    msg match{
      /** Customer is instantiated and received his training program. Changing behaviour to active. */
      case CustomerTrainingProgram(tp, _) =>
        active(tp,Option.empty)
    }
  }

  private def active(trainingProgram: TrainingProgram, machineLabel: Option[MachineLabel]): Behavior[Msg] = {
    Behaviors.receiveMessagePartial[Msg] {

      /** Customer received a machine login request. It will check if:
       * - the exercise to be executed is in the training program
       * - if yes, check if it is out of order
       * - otherwise, keep exercising without updating training program */
      case CustomerMachineLogin(machineLabel, machineType, phMachine, machine) =>
        if(!isLogged) {
          isLogged = true

          if (exerciseAlreadyOnProgram(machineType, trainingProgram).isDefined) {
            /** Exercise on training program */
            val toBeExecuted = exerciseToBeExecuted(machineType, trainingProgram)
            machine ! CustomerLogging(customerId, context.self, toBeExecuted, isLogged)
            device ! CustomerLogged(phMachine, machineLabel)
          }
          else {
            /** Exercise out of training program */
            machine ! CustomerLogging(customerId, context.self, Option.empty[Exercise], isLogged)
            device ! CustomerLogged(phMachine, machineLabel)
          }
        }

        else machine ! CustomerLogging(customerId, context.self, Option.empty[Exercise], isLogged)

        Behaviors.same

      /** Machine actor notified customer to start exercising */
      case StartExercising(exExecute) =>
        exercising(exExecute, trainingProgram)
        device ! StartExercise()
        Behaviors.same

      /** BookWhileExercising notified timeout expired, it is time to book again */
      case NextMachineBooking(ex) =>
        requestMachineList(ex)
        Behaviors.same

      /** GymController sent to customer the list of available smart machines of type T */
      case MachineList(machines) =>
        if(machines.nonEmpty) booking(machines)
        Behaviors.same

      /** CustomerExercising notified customer that exercise completed, update training program */
      case ExerciseCompleted(exSet) =>
        isLogged = false
        device ! CustomerLogOut(machineLabel)
        context.self ! UpdateTrainingProgram(exSet)
        Behaviors.same

      /** Self-message to update customer training program */
      case UpdateTrainingProgram(tp) =>
        if(tp.allExercises.isEmpty) TrainingCompleted()
        active(tp,Option.empty)

      /** Updating machine label of machine that was booked from customer */
      case BookedMachine(mLabel) =>
        active(trainingProgram, mLabel)

      /** All exercises of training program were executed */
      case TrainingCompleted() =>
        Behaviors.stopped

    }
  }

  /** Instantiating a child actor to start booking */
  private def booking(machines: Set[ActorRef[MachineActor.Msg]]): Unit = {
    val machineBooker = context.spawn(MachineBooker(context.self, customerId), "machine-booker")
    machineBooker ! BookMachine(machines)
  }

  /** Instantiating two child actors to:
   * - keep track of exercise duration and notify customer
   * - keep track of timeout for next machine to be booked and notify customer */
  private def exercising(ex: (Option[Exercise], FiniteDuration), tp: TrainingProgram): Unit = {
    context.spawn(CustomerExercising(context.self, ex, tp), "exercising") ! ExerciseTiming

    if(exerciseToBookMachineFor(ex._1, tp).isDefined) {
      context.spawn(BookWhileExercising(context.self, exerciseToBookMachineFor(ex._1, tp).get, tp), "book-while-exercising") ! BookTiming
    }
  }

  /** Requesting my CustomerManager to as GymController for available machines of type T */
  private def requestMachineList(ex: Exercise): Unit =  managerRef ! MachineListOf(ex.parameters.machineType, context.self)

  /** Next exercise to be exercuted, checking if the last one was
   * - not in training program
   * - out of order
   * - in order */
  private def exerciseToBeExecuted (mt: MachineType, tp: TrainingProgram): Option[Exercise] = {
    val ex = exerciseAlreadyOnProgram(mt, tp)

    if (ex.isEmpty) Option.empty[Exercise]
    if (isOutOfOrder(ex.get, tp)) ex
    else Option(tp.allExercises.head)
  }

  /** Next exercise to book machine for, checking if the last one was
   * - not in training program
   * - out of order
   * - in order */
  private def exerciseToBookMachineFor(ex: Option[Exercise], tp: TrainingProgram): Option[Exercise] = {
    if(ex.isEmpty && tp.allExercises.nonEmpty) {
      Option(tp.allExercises.head)
    }else if(ex.isEmpty && tp.allExercises.isEmpty){
      Option.empty[Exercise]
    }else if(isOutOfOrder(ex.get, tp)) {
      Option(tp.allExercises.head)
    }else if(tp.allExercises.tail.nonEmpty) {
      Option(tp.allExercises.tail.head)
    }else Option.empty[Exercise]
  }

  private def exerciseAlreadyOnProgram(mt: MachineType, tp: TrainingProgram): Option[Exercise] = tp.allExercises.find(e => e.parameters.machineType.equals(mt))

  private def isOutOfOrder(ex: Exercise, tp: TrainingProgram): Boolean = !tp.allExercises.head.equals(ex)
}


/**
 * MachineBooker is an child actor instantiated by CustomerActor
 * to send BookingRequest to machines. It implement the ask pattern
 * allowing 2 seconds to a machine to send a response, otherwise it
 * will pass to the next one.
 * When the machines list will be finished, will notify Customer Actor
 * with the result.
 */
object MachineBooker {
  trait Msg
  case class BookMachine(machines: Set[ActorRef[MachineActor.Msg]]) extends Msg
  final case class OnBookingResponse(machine: ActorRef[MachineActor.Msg], label: MachineLabel, isBooked: Boolean) extends Msg
  private case class BookedAndFinished(machineLabel: Option[MachineLabel]) extends Msg

  def apply(customer: ActorRef[CustomerActor.Msg], customerId: String): Behavior[Msg] = Behaviors.setup[Msg] { context =>
    Behaviors.receiveMessage[Msg] {
       case BookMachine(machines) =>
         implicit val timeout: Timeout = 2 seconds

         context.ask(machines.head, (booker: ActorRef[MachineBooker.Msg]) => BookingRequest(booker, customerId) ) {
           case Failure(_) => BookMachine(machines.tail)
           case Success(OnBookingResponse(_, machineLabel, true)) => BookedAndFinished(Option(machineLabel))
           case Success(OnBookingResponse(_,_, false)) => if(machines.tail.isEmpty) BookedAndFinished(Option.empty)
                                                          else BookMachine(machines.tail)
         }
         Behaviors.same

      case BookedAndFinished(machineLabel) =>
        customer ! CustomerActor.BookedMachine(machineLabel)
        Behaviors.stopped[Msg]
    }
  }
}


case class IllegalDurationValue() extends IllegalArgumentException

