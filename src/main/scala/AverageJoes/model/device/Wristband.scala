package AverageJoes.model.device

import AverageJoes.model.machine.PhysicalMachine
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

/**
 * AC
 * @param deviceID: ID of the device. In real devices, is stored on config files
 */
class Wristband(context: ActorContext[Device.Msg], deviceID: String) extends AbstractBehavior[Device.Msg](context) with Device {

  def display (s: String): Unit = {
    println(s)
  }

  def rfid(ref: ActorRef[PhysicalMachine.Msg]) : Unit = {ref ! PhysicalMachine.Msg.Rfid(deviceID)}

}

object Wristband{
  def apply(deviceID: String): Behavior[Device.Msg] = Behaviors.setup(context => new Wristband(context, deviceID))
}