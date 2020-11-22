package AverageJoes.view

import AverageJoes.common.MachineTypes.MachineType
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import javax.swing.SwingUtilities

import scala.swing.{GridPanel, Swing}


sealed trait ViewToolActor extends AbstractBehavior[ViewToolActor.Msg] {
  createViewEntity()

  def createViewEntity()
  def updateViewEntity(mg: String)
}

object ViewToolActor {
  sealed trait Msg

  object Msg {
    final case class CreateViewObject() extends Msg
    final case class UpdateViewObject(msg: String) extends Msg
    final case class SetMachineParameters(list: List[(String,Int)]) extends Msg
    final case class ExerciseCompleted() extends Msg
  }

  class ViewDeviceActor(override val context: ActorContext[Msg], val deviceLabel: String,
                        actorRef: ActorRef[Device.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor  {
    var panel: Option[GridPanel] = Option.empty
    var machine: Option[UserGui] = Option.empty

    override def onMessage(msg: ViewToolActor.Msg): Behavior[ViewToolActor.Msg] = msg match {
      case m: ViewToolActor.Msg.UpdateViewObject => updateViewEntity(m.msg); Behaviors.same
    }

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
                                val machineType: MachineType,
                                actorRef: ActorRef[PhysicalMachine.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor {
     var panel: Option[MachineView] = Option.empty
     var machine: Option[MachineGUI] = Option.empty

     def createViewEntity(): Unit = {
      scala.swing.Swing.onEDT{
        panel = Option.apply(View._getMachineView())
        machine = Option.apply(MachineGUI(machineID+" - "+machineLabel, machineType, actorRef))
        panel.get.contents += machine.get
        panel.get.addEntry(machineID, actorRef)
      }
    }

    override def onMessage(msg: Msg): Behavior[Msg] = msg match {
      case m: ViewToolActor.Msg.UpdateViewObject => updateViewEntity(m.msg); Behaviors.same
      case m: Msg.SetMachineParameters => setMachineParameters(m.list); Behaviors.same
      case m: Msg.ExerciseCompleted =>  exerciseCompleted(); Behaviors.same
    }

    override def updateViewEntity(msg: String): Unit = {
      scala.swing.Swing.onEDT{
         machine.get.update(msg)
      }
    }


    def setMachineParameters(list: List[(String,Int)]): Unit = {
      scala.swing.Swing.onEDT{
        machine.get.setParameters(list)
      }
    }
    /** TODO: text field to be clean when exercise completed */
    def exerciseCompleted(): Unit = {
      scala.swing.Swing.onEDT{
        machine.get.setButton(true)
      }
    }

  }

  object ViewPhysicalMachineActor {
    def apply(machineID:String, machineLabel: MachineLabel, machineType: MachineType, actorRef: ActorRef[PhysicalMachine.Msg]): Behavior[Msg] =
      Behaviors.setup(context => new ViewPhysicalMachineActor(context, machineID, machineLabel,  machineType, actorRef))
  }

  object ViewDeviceActor {
    def apply(deviceLabel:String, actorRef: ActorRef[Device.Msg]): Behavior[Msg] =
      Behaviors.setup(context => new ViewDeviceActor(context, deviceLabel, actorRef))
  }
}
