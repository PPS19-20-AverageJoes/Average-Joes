package AverageJoes.model.machine

import java.util.Optional

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
  def apply(controller: ActorRef[GymController.Msg], machineType: PhysicalMachine.MachineType.Type): Behavior[Msg] =
    Behaviors.setup(context => new MachineActor(context, controller, machineType))

  sealed trait Msg extends LoggableMsg
  object Msg {
    final case class PMActorStarted(replyTo: ActorRef[PhysicalMachine.Msg]) extends Msg
    final case class UserLogIn(userID: String) extends Msg
    final case class UserLogInStatus(status: Boolean) extends Msg
    final case class MachineBooking(userID: String, replyTo: ActorRef[BookingStatus]) extends Msg
    final case class BookingStatus(status: Boolean) extends Msg
    final case class UserRef(replyTo: ActorRef[UserLoggedInMachine]) extends Msg
    final case class UserLoggedInMachine() extends Msg
    final case class UserMachineWorkoutPlan(userID: String, exercise: Class[_ <: MachineParameters]) extends Msg
    final case class UserMachineWorkoutCompleted(user: ActorRef[Msg], exercise: Class[_ <: MachineParameters]) extends Msg
    final case class UserLogOut() extends Msg

    final case class BookingRequest(replyTo: ActorRef[CustomerManager.Command]) extends Msg
  }
}

class MachineActor(context: ActorContext[Msg], controller: ActorRef[GymController.Msg], //da rendere actor ref
                   machineType: PhysicalMachine.MachineType.Type) extends AbstractBehavior[Msg](context) with LogOnMessage[Msg]{

  var booked: (Boolean, String) = (false, "")
  var physicalMachine: Optional[ActorRef[PhysicalMachine.Msg]] = Optional.empty()

  override def onMessageLogged(msg: Msg): Behavior[Msg] = msg match {
    case Msg.PMActorStarted(replyTo) => physicalMachine = Optional.of(replyTo)
      this

    case Msg.UserLogIn(userID) =>
      availabilityCheck(userID)
      this

    case Msg.MachineBooking(userID, replyTo) =>
      if (booked._1) {
        booked = (true, userID)
      }
      replyTo ! Msg.BookingStatus(booked._1)
      this

    case Msg.UserRef(replyTo) =>
      replyTo ! Msg.UserLoggedInMachine()
      this

    case Msg.UserMachineWorkoutPlan(userID, exercise) =>
      controller ! GymController.Msg.UserMachineWorkoutPlan(userID, exercise)
      this

    case Msg.UserMachineWorkoutCompleted (user, exercise) =>
      controller !  GymController.Msg.UserMachineWorkoutCompleted(user, exercise)
      user ! Msg.UserLogOut()
      this

  }

  def availabilityCheck(userId: String): Unit = {
    if (!booked._1 || (booked._1 && booked._2.equals(userId))) {
      booked = (false,"")
      controller ! GymController.Msg.UserLogInStatus(booked._1)
    } else {
      controller ! GymController.Msg.UserLogInStatus(booked._1)
    }
  }

  override val logName: String = "Machine Actor"
  override val loggingContext: ActorContext[Msg] = this.context
}