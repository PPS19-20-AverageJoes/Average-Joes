package averageJoes.model.machine

import averageJoes.model.fitness.ExecutionValues
import averageJoes.model.machine.FileWriterActor.{Msg, WriteOnFile}
import averageJoes.model.workout.MachineParameters
import averageJoes.utils.FileParser
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}


object FileWriterActor {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new FileWriterActor(context))

  sealed trait Msg
    final case class WriteOnFile(customerID: String, machineParameters: MachineParameters, executionValues: ExecutionValues) extends Msg
}

class FileWriterActor(context: ActorContext[FileWriterActor.Msg])
  extends AbstractBehavior[FileWriterActor.Msg](context) {
  def onMessage(msg: Msg): Behavior[Msg] = msg match {
    case WriteOnFile(customerID, parameters, executionValues) =>
      val path = "src/main/resources/workoutData.json"
      FileParser.encoding(path, WriteOnFile(customerID, parameters, executionValues))
      Behaviors.stopped
  }

}
