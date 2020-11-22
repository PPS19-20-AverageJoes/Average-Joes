package AverageJoes.model.fitness

import AverageJoes.common.database.Workout
import AverageJoes.common.database.table.{CustomerImpl, Workout, WorkoutImpl}
import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.model.fitness.ExerciseExecutionConfig.ParameterExtractor
import AverageJoes.model.fitness.WorkoutConverter.Converter
import AverageJoes.model.hardware.PhysicalMachine.{ChestFlyParameters, CyclingMachineParameters, LegPressParameters, LiftingMachineParameters, RunningMachineParameters}
import AverageJoes.model.workout.MachineParameters
import AverageJoes.utils.SafePropertyValue.SafePropertyVal

import scala.collection.SortedSet

trait Exercise {
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ImplicitParameterExtractors._

    def order: Int
    def parameters: MachineParameters
    def executionParameters: Parameters[SafePropertyVal] = ParameterExtractor.extractParameters(parameters)
}

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
    import AverageJoes.common.MachineTypes._
    import AverageJoes.common.MachineTypeConverters._

    object Converters {
        implicit val converter: Converter[Workout] = {
            case WorkoutImpl(_, _, sets, timer, repetitions, incline, speed, weight, typeMachine, setForSec, _) => machineTypeOf(typeMachine) match {
                case RUNNING => RunningMachineParameters(incline, speed, timer)
                case LIFTING => LiftingMachineParameters(weight, sets, repetitions, setForSec)
                case CYCLING => CyclingMachineParameters(incline, timer)
                case LEG_PRESS => LegPressParameters(weight, sets, repetitions, setForSec)
                case CHEST_FLY => ChestFlyParameters(weight, sets, repetitions, setForSec)

                /** TODO: other machines to be added */
            }
        }
    }

    object Ordering {
        implicit val ordering: Ordering[Exercise] = (x: Exercise, y: Exercise) => x.order.compareTo(y.order)
    }
}

object Main extends App {
    import AverageJoes.utils.SafePropertyValue.NonNegative
    import AverageJoes.common.MachineTypeConverters._
    import AverageJoes.model.fitness.ImplicitExercise.Converters._
    import AverageJoes.model.fitness.ImplicitExercise.Ordering._

    val c1 = CustomerImpl("sokol", "guri", "sokol", "27/08/2020", "customer1" )

        import AverageJoes.model.fitness.ImplicitExercise._

    val exList = Workout.workoutStorage.getWorkoutForCustomer("Wristband1")
      .map(w => Exercise(w))


    val workoutSet: SortedSet[Exercise] = collection.SortedSet(exList: _*)



    val tp = TrainingProgram(c1)(workoutSet)
       print(tp.allExercises)

    //print(Exercise(WorkoutImpl("123", 10,10, 10, 10, 10, 10, 10, stringOf(RUNNING), 10, "10")))

}