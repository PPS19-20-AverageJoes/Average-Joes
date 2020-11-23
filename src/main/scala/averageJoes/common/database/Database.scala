package averageJoes.common.database

import averageJoes.common.database.table.{CustomerImpl, WorkoutImpl}
import averageJoes.utils.FileParser


object Customer {
  var customerStorage: Storage[CustomerImpl] =
    FileParser.parsing("src/main/resources/customer.json", new GymStorage[CustomerImpl])

}

object Workout {
   var workoutStorage: StorageWorkout[WorkoutImpl] = new GymStorageWorkout[WorkoutImpl]
    FileParser.parsing("src/main/resources/workout.json", workoutStorage)
}
