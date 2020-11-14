package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.model.fitness.ExerciseExecutionConfig.ParameterExtractor
import AverageJoes.model.fitness.MachineExecution.MachineEquipment
import AverageJoes.utils.SafePropertyValue.SafePropertyVal

trait Exercise {
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ImplicitParameterExtractors._

    /**
     * TODO: durata di un esercizio
     */

    def equipment: MachineEquipment
    def executionParameters: Parameters[SafePropertyVal] = ParameterExtractor.extractParameters(equipment)
}

object Exercise{
    def apply(equipment: MachineEquipment): Exercise  =  ExerciseImpl(equipment)
    private case class ExerciseImpl(equipment: MachineEquipment) extends Exercise
}
