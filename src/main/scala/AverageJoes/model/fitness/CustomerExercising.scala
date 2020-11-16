package AverageJoes.model.fitness

import AverageJoes.model.customer.CustomerActor
import AverageJoes.model.customer.CustomerActor.ExerciseCompleted
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}

import scala.concurrent.duration.FiniteDuration

object CustomerExercising {
  trait Msg
  private case object TimerKey
  private case object ExerciseFinished extends Msg

  def apply(target: ActorRef[CustomerActor.Msg], after: FiniteDuration, tp: TrainingProgram): Behavior[Msg] = {
    Behaviors.withTimers(timers => new CustomerExercising(timers, target, after, tp).initializing())
  }

}
class CustomerExercising(
                          timers: TimerScheduler[CustomerExercising.Msg],
                          target: ActorRef[CustomerActor.Msg],
                          after: FiniteDuration,
                          tp: TrainingProgram) {
  import CustomerExercising._

  private def initializing(): Behavior[CustomerExercising.Msg] = {
    Behaviors.receiveMessage[CustomerExercising.Msg] { message =>
      timers.startSingleTimer(TimerKey, ExerciseFinished, after)
      exerciseFinished()
    }
  }

  def exerciseFinished(): Behavior[Msg] = {
    Behaviors.receiveMessage[Msg] {
      case ExerciseFinished =>
        target ! ExerciseCompleted(tp)
        Behaviors.stopped
    }
  }
}