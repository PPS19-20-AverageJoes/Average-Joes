package AverageJoes.model.machine

import AverageJoes.common.MsgActorMessage._
import AverageJoes.common.{MsgActorMessage, ServerSearch}
import AverageJoes.controller.HardwareController
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, Behaviors}

sealed trait PhysicalMachine extends AbstractBehavior[PhysicalMachine.Msg]{
  val machineID: String //TODO: recuperare da configurazione su DB?
  val ma: ActorRef[MsgActorMessage] //MachineActor

  override def onMessage(msg: PhysicalMachine.Msg): Behavior[PhysicalMachine.Msg] = {
    msg match{
      case PhysicalMachine.MsgRfid(userID) => ma ! MsgUserLogin(userID); Behaviors.same
      case PhysicalMachine.MsgDisplay(message) => display(message); Behaviors.same
    }
  }

  def display (s: String): Unit
}



object PhysicalMachine {
  sealed trait Msg
  case class MsgRfid(userID: String) extends Msg //Rfid fired
  case class MsgDisplay(message: String) extends Msg

  sealed trait MsgDaemon
  case class MsgMachineActorStarted(refMA: ActorRef[MsgActorMessage]) extends MsgDaemon

  object PhMachineType{
    val legPress = 1
    val chestFly = 2
  }
  def apply(phMachineType:Int, ma: ActorRef[MsgActorMessage], machineID: String): PhysicalMachine = {
    phMachineType match{
        case PhMachineType.legPress => LegPress(ma, machineID)
        case PhMachineType.chestFly => ChestFly(ma, machineID)
    }
  }

  private case class ChestFly(ma: ActorRef[MsgActorMessage], machineID: String) extends PhysicalMachine{
    override def display(s: String): Unit = {
      val _display: String = machineID + " " + s
    }
  }
  private  case class LegPress(ma: ActorRef[MsgActorMessage], machineID: String) extends PhysicalMachine{
    override def display(s: String): Unit = {
      val _display: String = machineID + " " + s
    }
  }



  //Every PhysicalMachine need a daemon that tell the server of the starting up and retreive the actorref of the virtual Machine
  def startDaemon(actorRefFactory: ActorRefFactory, machineID: String, machineType: Class[_ <: PhysicalMachine]): Unit ={
    actorRefFactory.actorOf(Props(classOf[PMDaemon], machineID, machineType), machineID)
  }

  case class PMDaemon(machineID: String, phMachineType: Int) extends AbstractBehavior[MsgDaemon] with ServerSearch{
    server ! MsgPhysicalMachineWakeUp(machineID)

    override def onMessage(msg: MsgDaemon): Behavior[MsgDaemon] = {
      msg match{
        case MsgMachineActorStarted(refMA) => {
          val pm = startPhysicalMachine(context, machineID,machineType ,refMA)
          context.spawn[PhysicalMachine.Msg](PhysicalMachine(phMachineType,refMA,machineID),"name")
          refMA ! MsgPMActorStarted(machineID, pm)

          //ToDo: kill daemon
          Behaviors.same
        }
      }
    }


    override def receive: Receive = {
      case m: MsgMachineActorStarted => {
        val pm = startPhysicalMachine(context, machineID,machineType ,m.machine)
        m.machine ! MsgPMActorStarted(machineID, pm)
        context.parent ! MsgPMActorStarted(machineID, pm)
        //ToDo: kill daemon
      }

    }

    def startPhysicalMachine(actorRefFactory: ActorRefFactory, machineID: String, machineType: Class[_ <: PhysicalMachine], ma: ActorRef): ActorRef = {
      actorRefFactory.actorOf(Props(machineType, ma, machineID), machineID)
    }
  }

}