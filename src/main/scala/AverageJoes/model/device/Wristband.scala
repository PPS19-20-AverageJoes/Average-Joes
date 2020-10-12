package AverageJoes.model.device

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.{ActorRef, ActorRefFactory, Props}

/**
 * AC
 * @param deviceID: ID of the device. In real devices, is stored on config files
 */
class Wristband(val deviceID: String) extends Device {

  def apply(): Behavior[Device.MsgDevice] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.whom)
    message.replyTo ! Greeted(message.whom, context.self)
    Behaviors.same
  }

  def display (s: String): Unit ={
    println(s)
  }

  def rfid(ref: ActorRef) : Unit ={
    ref ! MsgRfid(deviceID)
  }

  //noinspection SpellCheckingInspection
  //ToDo: è possibile uilizzare sia la receive della classe che quella della superclasse?
  override def receive: Receive = {
    case m: MsgUserLoggedInMachine => display(m.refMachineActor.toString()) //ToDo: va passato un id o similari
    case m: MsgNearDevice => rfid(m.device)
  }
}

object Wristband{
  def startWristband(actorRefFactory: ActorRefFactory, deviceID: String): ActorRef = {
    actorRefFactory.actorOf(Props(classOf[Wristband], deviceID), deviceID)
  }
}