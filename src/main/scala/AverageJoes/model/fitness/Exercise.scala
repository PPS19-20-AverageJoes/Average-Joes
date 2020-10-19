package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseExecution.{EQUIPMENT, Equipment, MACHINE_EQUIPMENT}
import AverageJoes.utils.ExerciseUtils.{FORCE, Force, LEVEL, Level, MUSCLE, Muscle}

/**
 * TODO class to be refactored
 */

trait Exercise[E<:Equipment] {
    val description: String
    val musclesWorked: Set[Muscle]
    val level: Level
    val force: Force
    val execution: ExerciseExecution[E]
}

object Exercise{
    def apply(description: String, musclesWorked: Set[Muscle], level: Level, force: Force, execution: ExerciseExecution[Equipment]): Exercise[Equipment]
        =  ExerciseImpl(description, musclesWorked, level, force, execution)

    private case class ExerciseImpl(description: String, musclesWorked: Set[Muscle], level: Level,
                                    force: Force, execution: ExerciseExecution[Equipment]) extends Exercise[Equipment]
}


object Main extends App{
    val ex: Exercise[Equipment] =  Exercise("first exercise", Set(MUSCLE.ABDOMINAL, MUSCLE.BICEPS), LEVEL.ADVANCED,
        FORCE.PULL, BasicExerciseExecution(List(EQUIPMENT.BANDS, EQUIPMENT.DUMBBELLS, MACHINE_EQUIPMENT.CyclingMachine(5,6))))
    println(ex)

}
