package AverageJoes.view

import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import javax.swing.SwingUtilities

import scala.swing.{GridPanel, Swing}


sealed trait ViewToolActor extends AbstractBehavior[ViewToolActor.Msg] {
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

  class ViewDeviceActor(override val context: ActorContext[Msg], val deviceLabel: String,
                        actorRef: ActorRef[Device.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor  {
    var panel: Option[GridPanel] = Option.empty
    var machine: Option[UserGui] = Option.empty

    def createViewEntity(): Unit = {
      scala.swing.Swing.onEDT{
        panel = Option.apply(View._getUserView())
        machine = Option.apply(UserGui(actorRef: ActorRef[Device.Msg], deviceLabel))
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
                                val machineID: String,
                                val machineLabel: MachineLabel,
                                actorRef: ActorRef[PhysicalMachine.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor {
     var panel: Option[MachineView] = Option.empty
     var machine: Option[MachineGUI] = Option.empty

     def createViewEntity(): Unit = {
      scala.swing.Swing.onEDT{
        panel = Option.apply(View._getMachineView())
        machine = Option.apply(MachineGUI(machineID+" - "+machineLabel))
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
    def apply(machineID:String, machineLabel: MachineLabel, actorRef: ActorRef[PhysicalMachine.Msg]): Behavior[Msg] =
      Behaviors.setup(context => new ViewPhysicalMachineActor(context, machineID, machineLabel, actorRef))
  }

  object ViewDeviceActor {
    def apply(deviceLabel:String, actorRef: ActorRef[Device.Msg]): Behavior[Msg] =
      Behaviors.setup(context => new ViewDeviceActor(context, deviceLabel, actorRef))
  }
}
