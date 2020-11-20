package AverageJoes.model.fitness

import AverageJoes.common.database.table.Customer


trait TrainingProgram {
  val customer: Customer
  def addExercise(ex: Exercise): TrainingProgram
  def removeExercise(ex: Exercise): TrainingProgram
  def allExercises: Set[Exercise]
}

object TrainingProgram {

  def apply(customer: Customer)(exSet: Set[Exercise]): TrainingProgram = FitnessProgram(customer)(exSet)

  private case class FitnessProgram(customer: Customer)(exSet: Set[Exercise]) extends TrainingProgram {
      var exercises: Set[Exercise] = exSet

      override def allExercises: Set[Exercise] = exercises

      override def addExercise(ex: Exercise): TrainingProgram = {exercises = exercises + ex; this}

      override def removeExercise(ex: Exercise): TrainingProgram = {
        if (exercises.isEmpty) throw new NoExercisesFound;
        else  exercises = exercises - ex;
        this
      }
  }
}

class NoExercisesFound extends IllegalStateException
