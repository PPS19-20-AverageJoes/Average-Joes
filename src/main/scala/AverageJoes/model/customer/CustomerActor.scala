package AverageJoes.model.customer

import AverageJoes.common.{LoggableMsg, LoggableMsgTo}
import AverageJoes.model.customer.CustomerManager.MachineListOf
import AverageJoes.model.customer.MachineBooker.BookMachine
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.fitness.{BookWhileExercising, CustomerExercising, Exercise, TrainingProgram}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.machine.MachineActor.Msg.{BookingRequest, CustomerLogging}
import AverageJoes.common.MachineTypes.MachineType
import AverageJoes.model.fitness.BookWhileExercising.BookTiming
import AverageJoes.model.fitness.CustomerExercising.ExerciseTiming
import AverageJoes.model.hardware.Device.Msg.{CustomerLogOut, CustomerLogged}
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
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
  final case class CustomerMachineLogin(machineLabel: MachineLabel, machineType: MachineType, phMachine: ActorRef[PhysicalMachine.Msg], machine: ActorRef[MachineActor.Msg]) extends Msg
  final case class ExerciseStarted(exExecute: Exercise, trainingProgram: TrainingProgram) extends Msg
  final case class ExerciseCompleted(tp: TrainingProgram) extends Msg
  final case class NextMachineBooking(ex: Exercise) extends Msg
  final case class UpdateTrainingProgram(tp: TrainingProgram) extends Msg
  final case class TrainingCompleted() extends Msg
  final case class MachineList(machines: Set[ActorRef[MachineActor.Msg]]) extends Msg

  final case class BookedMachine(machineLabel: Option[MachineLabel]) extends Msg

  final case object Passivate extends Msg
}

class CustomerActor(ctx: ActorContext[CustomerActor.Msg], manager: ActorRef[CustomerManager.Msg], customerId: String, device: ActorRef[Device.Msg])
  extends AbstractBehavior[CustomerActor.Msg](ctx) {
  import CustomerActor._

  val managerRef: ActorRef[CustomerManager.Msg] = manager
  var exercisesWithMachines: Map[Exercise, Option[Set[ActorRef[MachineActor.Msg]]]] = Map.empty
  var logged: Boolean = false

  override def onMessage(msg: Msg): Behavior[Msg] = {
    msg match{
      case CustomerTrainingProgram(tp, group) =>
        println("[CUSTOMER ACTOR] "+customerId+": received training program with ---- " + tp.allExercises)
        //group ! CustomerReady(tp.allExercises.head, context.self)
        active(tp,Option.empty)
    }
  }


  private def active(trainingProgram: TrainingProgram, machineLabel: Option[MachineLabel]): Behavior[Msg] = {
    Behaviors.receiveMessagePartial[Msg] {

      case NextMachineBooking(ex) =>
        println("[CUSTOMER ACTOR] "+customerId+": received NextMachineBooking")
        requestMachineList(ex)
        Behaviors.same

      case MachineList(machines) =>
        println("[CUSTOMER ACTOR] "+customerId+": received MachinesList --- "+ machines)
        if(machines.nonEmpty) booking(machines)
        Behaviors.same

      case CustomerMachineLogin(machineLabel, machineType, phMachine, machine) =>
        if(loggingAllowed(machineType, trainingProgram)) {
          println("[CUSTOMER ACTOR] "+customerId+": accepted logging request from "+ machineLabel)
          println("[CUSTOMER ACTOR] "+customerId+": is doing "+machineType+" and first exercise is "+ trainingProgram.allExercises.head.parameters.machineType)

          logged = true

           val toBeExecuted = exerciseToBeExecuted(machineType,trainingProgram)

            machine ! CustomerLogging(customerId, toBeExecuted.parameters, isLogged = true)
            context.self ! ExerciseStarted(toBeExecuted, trainingProgram)
            device ! CustomerLogged(phMachine, machineLabel)
        }
        else {
          println("[CUSTOMER ACTOR] "+customerId+": refused logging request from "+ machineLabel)
          machine ! CustomerLogging(customerId, null, isLogged = false)
        }
        Behaviors.same


      case ExerciseStarted(exExecute, tp) =>
        exercising(exExecute, tp)
        Behaviors.same


      case ExerciseCompleted(exSet) =>
        println("[CUSTOMER ACTOR] "+customerId+": completed exercising, updated exercise set is: "+ exSet)
        logged = false
        device ! CustomerLogOut(machineLabel)
        context.self ! UpdateTrainingProgram(exSet)
        Behaviors.same


      case UpdateTrainingProgram(tp) =>
        println("[CUSTOMER ACTOR] "+customerId+": updated training program ---"+ tp.allExercises)
        if(tp.allExercises.isEmpty) TrainingCompleted()
        active(tp,Option.empty)

      case BookedMachine(machineLabel) => active(trainingProgram, machineLabel)

      case TrainingCompleted() =>
        println("[CUSTOMER ACTOR] "+customerId+": TRAINING COMPLETED ---")
        Behaviors.stopped

    }
  }


  private def booking(machines: Set[ActorRef[MachineActor.Msg]]): Unit = {
    val machineBooker = context.spawn(MachineBooker(context.self, customerId), "machine-booker")
    machineBooker ! BookMachine(machines)
  }

  private def exercising(exExec: Exercise, tp: TrainingProgram): Unit = {

    println("[CUSTOMER ACTOR] "+customerId+": started exercising --- "+ exExec)
    context.spawn(CustomerExercising(context.self, exExec, tp), "exercising") ! ExerciseTiming

    if(exerciseToBookMachineFor(exExec, tp).isDefined) {
      println("[CUSTOMER ACTOR] " + customerId + ": next machine to be booked is --- " + exerciseToBookMachineFor(exExec, tp), tp)
      context.spawn(BookWhileExercising(context.self, exerciseToBookMachineFor(exExec, tp).get, tp), "book-while-exercising") ! BookTiming
    }
    else
      println("[CUSTOMER ACTOR] "+customerId+": last exercise, NO MORE MACHINE TO BE BOOKED --- "+ exerciseToBookMachineFor(exExec, tp), tp)

  }


  private def requestMachineList(ex: Exercise): Unit = {
    managerRef ! MachineListOf(ex.parameters.machineType, context.self)
  }

  private def exerciseToBeExecuted (mt: MachineType, tp: TrainingProgram): Exercise = {
    val ex = exerciseAlreadyOnProgram(mt, tp).get
    if (isOutOfOrder(ex, tp)) ex
    else tp.allExercises.head
  }

  private def exerciseToBookMachineFor(ex: Exercise, tp: TrainingProgram): Option[Exercise] =
    if(isOutOfOrder(ex, tp)) Option(tp.allExercises.head)
    else if(tp.allExercises.tail.nonEmpty) Option(tp.allExercises.tail.head)
    else Option.empty[Exercise]


  /** TODO: to be refactored. Done this way only for testing purpose */
  private def loggingAllowed(mt: MachineType, tp: TrainingProgram): Boolean = {
    if(logged) {
      println("[CUSTOMER ACTOR LOGIN NOT ALLOWED] " +customerId+" already logged"); false
    } else if(tp.allExercises.isEmpty) {
      println("[CUSTOMER ACTOR  LOGIN NOT ALLOWED] " +customerId+" empty training program"); false
    } else if(exerciseAlreadyOnProgram(mt, tp).isEmpty) {
      println("[CUSTOMER ACTOR  LOGIN NOT ALLOWED] " +customerId+" no exercises found with this machine type"); false
    } else{true}
  }


  private def exerciseAlreadyOnProgram(mt: MachineType, tp: TrainingProgram): Option[Exercise] = tp.allExercises.find(e => e.parameters.machineType.equals(mt))

  private def isOutOfOrder(ex: Exercise, tp: TrainingProgram): Boolean = !tp.allExercises.head.equals(ex)


}



object MachineBooker {
  trait Msg
  case class BookMachine(machines: Set[ActorRef[MachineActor.Msg]]) extends Msg with LoggableMsgTo { override def To: String = "MachineBooker" }
  final case class OnBookingResponse(machine: ActorRef[MachineActor.Msg], label: MachineLabel, isBooked: Boolean) extends Msg
  private case class BookedAndFinished(machineLabel: Option[MachineLabel]) extends Msg with LoggableMsgTo { override def To: String = "MachineBooker" }

  def apply(customer: ActorRef[CustomerActor.Msg], customerId: String): Behavior[Msg] = Behaviors.setup[Msg] { context =>
    Behaviors.receiveMessage[Msg] {
       case BookMachine(machines) =>
         println("[CUSTOMER ACTOR] "+customerId+": started booking ---")
         implicit val timeout: Timeout = 3 seconds

         context.ask(machines.head, (booker: ActorRef[MachineBooker.Msg]) => BookingRequest(booker, customerId) ) {
           case Success(OnBookingResponse(_,machineLabel, true)) => BookedAndFinished(Option(machineLabel))
           case Success(OnBookingResponse(_,_, false)) =>
             if(machines.tail.isEmpty) BookedAndFinished(Option.empty)
             else BookMachine(machines.tail)
           case Failure(_) => BookMachine(machines.tail)
         }
         Behaviors.same


      case BookedAndFinished(machineLabel) =>
        customer ! CustomerActor.BookedMachine(machineLabel);
        Behaviors.stopped[Msg]
    }
  }

}


case class IllegalDurationValue() extends IllegalArgumentException
