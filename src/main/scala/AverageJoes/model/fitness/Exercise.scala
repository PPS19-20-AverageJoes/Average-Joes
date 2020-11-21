package AverageJoes.model.fitness

import AverageJoes.common.database.table.{Workout, WorkoutImpl}
import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.model.fitness.ExerciseExecutionConfig.ParameterExtractor
import AverageJoes.model.fitness.WorkoutConverter.Converter
import AverageJoes.model.hardware.PhysicalMachine.{ChestFlyParameters, CyclingMachineParameters, LegPressParameters, LiftingMachineParameters, RunningMachineParameters}
import AverageJoes.model.workout.MachineParameters
import AverageJoes.utils.SafePropertyValue.SafePropertyVal

trait Exercise {
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ImplicitParameterExtractors._

    def parameters: MachineParameters
    def executionParameters: Parameters[SafePropertyVal] = ParameterExtractor.extractParameters(parameters)
}

object Exercise{
    def apply(parameters: MachineParameters): Exercise = ExerciseImpl(parameters)
    def apply(w: Workout)(implicit workoutToExercise: Converter[Workout]): Exercise = ExerciseImpl(workoutToExercise.convert(w))

    private case class ExerciseImpl(parameters: MachineParameters) extends Exercise
}


object WorkoutConverter {
    trait Converter[W <: Workout] { def convert(w: W): MachineParameters}
    def workoutToParameters[W <: Workout](w: W)(implicit converter: Converter[W]): MachineParameters = converter.convert(w)
}


object ImplicitWorkoutConverters {
    import AverageJoes.common.database.table.Workout
    import AverageJoes.common.MachineTypes._
    import AverageJoes.common.MachineTypeConverters._

    implicit val converter: Converter[Workout] =  {
        case WorkoutImpl(customerID, sets, timer, repetitions, incline, speed, weight, typeMachine, setForSec, id) => machineTypeOf(typeMachine) match {
            case RUNNING => RunningMachineParameters(incline,speed, timer)
            case LIFTING => LiftingMachineParameters(weight, sets, repetitions, setForSec)
            case CYCLING => CyclingMachineParameters(incline, timer)
            case LEG_PRESS => LegPressParameters(weight,sets,repetitions,setForSec)
            case CHEST_FLY => ChestFlyParameters(weight,sets,repetitions,setForSec)
            /** TODO: other machines to be added */
        }
    }
}

object Main extends App {
    import AverageJoes.common.MachineTypes._
    import AverageJoes.common.MachineTypeConverters._
    import AverageJoes.model.fitness.ImplicitWorkoutConverters._

    print(Exercise(WorkoutImpl("123", 10, 10, 10, 10, 10, 10, stringOf(RUNNING), 10, "10")))

}