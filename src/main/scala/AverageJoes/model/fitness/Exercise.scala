package AverageJoes.model.fitness

import AverageJoes.common.database.Workout
import AverageJoes.common.database.table.{CustomerImpl, Workout, WorkoutImpl}
import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.model.fitness.ExerciseExecutionConfig.ParameterExtractor
import AverageJoes.model.fitness.WorkoutConverter.Converter
import AverageJoes.model.hardware.PhysicalMachine.{CyclingMachineParameters, LegPressParameters}
import AverageJoes.model.workout.{LiftMachineParameters, MachineParameters, RunningMachineParameters}
import AverageJoes.utils.SafePropertyValue.NonNegative.NonNegInt
import AverageJoes.utils.SafePropertyValue.SafePropertyVal

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


object ImplicitWorkoutConverters {
    import AverageJoes.common.database.table.Workout
    import AverageJoes.common.MachineTypes._
    import AverageJoes.common.MachineTypeConverters._

    implicit val converter: Converter[Workout] =  {
        case WorkoutImpl(_, _, sets, timer, repetitions, incline, speed, weight, typeMachine, setForSec, _) => machineTypeOf(typeMachine) match {
            case RUNNING => RunningMachineParameters(incline,speed, timer)
            case LIFTING => LiftMachineParameters(weight, sets, repetitions, setForSec)
            case CYCLING => CyclingMachineParameters(incline, timer)
            case LEG_PRESS => LegPressParameters(weight,incline,sets,repetitions,setForSec)
            /** TODO: other machines to be added */
        }
    }
}


object Main extends App {
    import AverageJoes.utils.SafePropertyValue.NonNegative
    import AverageJoes.common.MachineTypeConverters._
    import AverageJoes.model.fitness.ImplicitWorkoutConverters._

    val c1 = CustomerImpl("sokol", "guri", "sokol", "27/08/2020", "customer1" )

        import AverageJoes.model.fitness.ImplicitWorkoutConverters._

        val workoutSet = Workout.workoutStorage.getWorkoutForCustomer("Wristband1")
          .map(w => Exercise(w))
          .sortBy(e => e.order)((x: Int, y: Int) => x.compare(y))
          .toSet

        val tp = TrainingProgram(c1)(workoutSet)
       print(tp.allExercises)

    //print(Exercise(WorkoutImpl("123", 10,10, 10, 10, 10, 10, 10, stringOf(RUNNING), 10, "10")))

}