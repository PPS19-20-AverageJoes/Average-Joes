package AverageJoes.model.customer

import AverageJoes.common.LoggableMsg
import AverageJoes.model.customer.CustomerManager.{MachineList, MachineListOf}
import AverageJoes.model.device.Device.Msg.CustomerLogged
import AverageJoes.model.fitness.{Exercise, TrainingProgram}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.BookingRequest
import AverageJoes.utils.ExerciseUtils.MachineType
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}


object CustomerActor {
  def apply(manager: ActorRef[CustomerManager.Msg], customerId: String): Behavior[Msg] =
    Behaviors.setup(context => new CustomerActor(context, manager, customerId))

  trait Msg extends LoggableMsg

  final case class OnBookingResponse(machine: ActorRef[MachineActor.Msg], isBooked: Boolean) extends Msg
  final case class CustomerTrainingProgram(tp: TrainingProgram) extends Msg

  final case class ExerciseStarted(trainingProgram: TrainingProgram) extends Msg
  final case class ExerciseFinished() extends Msg
  final case object Passivate extends Msg

}


class CustomerActor(ctx: ActorContext[CustomerActor.Msg],
                    manager: ActorRef[CustomerManager.Msg],
                    customerId: String) extends AbstractBehavior[CustomerActor.Msg](ctx) {

  import CustomerActor._

  val managerRef: ActorRef[CustomerManager.Msg] = manager
  var exercisesWithMachines: Map[Exercise, Option[Set[ActorRef[MachineActor.Msg]]]] = Map.empty

  override def onMessage(msg: Msg): Behavior[Msg] = waiting()


  private def waiting(): Behavior[Msg] = Behaviors.receiveMessage[Msg]{
    /** Customer group informs me about my training program. Now I will extract the exercises
     * and initialize the data structure to keep track of ActorRef of MachineActors */
    case CustomerTrainingProgram(tp) =>
      requestMachineList(tp)
      booking(tp)

    case Passivate =>
      Behaviors.stopped
  }

  private def booking(tp: TrainingProgram): Behavior[Msg] = Behaviors.receiveMessage {
    case MachineList(machines) =>
      implicit val timeout: Timeout = 3 seconds

      context.ask(machines.head, BookingRequest) {
        case Success(OnBookingResponse(_, true)) => ExerciseStarted(tp)
        case Failure(_) => MachineList(machines.tail)
      }
      this
    case ExerciseStarted(tp) => exercising(tp)

    case _ => this
  }

  /**
   * Set training program as a "global" value. Add add+remove methods for training program
   */
  private def exercising(tp: TrainingProgram): Behavior[Msg] = Behaviors.receiveMessage[Msg] {
    case ExerciseFinished() =>
      context.self ! CustomerTrainingProgram(updatedTrainingProgram(tp))
      this
    case _ => exercising(tp)
  }


  private def requestMachineList(tp: TrainingProgram) = {
    initializeExMachines(tp.allExercises)
    managerRef ! MachineListOf(machineToBeExecuted(tp.allExercises).get, context.self)
  }

  /** Method to return a map of an exercise and a set of ActorRef of the machines to be able
   * to execute the exercise on. Firstly it is an empty set. */
  private def initializeExMachines(exercises: Set[Exercise]): Map[Exercise, Set[ActorRef[MachineActor.Msg]]] = {
    val initMachineRef = (m: Map[Exercise, Set[ActorRef[MachineActor.Msg]]], ex: Exercise) =>  m + (ex -> Set.empty[ActorRef[MachineActor.Msg]])
    exercises.foldLeft (Map.empty[Exercise, Set[ActorRef[MachineActor.Msg]]]) (initMachineRef)
  }


  private def machineToBeExecuted(exercises: Set[Exercise]): Option[MachineType] = {
    import AverageJoes.utils.ExerciseUtils.ExerciseParameters._
    exercises.head.executionParameters.valueOf(TYPE)[MachineType]
  }

  /**
   * TODO: handle empty set or no exercises left
   */
  private def updatedTrainingProgram(tp: TrainingProgram): TrainingProgram =
    tp.removeExercise(tp.allExercises.head)

}
