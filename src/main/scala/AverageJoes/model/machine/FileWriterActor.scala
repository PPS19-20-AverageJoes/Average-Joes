package AverageJoes.model.machine

import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter

import AverageJoes.model.fitness.ExecutionValues
import AverageJoes.model.machine.FileWriterActor.{Msg, WriteOnFile}
import AverageJoes.model.workout.MachineParameters
import AverageJoes.utils.FileParser
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}


object FileWriterActor {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new FileWriterActor(context))

  sealed trait Msg
    final case class WriteOnFile(customerID: String, machineParameters: MachineParameters, executionValues: ExecutionValues, date:String) extends Msg
}

class FileWriterActor(context: ActorContext[FileWriterActor.Msg])
  extends AbstractBehavior[FileWriterActor.Msg](context) {
  def onMessage(msg: Msg): Behavior[Msg] = msg match {
    case WriteOnFile(customerID, parameters, executionValues, date) =>
      val path = "src/main/resources/workoutData.json"
      FileParser.encoding(path, WriteOnFile(customerID, parameters, executionValues, date))
      Behaviors.stopped
  }

}
