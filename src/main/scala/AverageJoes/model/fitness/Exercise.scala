package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseConfiguration.Parameters
import AverageJoes.model.fitness.ExerciseExecutionEquipment.EQUIPMENT
import AverageJoes.model.fitness.ExerciseExecutionMetric.WithTimer
import AverageJoes.model.fitness.ParameterExtractor.Extractor
import AverageJoes.utils.ExerciseUtils._

trait Exercise {

    val description: String
    val musclesWorked: Set[Muscle]
    val level: Level
    val force: Force
    val exerciseExecution: ExerciseExecution

    def executionParameters(implicit paramExtractor: Extractor[ExerciseExecution]): Parameters =
        paramExtractor.extract(exerciseExecution)
}

object Exercise{
    def apply(description: String, musclesWorked: Set[Muscle], level: Level, force: Force, exerciseExecution: ExerciseExecution): Exercise
    =  ExerciseImpl(description, musclesWorked, level, force, exerciseExecution)

    def apply(description: String, musclesWorked: Set[Muscle], execution: ExerciseExecution): Exercise
    =  ExerciseImpl(description, musclesWorked, LEVEL.DEFAULT, FORCE.DEFAULT, execution)

    private case class ExerciseImpl(description: String, musclesWorked: Set[Muscle], level: Level, force: Force, exerciseExecution: ExerciseExecution)
      extends Exercise
}

object Main extends App {

    val exercise: Exercise = Exercise("first one",
        Set(MUSCLE.ABDOMINAL), LEVEL.DEFAULT, FORCE.DEFAULT,
        new BasicExerciseExecution(WithTimer(10,10), List(EQUIPMENT.BOSU, EQUIPMENT.DUMBBELLS))
    )

    import AverageJoes.model.fitness.ParameterExtractor.ImplicitParameterExtractors._
    println(exercise.executionParameters)
}