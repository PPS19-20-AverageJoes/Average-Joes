package AverageJoes.model.fitness

import AverageJoes.model.customer.Customer

/**
 * TODO: 1. create TrainingProgram storage
 *       2. handle two training programs for the same customer
 *       3. should have a start & expiry date ?
 *       4. should add exercises by calling the method from TrainingProgram or
 *          through another object (TrainingProgramsManager?) ?
 */

trait TrainingProgram {
  val customer: Customer
  def addExercise(ex: Exercise): TrainingProgram
  def removeExercise(ex: Exercise): TrainingProgram
  def allExercises: Set[Exercise]
}

object TrainingProgram {

  def apply(customer: Customer): TrainingProgram = FitnessProgram(customer)

  private case class FitnessProgram(customer: Customer) extends TrainingProgram {
      var exercises: Set[Exercise] = Set()

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
