package AverageJoes.common.database

import AverageJoes.common.database.table.{CustomerImpl, Workout, WorkoutImpl}
import AverageJoes.utils.FileParser


object Customer {
  var customerStorage: Storage[CustomerImpl] =
    FileParser.parsing("src/main/resources/customer.json", new GymStorage[CustomerImpl])

}

object Workout {
   var workoutStorage: StorageWorkout[WorkoutImpl] = new GymStorageWorkout[WorkoutImpl]
    FileParser.parsing("src/main/resources/workout.json", workoutStorage)
}
