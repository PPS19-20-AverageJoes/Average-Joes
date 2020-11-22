package AverageJoes.model.fitness

import AverageJoes.common.database.table.Customer

import scala.collection.SortedSet


trait TrainingProgram {
  val customer: Customer
  def addExercise(ex: Exercise): TrainingProgram
  def removeExercise(ex: Exercise): TrainingProgram
  def allExercises: SortedSet[Exercise]
}

object TrainingProgram {

  def apply(customer: Customer)(exSet: SortedSet[Exercise]): TrainingProgram = FitnessProgram(customer)(exSet)

  private case class FitnessProgram(customer: Customer)(exSet: SortedSet[Exercise]) extends TrainingProgram {
      var exercises: SortedSet[Exercise] = exSet

      override def allExercises: SortedSet[Exercise] = exercises

      override def addExercise(ex: Exercise): TrainingProgram = {exercises = exercises + ex; this}

      override def removeExercise(ex: Exercise): TrainingProgram = {
        if (exercises.isEmpty) throw new NoExercisesFound;
        else  exercises = exercises - ex;
        this
      }
  }
}

class NoExercisesFound extends IllegalStateException
