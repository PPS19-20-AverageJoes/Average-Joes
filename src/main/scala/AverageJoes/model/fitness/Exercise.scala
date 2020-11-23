package AverageJoes.model.fitness

import AverageJoes.common.database.table.{Workout, WorkoutImpl}
import AverageJoes.model.fitness.WorkoutConverter.Converter
import AverageJoes.model.workout.MachineParameters

/**
 * Exercise Trait to represent an exercise of Training Program.
 * The exercises will be ordered in base of order parameter that
 * represents the exercise priority in the training session.
 *
 * Machine parameters represents the configurable parameters of a smart
 * machine used during the exercise execution.
 */
trait Exercise {
    def order: Int
    def parameters: MachineParameters
}

/**
 * Exercise can be instantiated using order and machine parameters.
 * It can be instantiated using Workout's of the gym data base. Other
 * than workout as parameter, it need also an implicit converter.
 */
object Exercise{
    def apply(order:Int, parameters: MachineParameters): Exercise = ExerciseImpl(order, parameters)
    def apply(w: Workout)(implicit workoutToExercise: Converter[Workout]): Exercise = ExerciseImpl(w.order, workoutToExercise.convert(w))

    private case class ExerciseImpl(order: Int, parameters: MachineParameters) extends Exercise
}


object WorkoutConverter {
    trait Converter[W <: Workout] { def convert(w: W): MachineParameters}
    def workoutToParameters[W <: Workout](w: W)(implicit converter: Converter[W]): MachineParameters = converter.convert(w)
}


object ImplicitExercise {
    import AverageJoes.common.database.table.Workout
    import AverageJoes.model.workout.MachineTypeConverters._

    /** Implicit exercise converter */
    object Converters {
        implicit val converter: Converter[Workout] = {
            case WorkoutImpl(_, _, sets, timer, repetitions, incline, speed, weight, typeMachine, setForSec, _) =>
                MachineParameters.inoculateParameters(machineTypeOf(typeMachine), sets, repetitions, setForSec, timer, weight, incline, speed)
        }
    }

    /** Implicit exercise ordering */
    object Ordering {
        implicit val ordering: Ordering[Exercise] = (x: Exercise, y: Exercise) => x.order.compareTo(y.order)
    }
}
