package AverageJoes.model.fitness

import AverageJoes.utils.ExerciseUtils._

trait Exercise {
    val description: String
    val musclesWorked: Set[Muscle]
    val level: Level
    val force: Force
    val exerciseExecution: ExerciseExecution
    def smartExecutionParam(exerciseExecution: ExerciseExecution): Option[Int] = None
}

object Exercise{
    def apply(description: String, musclesWorked: Set[Muscle], level: Level, force: Force, exerciseExecution: ExerciseExecution): Exercise
    =  ExerciseImpl(description, musclesWorked, level, force, exerciseExecution)

    def apply(description: String, musclesWorked: Set[Muscle], execution: ExerciseExecution): Exercise
    =  ExerciseImpl(description, musclesWorked, LEVEL.DEFAULT, FORCE.DEFAULT, execution)

    private case class ExerciseImpl(description: String, musclesWorked: Set[Muscle], level: Level, force: Force, exerciseExecution: ExerciseExecution)
      extends Exercise {
        //verride type Parameters = this.type

        override def smartExecutionParam(exerciseExecution: ExerciseExecution): Option[Int] = exerciseExecution match {
            case ee if ee.withoutMachineExecution => None
            case _ => smartExecutionParam(exerciseExecution)
        }
    }
}
