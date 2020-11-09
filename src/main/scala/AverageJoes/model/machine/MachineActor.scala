package AverageJoes.model.machine

import AverageJoes.common.{LogOnMessage, LoggableMsg}
import AverageJoes.controller.GymController
import AverageJoes.model.customer.CustomerManager
import AverageJoes.model.machine.MachineActor._
import AverageJoes.model.workout.MachineParameters
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
/**
 * Machine actor class
 * controller: controller ActorRef
 */
object MachineActor{
  def apply(controller: ActorRef[GymController.Msg], physicalMachine: ActorRef[PhysicalMachine.Msg], machineType: PhysicalMachine.MachineType.Type): Behavior[Msg] =
    Behaviors.setup(context => new MachineActor(context, controller, physicalMachine, machineType))

  sealed trait Msg extends LoggableMsg
  object Msg {
    final case class PMActorStarted(replyTo: ActorRef[PhysicalMachine.Msg]) extends Msg
    final case class UserLogIn(customerID: String) extends Msg
    final case class UserMachineWorkoutPlan(customerID: String) extends Msg
    final case class UserMachineWorkout(customerID: String, exercise: Class[_ <: MachineParameters]) extends Msg
    final case class deadDevice() extends Msg
    final case class BookingRequest(replyTo: ActorRef[CustomerManager.Command], customerID: String) extends Msg
  }
}

class MachineActor(context: ActorContext[Msg], controller: ActorRef[GymController.Msg], physicalMachine: ActorRef[PhysicalMachine.Msg],
                   machineType: PhysicalMachine.MachineType.Type) extends AbstractBehavior[Msg](context) with LogOnMessage[Msg]{

  var bookedCustomer: Option[String] = Option.empty


  override def onMessageLogged(msg: Msg): Behavior[Msg] = {
      idle()
  }

  private def idle(): Behavior[Msg] = {
    Behaviors.receiveMessage {
      case Msg.UserLogIn(customerID) =>
        controller ! GymController.Msg.UserLogin(customerID, context.self)
        connecting()

      case Msg.BookingRequest(replyTo, customerID) =>
        bookedCustomer = Option.apply(customerID)
        //replyTo ! CustomerManager.BookingConfirmation(boolean)
        //da aggiungereMachineType
       bookedStatus()
    }
  }

  /**
   * receive machine parameters and let the physical machine know about them
   * check if the user is still connected
   * @return
   */
  private def connecting(): Behavior[Msg] = {
    ///risposta dal customer manager (customer id, parameteri, isLogged) --> cUSTUMER LOGGING
    //se false torno in idle
    //phymachine(parametri, customerID)
    Behaviors.receiveMessage {
     case CustomerManager.Logging(customerID, machineParameters, isLogged) =>
      if(!isLogged){
         idle()
      } else {
        physicalMachine ! Msg.UserMachineWorkoutPlan(customerID)
        updateAndLogOut()
      }

    }
  }

  //userlogin()--> sloggato => chiedo i parametri di uscita alla physicalmachine
  //spawn sotto attore che scrive su disco
  //deaddevice() --> idle => chiedo i parametri
  //spawn sotto attore che scrive su disco
  private def updateAndLogOut(): Behavior[Msg] = {

    Behaviors.receiveMessage {
      case Msg.UserLogIn(customerID) =>
        physicalMachine ! Msg.UserMachineWorkoutPlan(customerID)
      case Msg.UserMachineWorkout(customerID, exercise) =>
        //spawn su disco
        updateAndLogOut()
      case Msg.UserMachineWorkout(customerID, exercise) =>
        //spawn su disco
        idle()
    }
  }

  private def bookedStatus(): Behavior[Msg]= {
    Behaviors.receiveMessage{
          //verificare che i custumer id coincida con quello bookato
      case Msg.UserLogIn(customerID) =>
        if(bookedCustomer.get().equals(customerID))
        controller ! GymController.Msg.UserLogin(customerID, context.self)
        connecting()
    }
  }

  override val logName: String = "Machine Actor"
  override val loggingContext: ActorContext[Msg] = this.context
}