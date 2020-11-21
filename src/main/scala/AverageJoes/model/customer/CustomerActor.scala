package AverageJoes.model.customer


import AverageJoes.common.{LogManager, LoggableMsg, LoggableMsgTo}
import AverageJoes.model.customer.CustomerManager.MachineListOf
import AverageJoes.model.customer.MachineBooker.BookMachine
import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.fitness.{BookWhileExercising, CustomerExercising, Exercise, TrainingProgram}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.{BookingRequest, CustomerLogging}
import AverageJoes.utils.SafePropertyValue.SafePropertyVal
import AverageJoes.common.MachineTypes.MachineType
import AverageJoes.utils.ExerciseUtils.ExerciseParameters.DURATION
import AverageJoes.model.customer.CustomerGroup.CustomerReady
import AverageJoes.model.fitness.BookWhileExercising.BookTiming
import AverageJoes.model.fitness.CustomerExercising.ExerciseTiming
import AverageJoes.model.hardware.Device.Msg.{CustomerLogOut, CustomerLogged}
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
import AverageJoes.utils.SafePropertyValue.NonNegative.NonNegDuration
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}


object CustomerActor {
  def apply(manager: ActorRef[CustomerManager.Msg], customerId: String, device: ActorRef[Device.Msg]): Behavior[Msg] =
    Behaviors.setup(context => new CustomerActor(context, manager, customerId, device))

  trait Msg extends LoggableMsg
  final case class CustomerTrainingProgram(tp: TrainingProgram, group: ActorRef[CustomerGroup.Msg]) extends Msg
  final case class CustomerMachineLogin(machineLabel: MachineLabel, phMachine: ActorRef[PhysicalMachine.Msg], machine: ActorRef[MachineActor.Msg]) extends Msg
  final case class ExerciseStarted(trainingProgram: TrainingProgram) extends Msg
  final case class ExerciseCompleted(tp: TrainingProgram) extends Msg
  final case class NextMachineBooking(ex: Exercise) extends Msg
  final case class UpdateTrainingProgram(tp: TrainingProgram) extends Msg
  final case class TrainingCompleted() extends Msg
  final case class MachineList(machines: Set[ActorRef[MachineActor.Msg]]) extends Msg

  final case class BookedMachine(machineLabel: MachineLabel) extends Msg

  final case object Passivate extends Msg
}

class CustomerActor(ctx: ActorContext[CustomerActor.Msg], manager: ActorRef[CustomerManager.Msg], customerId: String, device: ActorRef[Device.Msg])
  extends AbstractBehavior[CustomerActor.Msg](ctx) {
  import CustomerActor._

  val managerRef: ActorRef[CustomerManager.Msg] = manager
  var exercisesWithMachines: Map[Exercise, Option[Set[ActorRef[MachineActor.Msg]]]] = Map.empty
  var logged: Boolean = false
  //var trainingProgram: Option[TrainingProgram] = None

  override def onMessage(msg: Msg): Behavior[Msg] = {
    LogManager.logBehaviourChange("CustomerActor_"+customerId,"OnMessage")
    msg match{
      case CustomerTrainingProgram(tp, group) =>
        group ! CustomerReady(tp.allExercises.head, context.self)
        active(tp,Option.empty)
      case _ => println("************************"+msg); this
    }
  }


  private def active(trainingProgram: TrainingProgram, machineLabel: Option[MachineLabel]): Behavior[Msg] = {
    LogManager.logBehaviourChange("CustomerActor_"+customerId,"active")
    Behaviors.receiveMessagePartial[Msg] {

      case NextMachineBooking(ex) =>
        requestMachineList(ex)
        Behaviors.same

      case MachineList(machines) =>
        if(machines.nonEmpty) booking(machines)
        Behaviors.same

      case CustomerMachineLogin(machineLabel, phMachine, machine) =>
        if(loggingAllowed()) {
          logged = true
          machine ! CustomerLogging(customerId,trainingProgram.allExercises.head.parameters, isLogged = true)
          device ! CustomerLogged(phMachine, machineLabel)
          context.self ! ExerciseStarted(trainingProgram)
        }
        else machine ! CustomerLogging(customerId, null, isLogged = false)
        Behaviors.same


      case ExerciseStarted(tp) =>
        exercising(tp)
        Behaviors.same


      case ExerciseCompleted(tp) =>
        logged = false
        device ! CustomerLogOut(machineLabel)
        context.self ! UpdateTrainingProgram(updatedTrainingProgram(tp))
        Behaviors.same


      case UpdateTrainingProgram(tp) =>
        if(tp.allExercises.isEmpty) TrainingCompleted()
       active(tp,Option.empty)

      case BookedMachine(machineLabel) => active(trainingProgram, Option(machineLabel))

      /*case TrainingCompleted() =>
        Behaviors.stopped*/


      /*case Passivate =>
        Behaviors.stopped*/
    }
  }


  private def booking(machines: Set[ActorRef[MachineActor.Msg]]): Unit = {
    val machineBooker = context.spawn(MachineBooker(context.self, customerId), "machine-booker")
    machineBooker ! BookMachine(machines)
  }

  private def exercising(tp: TrainingProgram): Unit = {
    /** TODO: handle exercise duration */
    context.spawn(CustomerExercising(context.self, exDuration(tp), tp), "exercising") ! ExerciseTiming
    context.spawn(BookWhileExercising(context.self, exDuration(tp), tp), "book-while-exercising") ! BookTiming
  }


  private def requestMachineList(ex: Exercise): Unit = {
    managerRef ! MachineListOf(machineToBeExecuted(ex).get, context.self)
  }

  /* private def initializeExMachines(exercises: Set[Exercise]): Map[Exercise, Set[ActorRef[MachineActor.Msg]]] = {
    val initMachinesRef = (m: Map[Exercise, Set[ActorRef[MachineActor.Msg]]], ex: Exercise) =>  m + (ex -> Set.empty[ActorRef[MachineActor.Msg]])
    exercises.foldLeft (Map.empty[Exercise, Set[ActorRef[MachineActor.Msg]]]) (initMachinesRef)
  } */


  private def machineToBeExecuted(ex: Exercise): Option[MachineType] = {
    if (ex != null)  Option.apply(ex.parameters.machineType) else Option.empty
  }

  private def loggingAllowed(): Boolean = !logged

  /**
   * TODO: handle empty set or no exercises left
   */
  private def updatedTrainingProgram(tp: TrainingProgram): TrainingProgram =
    tp.removeExercise(tp.allExercises.head)

  private def parameters(tp: TrainingProgram): Parameters[SafePropertyVal] = tp.allExercises.head.executionParameters

  private def exDuration(tp: TrainingProgram): NonNegDuration = tp.allExercises.head.parameters.duration

}


object MachineBooker {
  trait Msg
  case class BookMachine(machines: Set[ActorRef[MachineActor.Msg]]) extends Msg with LoggableMsgTo { override def To: String = "MachineBooker" }
  final case class OnBookingResponse(machine: ActorRef[MachineActor.Msg], label: MachineLabel, isBooked: Boolean) extends Msg
  private case class BookedAndFinished() extends Msg with LoggableMsgTo { override def To: String = "MachineBooker" }

  def apply(customer: ActorRef[CustomerActor.Msg], customerId: String): Behavior[Msg] = Behaviors.setup[Msg] { context =>
    Behaviors.receiveMessage[Msg] {
       case BookMachine(machines) =>
         implicit val timeout: Timeout = 3 seconds

         /** TODO: machine actor should reply to MachineBooker and keep track of CustomerActor */
         context.ask(machines.head, (booker: ActorRef[MachineBooker.Msg]) => BookingRequest(booker, customerId) ) {
           case Success(OnBookingResponse(_,machineLabel, true)) => customer ! CustomerActor.BookedMachine(machineLabel) ; BookedAndFinished()
           case Success(OnBookingResponse(_,_, false)) => BookMachine(machines.tail)
           case Failure(_) => BookMachine(machines.tail)
         }
         Behaviors.same


      case BookedAndFinished() => Behaviors.stopped[Msg]
    }
  }

}


case class IllegalDurationValue() extends IllegalArgumentException
