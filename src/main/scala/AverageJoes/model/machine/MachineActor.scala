package AverageJoes.model.machine

import java.util.Optional
import AverageJoes.model.machine.MachineActor.{BookingStatus, Command, MachineBooking, PMActorStarted, UserLogIn, UserLogInStatus, UserLogOut, UserLoggedInMachine, UserMachineWorkoutCompleted, UserMachineWorkoutPlan, UserRef}
import AverageJoes.model.workout.Exercise
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
/**
 * Machine actor class
 * controller: controller ActorRef
 */
object MachineActor{
  def apply(controller: ActorRef[Command], machineType: Class[_ <: PhysicalMachine]): Behavior[Command] =
    Behaviors.setup(context => new MachineActor(context, controller, machineType))

  sealed trait Command

  final case class PMActorStarted(replyTo: ActorRef[Command]) extends Command
  final case class UserLogIn(userID: String) extends Command
  final case class UserLogInStatus(status: Boolean) extends Command
  final case class MachineBooking(userID: String, replyTo: ActorRef[BookingStatus]) extends Command
  final case class BookingStatus(status: Boolean) extends Command
  final case class UserRef(replyTo: ActorRef[UserLoggedInMachine]) extends Command
  final case class UserLoggedInMachine() extends Command
  final case class UserMachineWorkoutPlan (userID: String, exercise: Class[_ <: Exercise]) extends Command
  final case class UserMachineWorkoutCompleted (user: ActorRef[Command], exercise: Class[_ <: Exercise]) extends Command
  final case class UserLogOut() extends Command
}

class MachineActor(context: ActorContext[Command], controller: ActorRef[Command], //da rendere actor ref
                   machineType:Class[_ <: PhysicalMachine]) extends AbstractBehavior[Command](context) {

  var booked: (Boolean, String) = (false, "")
  var physicalMachine: Optional[ActorRef[Command]] = Optional.empty()

  override def onMessage(msg: Command): Behavior[Command] = msg match {
    case PMActorStarted(replyTo) => physicalMachine = Optional.of(replyTo)
      this

    case UserLogIn(userID) =>
      availabilityCheck(userID)
      this

    case MachineBooking(userID, replyTo) =>
      if (booked._1) {
        booked = (true, userID)
      }
      replyTo ! BookingStatus(booked._1)
      this

    case UserRef(replyTo) =>
      replyTo ! UserLoggedInMachine()
      this

    case UserMachineWorkoutPlan(userID, exercise) =>
      controller ! UserMachineWorkoutPlan(userID, exercise)
      this

    case UserMachineWorkoutCompleted (user, exercise) =>
      controller !  UserMachineWorkoutCompleted(user, exercise)
      user ! UserLogOut()
      this

  }

  def availabilityCheck(userId: String): Unit = {
    if (!booked._1 || (booked._1 && booked._2.equals(userId))) {
      booked = (false,"")
      controller ! UserLogInStatus(booked._1)
    } else {
      controller ! UserLogInStatus(booked._1)
    }
  }
}