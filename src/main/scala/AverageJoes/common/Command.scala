package AverageJoes.common

import AverageJoes.model.workout.Exercise
import akka.actor.typed.ActorRef

sealed trait Command

object Command {


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

  final case class NotifiedByMachine(requestId: Long, replyTo: ActorRef[NotifyWristband]) extends Command
  final case class NotifyWristband(requestId: Long) extends Command

  final case class UserAlive(requestId: Long, replyTo: ActorRef[UserAliveSignal]) extends Command
  final case class UserAliveSignal(requestId: Long) extends Command

  case object Passivate extends Command
}

