package AverageJoes.model.fitness

import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.{ExerciseParameters, Parameters}
import AverageJoes.model.fitness.ExerciseExecutionConfig.ParameterExtractor.Extractor
import AverageJoes.model.fitness.MachineExecution.MachineEquipment
import AverageJoes.utils.ExerciseUtils.MACHINE_TYPE
import AverageJoes.utils.SafePropertyValue.SafePropertyVal

object ExerciseExecutionConfig {

  object ExerciseConfiguration {
    import AverageJoes.utils.ExerciseUtils.ExerciseParameter

    trait Parameters {
      type Property = ExerciseParameter
      type PropValue = SafePropertyVal
      type ConfigValue = (Property, PropValue)

      def valueOf(prop: Property): Option[(Property, PropValue)]
      def addValueOf(cv: ConfigValue): Parameters
      def allParameters: Set[ConfigValue]
    }

    case class ExerciseParameters() extends Parameters {
      private var parameters: Set[ConfigValue] = Set()

      override def valueOf(prop: Property): Option[ConfigValue] = parameters.find(p => p._1.equals(prop))

      override def addValueOf(cv: ConfigValue): Parameters = {
        if (valueOf(cv._1).isEmpty) parameters = parameters + cv
        else removeOldParam(cv._1); parameters = parameters + cv
        this
      }

      def removeOldParam(prop: Property): Unit =
        if (valueOf(prop).isDefined) parameters = parameters - valueOf(prop).get

      override def allParameters: Set[ConfigValue] = parameters

    }

  }

  object ParameterExtractor {
    trait Extractor[E <: MachineEquipment] {
      def extract(e: E): Parameters
    }

    def extractParameters[E <: MachineEquipment](e: E)(implicit extractor: Extractor[E]): Parameters = {
      extractor.extract(e)
    }
  }


  object ImplicitParameterExtractors {

    import AverageJoes.utils.ExerciseUtils.CONFIGURABLE_PARAMETERS._
    import AverageJoes.model.fitness.MachineExecution.MACHINE_EQUIPMENT._

    /**
     * TODO: Use uniques enum for machine type
     */
    implicit val machineExecutionParamExtractor: Extractor[MachineEquipment] = {
      case LiftMachine(wight, sets) => ExerciseParameters()
        .addValueOf((SETS, sets))
        .addValueOf((WIGHT, wight))
      case RunningMachine(incline, speed, timer) => ExerciseParameters()
        .addValueOf((TYPE, MACHINE_TYPE.RUNNING))
        .addValueOf((SPEED, speed))
        .addValueOf((TIMER, timer))
        .addValueOf((INCLINE, incline))

      /** other machines */
    }
  }


}