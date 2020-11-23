package averageJoes.model.fitness


import averageJoes.model.hardware.PhysicalMachine.{LiftingMachineParameters, RunningMachineParameters}
import averageJoes.model.workout.MachineParameters
import averageJoes.utils.SafePropertyValue.SafePropertyVal

@Deprecated
object ExerciseExecutionConfig {
  import averageJoes.model.workout.ExerciseParameters.ExerciseParameter
  import averageJoes.model.workout.MachineTypes.MachineType

  @Deprecated
  object ExerciseConfiguration {

    trait Parameters[T <: SafePropertyVal] {
      val typeParams: MachineType
      def valueOf(prop: ExerciseParameter): Option[T]
      def addValueOf(cv: (ExerciseParameter, T)): Parameters[T]
      def allParameters: List[(ExerciseParameter, T)]
    }

    @Deprecated
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
    import averageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters

    trait Extractor[E <: MachineParameters] {
      def extract(e: E): Parameters[SafePropertyVal]
    }

    def extractParameters[E <: MachineParameters](e: E)(implicit extractor: Extractor[E]): Parameters[SafePropertyVal] = extractor.extract(e)
  }


  object ImplicitParameterExtractors {
    import averageJoes.model.workout.ExerciseParameters._
    import averageJoes.model.workout._
    import MachineTypes._
    import averageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.ExerciseParameters
    import averageJoes.model.fitness.ExerciseExecutionConfig.ParameterExtractor.Extractor

    implicit val machineExecutionParamExtractor: Extractor[MachineParameters] = {

      case liftMachine @ LiftingMachineParameters(wight, sets, rep, secForSet) => ExerciseParameters[SafePropertyVal](LIFTING, List.empty[(ExerciseParameter, SafePropertyVal)])
          .addValueOf((SETS, sets))
          .addValueOf((WEIGHT, wight))
          .addValueOf((REPETITIONS, rep))
          .addValueOf(SET_DURATION, secForSet)
          .addValueOf((DURATION, liftMachine.duration))

      case runningMachine @ RunningMachineParameters(incline, speed, time) => ExerciseParameters[SafePropertyVal](RUNNING, List.empty[(ExerciseParameter, SafePropertyVal)])
          .addValueOf((SPEED, speed))
          .addValueOf((TIMER, time))
          .addValueOf((INCLINE, incline))
          .addValueOf((DURATION, runningMachine.duration))


      /** other machines */
    }
  }
}
