package AverageJoes.model.machine

import AverageJoes.common.{LogManager, LogOnMessage, LoggableMsg, LoggableMsgTo, MachineTypes, NonLoggableMsg}
import AverageJoes.controller.GymController
import AverageJoes.model.customer.CustomerActor.StartExercising
import AverageJoes.model.customer.{CustomerActor, CustomerManager, MachineBooker}
import AverageJoes.model.fitness.{ExecutionValues, Exercise}
import AverageJoes.model.hardware.PhysicalMachine
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
import AverageJoes.model.machine.MachineActor._
import AverageJoes.model.workout.MachineParameters
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration.Duration

/**
 * Machine actor class
 * controller: controller ActorRef
 */
object MachineActor{
  def apply(controller: ActorRef[GymController.Msg], physicalMachine: ActorRef[PhysicalMachine.Msg], machineLabel: MachineLabel): Behavior[Msg] =
    Behaviors.setup(context => new MachineActor(context, controller, physicalMachine, machineLabel))

  sealed trait Msg extends LoggableMsg

  object Msg {
    final case class UserLogIn(customerID: String, machineLabel: MachineLabel, machineType: MachineTypes.MachineType) extends Msg
    final case class UserMachineWorkoutPlan(customerID: String) extends Msg
    final case class UserMachineWorkout(customerID: String, machineParameters: MachineParameters, executionValues: ExecutionValues) extends Msg
    final case class DeadDevice(customerID: String , exercise: MachineParameters) extends Msg
    final case class BookingRequest(replyTo: ActorRef[MachineBooker.Msg], customerID: String) extends Msg
    final case class CustomerLogging(customerID: String, customer: ActorRef[CustomerActor.Msg], ex:Option[Exercise], isLogged:Boolean) extends Msg
    final case class GoIdle(machineID: String) extends Msg
    final case class StartExercise() extends Msg
  }

  private final case class BookingTimeoutException() extends Msg with NonLoggableMsg
}

class MachineActor(context: ActorContext[Msg], controller: ActorRef[GymController.Msg], physicalMachine: ActorRef[PhysicalMachine.Msg],
                   machineLabel: MachineLabel) extends AbstractBehavior[Msg](context) {

  var bookedCustomer: Option[String] = Option.empty
  physicalMachine ! PhysicalMachine.Msg.MachineActorStarted("", context.self) //TODO non ho il machine id

  override def onMessage(msg: Msg): Behavior[Msg] = {
    LogManager.logBehaviourChange(logName,"onMessage")
    msg match {
      case Msg.GoIdle(machineID) => idle();
    }
  }

  private def idle(): Behavior[Msg] = {
    LogManager.logBehaviourChange(logName,"idle")
    Behaviors.receiveMessagePartial {
      case Msg.UserLogIn(customerID,machineLabel,machineType) =>
        println(controller)
        controller ! GymController.Msg.UserLogin(customerID, machineLabel, machineType, physicalMachine, context.self)
        connecting()

      case Msg.BookingRequest(replyTo, customerID) =>
        replyTo ! MachineBooker.OnBookingResponse(context.self, machineLabel, true)
        physicalMachine ! PhysicalMachine.Msg.Display("Booked by "+customerID)
        bookedStatus(customerID)
    }
  }

  /**
   * receive machine parameters and let the physical machine know about them
   * check if the user is still connected
   * @return
   */
  private case object TimerKey
  private def connecting(): Behavior[Msg] = Behaviors.withTimers[Msg] {timers =>
    timers.startSingleTimer(TimerKey, BookingTimeoutException(), Duration(3000, "millis"))
    LogManager.logBehaviourChange(logName,"connecting")
    Behaviors.receiveMessagePartial{
      case Msg.CustomerLogging(customerID, customer, ex, isLogged) =>
        if (!isLogged) {
          idle()
        } else {
          physicalMachine ! PhysicalMachine.Msg.ConfigMachine(customerID, ex)
          updateAndLogOut(customer, ex)
        }

      case BookingTimeoutException() => physicalMachine ! PhysicalMachine.Msg.Display("Free"); idle()

      case Msg.BookingRequest (replyTo, customerID) =>
        replyTo ! MachineBooker.OnBookingResponse(context.self, machineLabel, false)
        Behaviors.same
    }
  }

  //userlogin()--> sloggato => chiedo i parametri di uscita alla physicalmachine
  //spawn sotto attore che scrive su disco
  //deaddevice() --> idle => chiedo i parametri
  //spawn sotto attore che scrive su disco
  private def updateAndLogOut(customer: ActorRef[CustomerActor.Msg], ex:Option[Exercise]): Behavior[Msg] = {
    LogManager.logBehaviourChange(logName,"updateAndLogOut")
    Behaviors.receiveMessagePartial{
      case Msg.BookingRequest(replyTo, customerID) =>
        replyTo ! MachineBooker.OnBookingResponse(context.self, machineLabel, false)
        Behaviors.same

      case Msg.UserMachineWorkout(customerID, parameters,executionValues) =>
        val child: ActorRef[FileWriterActor.Msg] = context.spawn(FileWriterActor(),"childMachineActor")
        child ! FileWriterActor.WriteOnFile(customerID,parameters)
        idle()

      case Msg.StartExercise() =>
        customer ! CustomerActor.StartExercising(ex)
        Behaviors.same //ToDo: mandare messaggio a CustomerActor, che lo manderà al device

      case BookingTimeoutException() => Behaviors.same
    }
  }
  //verificare che i custumer id coincida con quello bookato
  private def bookedStatus(bookedCustomer: String): Behavior[Msg]= {
    LogManager.logBehaviourChange(logName,"bookedStatus")
    Behaviors.receiveMessagePartial{
      case Msg.UserLogIn(customerID, machineLabel, machineType) =>
        if(bookedCustomer.equals(customerID))
          controller ! GymController.Msg.UserLogin(customerID, machineLabel, machineType, physicalMachine,context.self)
        connecting()

      case Msg.BookingRequest(replyTo, customerID) =>
        replyTo ! MachineBooker.OnBookingResponse(context.self, machineLabel, false)
        Behaviors.same
    }
  }

  val logName: String = "Machine Actor"

}
