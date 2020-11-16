package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.model.customer.CustomerActor.CustomerTrainingProgram
import AverageJoes.model.customer.CustomerManager.{MachineList, MachineListOf}
import AverageJoes.model.customer.MachineBooker.BookMachine
import AverageJoes.model.device.Device
import AverageJoes.model.device.Device.Msg.CustomerLogged
import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.model.fitness.{CustomerExercising, Exercise, TrainingProgram}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.{BookingRequest, CustomerLogging}
import AverageJoes.model.machine.PhysicalMachine.MachineLabel
import AverageJoes.utils.ExerciseUtils.MachineTypes.MachineType
import AverageJoes.utils.SafePropertyValue.SafePropertyVal
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}


object CustomerActor {
  def apply(manager: ActorRef[CustomerManager.Msg], customerId: String): Behavior[Msg] = Behaviors.setup(context => new CustomerActor(context, manager, customerId))

  trait Msg extends LoggableMsg
  final case class CustomerTrainingProgram(tp: TrainingProgram) extends Msg
  final case class CustomerMachineLogin(machineLabel: MachineLabel, machine: ActorRef[MachineActor.Msg], device: ActorRef[Device.Msg]) extends Msg
  final case class ExerciseStarted(trainingProgram: TrainingProgram) extends Msg
  final case class ExerciseCompleted(tp: TrainingProgram) extends Msg

  final case object Passivate extends Msg
}

class CustomerActor(ctx: ActorContext[CustomerActor.Msg], manager: ActorRef[CustomerManager.Msg], customerId: String) extends AbstractBehavior[CustomerActor.Msg](ctx) {
  import CustomerActor._

  val managerRef: ActorRef[CustomerManager.Msg] = manager
  var exercisesWithMachines: Map[Exercise, Option[Set[ActorRef[MachineActor.Msg]]]] = Map.empty
  var logged: Boolean = false
  var trainingProgram: Option[TrainingProgram] = None

  override def onMessage(msg: Msg): Behavior[Msg] = waiting()

  private def waiting(): Behavior[Msg] = Behaviors.receiveMessage[Msg]{
    case CustomerTrainingProgram(tp) =>
      trainingProgram = Option.apply(tp)
      requestMachineList(tp)
      Behaviors.same

    case CustomerMachineLogin(machineLabel, machine, device) =>
      if(loggingAllowed()) {
        logged = true
        /** TODO: machine actor should expect Parameters, and not MachineParameters */
        machine ! CustomerLogging(customerId, parameters(trainingProgram.get), isLogged = true)
        device ! CustomerLogged(machineLabel, machine)
        context.self ! ExerciseStarted(trainingProgram.get)
      }
      else  machine ! CustomerLogging(customerId, null, isLogged = false)
      Behaviors.same

    case MachineList(machines) =>
      booking(machines)
      Behaviors.same

    case ExerciseStarted(tp) =>
      exercising(tp)
      Behaviors.same
      
    case ExerciseCompleted(tp) =>
      logged = false
      context.self ! CustomerTrainingProgram(updatedTrainingProgram(tp))
      this

    case Passivate =>
      Behaviors.stopped
  }


  private def booking(machines: Set[ActorRef[MachineActor.Msg]]): Unit = {
    val machineBooker = context.spawn(MachineBooker(context.self, customerId), "machine-booker")
    machineBooker ! BookMachine(machines)
  }
  
  private def exercising(tp: TrainingProgram): Unit = {
    /** TODO: handle exercise duration */
    context.spawn(CustomerExercising(context.self, 10.seconds, tp), "exercising")
  }


  private def requestMachineList(tp: TrainingProgram): Unit = {
    initializeExMachines(tp.allExercises)
    managerRef ! MachineListOf(machineToBeExecuted(tp.allExercises.head).get, context.self)
  }

  private def initializeExMachines(exercises: Set[Exercise]): Map[Exercise, Set[ActorRef[MachineActor.Msg]]] = {
    val initMachinesRef = (m: Map[Exercise, Set[ActorRef[MachineActor.Msg]]], ex: Exercise) =>  m + (ex -> Set.empty[ActorRef[MachineActor.Msg]])
    exercises.foldLeft (Map.empty[Exercise, Set[ActorRef[MachineActor.Msg]]]) (initMachinesRef)
  }


  private def machineToBeExecuted(ex: Exercise): Option[MachineType] = {
    if (ex != null)  Option.apply(ex.executionParameters.typeParams) else Option.empty
  }

  private def loggingAllowed(): Boolean = !logged

  /**
   * TODO: handle empty set or no exercises left
   */
  private def updatedTrainingProgram(tp: TrainingProgram): TrainingProgram =
    tp.removeExercise(tp.allExercises.head)

  private def parameters(tp: TrainingProgram): Parameters[SafePropertyVal] = tp.allExercises.head.executionParameters
}



object MachineBooker {
  trait Msg
  case class BookMachine(machines: Set[ActorRef[MachineActor.Msg]]) extends Msg
  final case class OnBookingResponse(machine: ActorRef[MachineActor.Msg], isBooked: Boolean) extends Msg
  private case class BookedAndFinished() extends Msg

  def apply(customer: ActorRef[CustomerActor.Msg], customerId: String): Behavior[Msg] = Behaviors.setup[Msg] { context =>
    Behaviors.receiveMessage[Msg] {
      case BookMachine(machines) =>
        implicit val timeout: Timeout = 3 seconds

        /** TODO: machine actor should reply to MachineBooker and keep track of CustomerActor */
        context.ask(machines.head, (booker: ActorRef[MachineBooker.Msg]) => BookingRequest(booker, customer, customerId) ) {
          case Success(OnBookingResponse(_, true)) => BookedAndFinished() // Send msg to customer actor
          case Failure(_) => BookMachine(machines.tail)
        }
        Behaviors.same

      case BookedAndFinished() => Behaviors.stopped[Msg]
    }
  }
}



object AnotherMachineBooking {
  trait Msg
  private case object BookAnotherMachine extends Msg
  private case object TimerKey

  def apply(target: ActorRef[CustomerActor.Msg], after: FiniteDuration, tp: TrainingProgram): Behavior[Msg] = {
    Behaviors.withTimers(timers => new AnotherMachineBooking(timers, target, new FiniteDuration(after.length-10, after.unit), tp).initializing())
  }
}

class AnotherMachineBooking(
                 timers: TimerScheduler[AnotherMachineBooking.Msg],
                 target: ActorRef[CustomerActor.Msg],
                 after: FiniteDuration,
                 tp: TrainingProgram) {
  import AnotherMachineBooking._

    private def initializing(): Behavior[AnotherMachineBooking.Msg] = {
      Behaviors.receiveMessage[AnotherMachineBooking.Msg] { message =>
        timers.startSingleTimer(TimerKey, BookAnotherMachine, after)
        waitUntilTimeout()
      }
    }

    def waitUntilTimeout(): Behavior[Msg] = {
      Behaviors.receiveMessage[Msg] {
        case BookAnotherMachine =>
          target ! CustomerTrainingProgram(tp)
          Behaviors.stopped
      }
    }
}




