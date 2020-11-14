package AverageJoes.model.fitness


import AverageJoes.model.fitness.MachineExecution.MachineEquipment
import AverageJoes.utils.SafePropertyValue.SafePropertyVal

object ExerciseExecutionConfig {
  import AverageJoes.utils.ExerciseUtils.ExerciseParameters.ExerciseParameter
  import AverageJoes.common.MachineTypes.MachineType

  object ExerciseConfiguration {

    trait Parameters[T <: SafePropertyVal] {
      val typeParams: MachineType
      def valueOf(prop: ExerciseParameter): Option[T]
      def addValueOf(cv: (ExerciseParameter, T)): Parameters[T]
      def allParameters: List[(ExerciseParameter, T)]
    }

    object ExerciseParameters {

      def apply[T<: SafePropertyVal](maType: MachineType, params: List[(ExerciseParameter, T)]): Parameters[T] = new ExerciseParametersImpl(maType, params)

      private class ExerciseParametersImpl[T<: SafePropertyVal](override val typeParams: MachineType,
                                              parameters: List[(ExerciseParameter, T)]) extends Parameters[T] {

        override def valueOf(param: ExerciseParameter): Option[T] = parameters.find(p => p._1.equals(param)) match {
          case Some(value) => Option.apply(value._2)
          case _ => Option.empty
        }

        override def addValueOf(cv: (ExerciseParameter, T)): Parameters[T] =
          if (valueOf(cv._1).isEmpty) new ExerciseParametersImpl(typeParams, cv :: parameters)
          else new ExerciseParametersImpl(typeParams, parameters.filter(p => p._1 != cv._1) :+ cv)

        override def allParameters: List[(ExerciseParameter, T)] = parameters

      }
    }
  }

  object ParameterExtractor {
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters

    trait Extractor[E <: MachineEquipment] {
      def extract(e: E): Parameters[SafePropertyVal]
    }

    def extractParameters[E <: MachineEquipment](e: E)(implicit extractor: Extractor[E]): Parameters[SafePropertyVal] = extractor.extract(e)
  }


  object ImplicitParameterExtractors {
    import AverageJoes.utils.ExerciseUtils.ExerciseParameters._
    import AverageJoes.model.fitness.MachineExecution.MACHINE_EQUIPMENT._
    import AverageJoes.common.MachineTypes._
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.ExerciseParameters
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ParameterExtractor.Extractor

    implicit val machineExecutionParamExtractor: Extractor[MachineEquipment] = {

      case LiftMachine(wight, sets) => ExerciseParameters[SafePropertyVal](LIFTING, List.empty[(ExerciseParameter, SafePropertyVal)])
          .addValueOf((SETS, sets))
          .addValueOf((WIGHT, wight))

      case RunningMachine(incline, speed, timer) => ExerciseParameters[SafePropertyVal](RUNNING, List.empty[(ExerciseParameter, SafePropertyVal)])
          .addValueOf((SPEED, speed))
          .addValueOf((TIMER, timer))
          .addValueOf((INCLINE, incline))

      /** other machines */
    }
  }
}
