package AverageJoes.model.machine

import AverageJoes.model.machine.FileWriterActor.{Msg, WriteOnFile}
import AverageJoes.model.workout.MachineParameters
import AverageJoes.utils.FileParser
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}


object FileWriterActor {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new FileWriterActor(context))

  // message that Child can handle
  sealed trait Msg
    final case class WriteOnFile(customerID: String, machineParameters: MachineParameters) extends Msg
}

class FileWriterActor(context: ActorContext[FileWriterActor.Msg])
  extends AbstractBehavior[FileWriterActor.Msg](context) {

  def onMessage(msg: Msg): Behavior[Msg] = msg match {
    case WriteOnFile(customerID, parameters) =>
      val path = "src/main/resources/workoutData.json"
      FileParser.encoding(path, WriteOnFile(customerID, parameters))
      this
  }

}
