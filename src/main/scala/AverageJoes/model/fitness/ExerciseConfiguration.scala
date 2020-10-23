package AverageJoes.model.fitness

object ExerciseConfiguration {
  import AverageJoes.utils.SafePropertyValue.NonNegative.NonNegInt
  import AverageJoes.utils.ExerciseUtils.ExerciseParameter

  trait Parameters {
    type Property = ExerciseParameter
    type PropValue = NonNegInt
    type ConfigValue = (Property, PropValue)

    def valueOf(prop: Property): Option[(Property, PropValue)]
    def addValueOf(cv: ConfigValue): Parameters
    def allParameters: Set[ConfigValue]
  }

  case class ExerciseParameters() extends Parameters {
    private var parameters: Set[ConfigValue] = Set()

    override def valueOf(prop: Property): Option[(Property, PropValue)] = parameters.find(p => p._1.equals(prop))
    override def addValueOf(cv: ConfigValue): Parameters = {if(valueOf(cv._1).isEmpty) parameters = parameters + cv; this}
    override def allParameters: Set[ConfigValue] = parameters
  }
}


/** Extractor used to return the configuration parameters of an exercise
 * Implicit mechanism used*/
object ParameterExtractor {
  import AverageJoes.model.fitness.ExerciseConfiguration._

  trait Extractor[E <: ExerciseExecution] {
    def extract(e: E): Parameters
  }

  def extractParameters[E <: ExerciseExecution](e: E)(implicit extractor: Extractor[E]): Parameters = {
    extractor.extract(e)
  }

  /**
   * ToDo: refactor to builder
   * ToDo: Mixin to not decorate parameter to DRY
   * ToDo: Type of tuple ->
   */
  import AverageJoes.model.fitness.ExerciseExecutionMetric._
  import AverageJoes.utils.ExerciseUtils.CONFIGURABLE_PARAMETERS._
  import AverageJoes.model.fitness.ExerciseExecutionEquipment.MACHINE_EQUIPMENT._

  object ImplicitParameterExtractors {
    implicit val basicExecutionParamExtractor: Extractor[ExerciseExecution] = (e: ExerciseExecution) => e.metric match {
      case WithTimer(sets, timer) => new ExerciseParameters()
        .addValueOf((SETS, sets))
        .addValueOf((TIMER, timer))
      case WithRepetitions(sets, repetitions) => new ExerciseParameters()
        .addValueOf((SETS, sets))
        .addValueOf((REPETITIONS, repetitions))
    }

    implicit val machineExecutionParamExtractor: Extractor[MachineExecution] = (e: MachineExecution) => e smartMachine() match {
      case LiftMachine(wight, sets) => new ExerciseParameters()
        .addValueOf((SETS, sets))
        .addValueOf((WIGHT, wight))
      case RunningMachine(incline, speed, timer) => new ExerciseParameters()
        .addValueOf((SPEED, speed))
        .addValueOf((TIMER, timer))
        .addValueOf((INCLINE, incline))
    }
  }
}

object Main extends App {
  import AverageJoes.model.fitness.ExerciseExecutionEquipment._
  import AverageJoes.model.fitness.ExerciseExecutionMetric._
  import AverageJoes.model.fitness.ExerciseConfiguration.Parameters

  val equipment: List[Equipment] = List(EQUIPMENT.BANDS, EQUIPMENT.BOSU, EQUIPMENT.DUMBBELLS)

  val metricRepetitions: ExecutionMetric = WithRepetitions(10, 10)
  val metricTimer: ExecutionMetric = WithTimer(3, 100)

  val basicExecution: ExerciseExecution =  BasicExerciseExecution(metricRepetitions, equipment)

  import AverageJoes.model.fitness.ParameterExtractor.ImplicitParameterExtractors._

  val param: Parameters = ParameterExtractor.extractParameters(basicExecution)
  println(param.allParameters)
}
