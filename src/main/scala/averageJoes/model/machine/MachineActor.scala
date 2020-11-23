package averageJoes.model.machine

import averageJoes.common.{LogManager, LoggableMsg, NonLoggableMsg}
import averageJoes.controller.GymController
import averageJoes.model.customer.{CustomerActor, MachineBooker}
import averageJoes.model.fitness.{ExecutionValues, Exercise}
import averageJoes.model.hardware.PhysicalMachine
import averageJoes.model.hardware.PhysicalMachine.MachineLabel
import averageJoes.model.machine.MachineActor._
import averageJoes.model.workout.{MachineParameters, MachineTypes}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * Machine actor class
 */
object MachineActor{
  def apply(controller: ActorRef[GymController.Msg], physicalMachine: ActorRef[PhysicalMachine.Msg], machineLabel: MachineLabel): Behavior[Msg] =
    Behaviors.setup(context => new MachineActor(context, controller, physicalMachine, machineLabel))

  sealed trait Msg extends LoggableMsg

  object Msg {
    final case class UserLogIn(customerID: String, machineLabel: MachineLabel, machineType: MachineTypes.MachineType) extends Msg
    final case class UserMachineWorkout(customerID: String, machineParameters: MachineParameters, executionValues: ExecutionValues) extends Msg
    final case class BookingRequest(replyTo: ActorRef[MachineBooker.Msg], customerID: String) extends Msg
    final case class CustomerLogging(customerID: String, customer: ActorRef[CustomerActor.Msg], ex:Option[Exercise], isLogged:Boolean) extends Msg
    final case class GoIdle(machineID: String) extends Msg
    final case class StartExercise(duration: FiniteDuration) extends Msg
  }

  private final case class BookingTimeoutException() extends Msg with NonLoggableMsg
}

class MachineActor(context: ActorContext[Msg], controller: ActorRef[GymController.Msg], physicalMachine: ActorRef[PhysicalMachine.Msg],
                   machineLabel: MachineLabel) extends AbstractBehavior[Msg](context) {

  var bookedCustomer: Option[String] = Option.empty
  physicalMachine ! PhysicalMachine.Msg.MachineActorStarted("MachineActor", context.self)

  override def onMessage(msg: Msg): Behavior[Msg] = {
    msg match {
      case Msg.GoIdle(_) => idle();
    }
  }

  /**
   * Idle state. Waiting for messages
   */
  private def idle(): Behavior[Msg] = {
    LogManager.logBehaviourChange(logName,"idle")
    Behaviors.receiveMessagePartial {
      case Msg.UserLogIn(customerID,machineLabel,machineType) =>
        controller ! GymController.Msg.UserLogin(customerID, machineLabel, machineType, physicalMachine, context.self)
        connecting()

      case Msg.BookingRequest(replyTo, customerID) =>
        replyTo ! MachineBooker.OnBookingResponse(context.self, machineLabel, isBooked = true)
        physicalMachine ! PhysicalMachine.Msg.Display("Booked by "+customerID)
        bookedStatus(customerID)
    }
  }

  /**
   * Receive machine parameters and let the physical machine know about them.
   * Check if the user is still connected
   */
  private case object TimerKey
  private def connecting(): Behavior[Msg] = {
    LogManager.logBehaviourChange(logName,"connecting")
    Behaviors.receiveMessagePartial{
      case Msg.CustomerLogging(customerID, customer, ex, isLogged) =>
        if (!isLogged) {
          idle()
        } else {
          physicalMachine ! PhysicalMachine.Msg.ConfigMachine(customerID, ex)
          updateAndLogOut(customer, ex)
        }

      case Msg.BookingRequest (replyTo, _) =>
        replyTo ! MachineBooker.OnBookingResponse(context.self, machineLabel, isBooked = false)
        Behaviors.same
    }
  }

  /**
   * Receive machine parameters executed by the physical machine and write them on file
   */
  private def updateAndLogOut(customer: ActorRef[CustomerActor.Msg], ex: Option[Exercise]): Behavior[Msg] = {
    LogManager.logBehaviourChange(logName,"updateAndLogOut")
    Behaviors.receiveMessagePartial{
      case Msg.BookingRequest(replyTo, _) =>
        replyTo ! MachineBooker.OnBookingResponse(context.self, machineLabel, isBooked = false)
        Behaviors.same

      case Msg.UserMachineWorkout(customerID, parameters,executionValues) =>
        val child: ActorRef[FileWriterActor.Msg] = context.spawn(FileWriterActor(),"childMachineActor")
        child ! FileWriterActor.WriteOnFile(customerID,parameters, executionValues)
        idle()

      case Msg.StartExercise(duration) =>
        customer ! CustomerActor.StartExercising((ex, duration))
        Behaviors.same

      case BookingTimeoutException() => Behaviors.same
    }
  }

  /**
   * Machine in booking status. Release if timer expires
   */
  private def bookedStatus(bookedCustomer: String): Behavior[Msg]= Behaviors.withTimers[Msg] {
    timers => timers.startSingleTimer(TimerKey, BookingTimeoutException(), Duration(120, "sec"))

      LogManager.logBehaviourChange("MACHINE ACTOR", "bookedStatus")

      Behaviors.receiveMessagePartial {
        case BookingTimeoutException() =>
          physicalMachine ! PhysicalMachine.Msg.Display("Free")
          idle()

        case Msg.UserLogIn(customerID, machineLabel, machineType) =>
          if (bookedCustomer.equals(customerID))
            controller ! GymController.Msg.UserLogin(customerID, machineLabel, machineType, physicalMachine, context.self)
          connecting()

        case Msg.BookingRequest(replyTo, _) =>
          replyTo ! MachineBooker.OnBookingResponse(context.self, machineLabel, isBooked = false)
          Behaviors.same
      }
    }


  val logName: String = "Machine Actor"

}
