package AverageJoes.model.machine

import AverageJoes.common.{LogOnMessage, LoggableMsg, MachineTypes}
import AverageJoes.controller.GymController
import AverageJoes.model.customer.CustomerManager
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
  def apply(controller: ActorRef[GymController.Msg], physicalMachine: ActorRef[PhysicalMachine.Msg], machineType: MachineTypes.MachineType): Behavior[Msg] =
    Behaviors.setup(context => new MachineActor(context, controller, physicalMachine, machineType))

  sealed trait Msg extends LoggableMsg

  object Msg {
    final case class UserLogIn(customerID: String, machineLabel: MachineLabel) extends Msg
    final case class UserMachineWorkoutPlan(customerID: String) extends Msg
    final case class UserMachineWorkout(customerID: String, machineParameters: MachineParameters) extends Msg
    final case class DeadDevice(customerID: String , exercise: MachineParameters) extends Msg
    final case class BookingRequest(replyTo: ActorRef[CustomerManager.Msg], customerID: String) extends Msg
    final case class CustomerLogging(customerID: String, machineParameters: MachineParameters, isLogged:Boolean) extends Msg
  }

  private final case class BookingTimeoutException() extends Msg
}

class MachineActor(context: ActorContext[Msg], controller: ActorRef[GymController.Msg], physicalMachine: ActorRef[PhysicalMachine.Msg],
                   machineType: MachineTypes.MachineType) extends AbstractBehavior[Msg](context) with LogOnMessage[Msg]{

  var bookedCustomer: Option[String] = Option.empty
  physicalMachine ! PhysicalMachine.Msg.MachineActorStarted("", context.self) //TODO non ho il machine id

  override def onMessageLogged(msg: Msg): Behavior[Msg] = {
    idle()
  }

  private def idle(): Behavior[Msg] = {
    Behaviors.receiveMessage {
      case Msg.UserLogIn(customerID, machineLabel) =>
        controller ! GymController.Msg.UserLogin(customerID, machineLabel, context.self)
        connecting()

      case Msg.BookingRequest(replyTo, customerID) =>
        bookedCustomer = Option.apply(customerID)
        //replyTo ! CustomerManager.BookingConfirmation(customerID, machineType,true)
        bookedStatus()
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
    Behaviors.receiveMessage {
      case Msg.CustomerLogging (customerID, machineParameters, isLogged) =>
        if (! isLogged) {
          idle ()
        } else {
          //physicalMachine ! PhysicalMachine.Msg.ConfigMachine(customerID, machineParameters)
          updateAndLogOut ()
        }
      case BookingTimeoutException() => idle()
      case Msg.BookingRequest (replyTo, customerID) =>
        //replyTo ! CustomerManager.BookingConfirmation(customerID, machineType, false)
        Behaviors.same
    }
  }

  //userlogin()--> sloggato => chiedo i parametri di uscita alla physicalmachine
  //spawn sotto attore che scrive su disco
  //deaddevice() --> idle => chiedo i parametri
  //spawn sotto attore che scrive su disco
  private def updateAndLogOut(): Behavior[Msg] = {

    Behaviors.receiveMessage {
      case Msg.BookingRequest(replyTo, customerID) =>
        //replyTo ! CustomerManager.BookingConfirmation(customerID, machineType, false)
        Behaviors.same

      case Msg.UserMachineWorkout(customerID, parameters) =>
        var child: ActorRef[FileWriterActor.Msg] = context.spawn(FileWriterActor(),"")
        child ! FileWriterActor.WriteOnFile(customerID,parameters)
        idle()

    }
  }
  //verificare che i custumer id coincida con quello bookato
  private def bookedStatus(): Behavior[Msg]= {
    Behaviors.receiveMessage{
      case Msg.UserLogIn(customerID, machineLabel) =>
        if(bookedCustomer.get.equals(customerID))
          controller ! GymController.Msg.UserLogin(customerID, machineLabel, context.self)
        connecting()

      case Msg.BookingRequest(replyTo, customerID) =>
        //replyTo ! CustomerManager.BookingConfirmation(customerID, machineType, false)
        Behaviors.same
    }
  }


  override val logName: String = "Machine Actor"
  override val loggingContext: ActorContext[Msg] = this.context

}