package AverageJoes.view

import AverageJoes.model.hardware.{Device, PhysicalMachine}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import javax.swing.SwingUtilities

import scala.swing.{GridPanel, Swing}


sealed trait ViewToolActor extends AbstractBehavior[ViewToolActor.Msg] {
  val machineID: String
  createViewEntity()

  override def onMessage(msg: ViewToolActor.Msg): Behavior[ViewToolActor.Msg] = msg match {
    case m: ViewToolActor.Msg.UpdateViewObject => updateViewEntity(m.msg); Behaviors.same
  }
  def createViewEntity()
  def updateViewEntity(mg: String)
}

object ViewToolActor {
  sealed trait Msg

  object Msg {
    final case class CreateViewObject() extends Msg
    final case class UpdateViewObject(msg: String) extends Msg
  }

  class ViewDeviceActor(override val context: ActorContext[Msg], override val machineID: String,
                        actorRef: ActorRef[Device.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor  {
    var panel: Option[GridPanel] = Option.empty
    var machine: Option[UserGui] = Option.empty

    def createViewEntity(): Unit = {
      scala.swing.Swing.onEDT{
        panel = Option.apply(View._getUserView())
        machine = Option.apply(UserGui(actorRef: ActorRef[Device.Msg]))
        panel.get.contents += machine.get
      }
    }

    override def updateViewEntity(msg: String): Unit = {
      scala.swing.Swing.onEDT{
        machine.get.update(msg)
      }
    }
  }

  class ViewPhysicalMachineActor(override val context: ActorContext[Msg],
                                 override val machineID: String,
                                 actorRef: ActorRef[PhysicalMachine.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor {
     var panel: Option[MachineView] = Option.empty
     var machine: Option[MachineGUI] = Option.empty

     def createViewEntity(): Unit = {
      scala.swing.Swing.onEDT{
        panel = Option.apply(View._getMachineView())
        machine = Option.apply(MachineGUI())
        panel.get.contents += machine.get
        panel.get.addEntry(machineID, actorRef)
      }
    }

    override def updateViewEntity(msg: String): Unit = {
      scala.swing.Swing.onEDT{
         machine.get.update(msg)
      }
    }
  }

  object ViewPhysicalMachineActor {
    def apply(machineID:String, actorRef: ActorRef[PhysicalMachine.Msg]): Behavior[Msg] =
      Behaviors.setup(context => new ViewPhysicalMachineActor(context, machineID, actorRef))
  }

  object ViewDeviceActor {
    def apply(machineID:String, actorRef: ActorRef[Device.Msg]): Behavior[Msg] =
      Behaviors.setup(context => new ViewDeviceActor(context, machineID, actorRef))
  }
}
