package AverageJoes.model.fitness

import AverageJoes.model.fitness.MachineExecution.MachineEquipment
import AverageJoes.utils.SafePropertyValue.SafePropertyVal

object ExerciseExecutionConfig {
  import AverageJoes.utils.ExerciseUtils.ExerciseParameters.ExerciseParameter

  object ExerciseConfiguration {

    trait Parameters[+T] {
      //type ConfigVal = (ExerciseParameter, T)

      def valueOf(prop: ExerciseParameter): Option[T]
      def addValueOf[R >: T](cv: (ExerciseParameter, R)): Parameters[R]
      def allParameters[R >: T]: List[(ExerciseParameter, R)]
    }

    object ExerciseParameters {

      def apply[T](params: List[(ExerciseParameter, T)]): Parameters[T] = new ExerciseParametersImpl(params)


      private class ExerciseParametersImpl[+T](parameters: List[(ExerciseParameter, T)]) extends Parameters[T] {
        //private var parameters: Set[ConfigVal] = Set()
        //type ConfigVal = (ExerciseParameter, T)

        override def valueOf(param: ExerciseParameter): Option[T] = parameters.find(p => p._1.equals(param)) match {
          case Some(value) => Some(value._2)
          case _ => None
        }

        override def addValueOf[R >: T](cv: (ExerciseParameter, R)): Parameters[R] =
          if (valueOf(cv._1).isEmpty) new ExerciseParametersImpl(cv :: parameters)
          else  new ExerciseParametersImpl(parameters.filter(p => (p._1!=cv._1)) :+ cv)

        override def allParameters[R >: T]: List[(ExerciseParameter, R)] = parameters

      }

    }

  }

  object ParameterExtractor {
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.Parameters

    trait Extractor[E <: MachineEquipment] {
      def extract(e: E): Parameters[SafePropertyVal]
    }

    def extractParameters[E <: MachineEquipment](e: E)(implicit extractor: Extractor[E]): Parameters[SafePropertyVal] =  extractor.extract(e)
  }


  object ImplicitParameterExtractors {
    import AverageJoes.utils.ExerciseUtils.ExerciseParameters._
    import AverageJoes.utils.ExerciseUtils.MachineTypes._
    import AverageJoes.model.fitness.MachineExecution.MACHINE_EQUIPMENT._
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ExerciseConfiguration.ExerciseParameters
    import AverageJoes.model.fitness.ExerciseExecutionConfig.ParameterExtractor.Extractor

    implicit val machineExecutionParamExtractor: Extractor[MachineEquipment] = {

      case LiftMachine(wight, sets) => ExerciseParameters[SafePropertyVal](List.empty[(ExerciseParameter, SafePropertyVal)])
        .addValueOf((TYPE, LIFTING))
        .addValueOf((SETS, sets))
        .addValueOf((WIGHT, wight))

      case RunningMachine(incline, speed, timer) => ExerciseParameters[SafePropertyVal](List.empty[(ExerciseParameter, SafePropertyVal)])
        .addValueOf((TYPE, RUNNING))
        .addValueOf((SPEED, speed))
        .addValueOf((TIMER, timer))
        .addValueOf((INCLINE, incline))

      /** other machines */
    }
  }

}
