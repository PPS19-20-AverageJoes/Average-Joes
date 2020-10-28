package AverageJoes.view


import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.swing.GridPanel


sealed trait ViewToolActor extends AbstractBehavior[ViewToolActor.Msg] {
  val panel: GridPanel
  val machineID: String
  var machine: GridPanel

  override def onMessage(msg: ViewToolActor.Msg): Behavior[ViewToolActor.Msg] = msg match {
    case m: ViewToolActor.Msg.CreateViewObject => createViewEntity(); Behaviors.same

  }
  def createViewEntity()
}

object ViewToolActor {
  sealed trait Msg

  object Msg {
    final case class CreateViewObject() extends Msg

  }

  case class ViewDeviceActor(override val context: ActorContext[Msg],
                             override val panel: GridPanel, override val machineID: String)
    extends AbstractBehavior[Msg](context) with ViewToolActor  {
    override var machine: GridPanel = MachineGUI(machineID)

    override def createViewEntity(): Unit = {
      panel.contents += machine
    }
  }

  case class ViewPhysicalMachineActor(override val context: ActorContext[Msg],
                                      override val panel: GridPanel, override val machineID: String)
    extends AbstractBehavior[Msg](context) with ViewToolActor {
    override var machine: GridPanel = UserGui()
    override def createViewEntity(): Unit = {

    }

  }
}
