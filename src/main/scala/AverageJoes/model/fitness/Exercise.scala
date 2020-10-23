package AverageJoes.model.fitness

<<<<<<< 57dee68b993d9a31acc64c9c37d29566fd4cd992
import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters
import AverageJoes.model.fitness.ExerciseExecutionConfig.ParameterExtractor
import AverageJoes.model.fitness.MachineExecution.MachineEquipment


trait Exercise {
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ImplicitParameterExtractors._

    def equipment: MachineEquipment
    def executionParameters: Parameters = ParameterExtractor.extractParameters(equipment)
}

object Exercise{
    def apply(equipment: MachineEquipment): Exercise  =  ExerciseImpl(equipment)
    private case class ExerciseImpl(equipment: MachineEquipment) extends Exercise
}
