package averageJoes.model.fitness

import averageJoes.model.customer.CustomerActor
import averageJoes.model.customer.CustomerActor.{ExerciseCompleted, NextMachineBooking, TrainingCompleted}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}

import scala.concurrent.duration.FiniteDuration

/**
 *  CustomerExercising is an actor to keep track of the exercise duration
 *  and notify the customer whe exercise is completed.
 *  If the exercise was in training program, it will update the training program.
 */
object CustomerExercising {
  trait Msg
  private case object TimerKey
  final case object ExerciseTiming extends Msg
  private case object ExerciseFinished extends Msg

  def apply(target: ActorRef[CustomerActor.Msg], ex: (Option[Exercise], FiniteDuration), tp: TrainingProgram): Behavior[Msg] = {
    Behaviors.withTimers(timers => new CustomerExercising(timers, target, ex, tp).initializing())
  }
}


class CustomerExercising(timers: TimerScheduler[CustomerExercising.Msg],
                         target: ActorRef[CustomerActor.Msg],
                         ex: (Option[Exercise], FiniteDuration),
                         tp: TrainingProgram) {
  import CustomerExercising._

  private def initializing(): Behavior[CustomerExercising.Msg] = {
    Behaviors.receiveMessage[CustomerExercising.Msg] { message =>
      timers.startSingleTimer(TimerKey, ExerciseFinished, ex._2)
      exerciseFinished()
    }
  }

  def exerciseFinished(): Behavior[Msg] = {
    Behaviors.receiveMessage[Msg] {
      case ExerciseFinished =>
        if(ex._1.isDefined) target ! ExerciseCompleted(TrainingProgram(tp.customer)(tp.allExercises - ex._1.get))
        else target ! ExerciseCompleted(TrainingProgram(tp.customer)(tp.allExercises))

        Behaviors.stopped
    }
  }
}


/**
 * BookWhileExercising will keep track of the time when a customer
 * should start booking other machines because he is finishing his exercise.
 */
object BookWhileExercising {
  trait Msg
  case object BookTiming extends Msg
  private case object BookAnotherMachine extends Msg
  private case object TimerKey

  def apply(target: ActorRef[CustomerActor.Msg],  ex: (Option[Exercise], FiniteDuration), tp: TrainingProgram): Behavior[Msg] = {
    Behaviors.withTimers(timers => new BookWhileExercising(timers, target, ex, tp).initializing())
  }
}

class BookWhileExercising(timers: TimerScheduler[BookWhileExercising.Msg],
                          target: ActorRef[CustomerActor.Msg],
                          ex: (Option[Exercise], FiniteDuration),
                          tp: TrainingProgram) {
  import BookWhileExercising._
  import scala.concurrent.duration.DurationInt


  private def initializing(): Behavior[BookWhileExercising.Msg] = {
    Behaviors.receiveMessage[BookWhileExercising.Msg] { message =>
      timers.startSingleTimer(TimerKey, BookAnotherMachine, ex._2 - (30 seconds))
      waitUntilTimeout()
    }
  }

  def waitUntilTimeout(): Behavior[Msg] = {
    Behaviors.receiveMessage[Msg] {
      case BookAnotherMachine =>
        if(noExercisesLeft(tp)) {
          timers.cancel(TimerKey);
          target ! TrainingCompleted()
        }
        else {
          timers.cancel(TimerKey)
          target ! NextMachineBooking(ex._1.get)
        }
        Behaviors.stopped
      case _ => waitUntilTimeout()
    }
  }

  private def noExercisesLeft(tp: TrainingProgram): Boolean = tp.allExercises.isEmpty
}

