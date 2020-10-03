package AverageJoes.model.machine

import AverageJoes.common.{MsgNearDevice, MsgRfid}
import AverageJoes.model.user.{SmartGymUser, SmartGymUserImpl}
import AverageJoes.model.wristband.Wristband
import akka.actor.{ActorSystem, Props}
import org.junit.Test


class MachineTest extends App {

  @Test
  def testExamResultsBasicBehaviour() {
    val system = ActorSystem("mySystem")
    val userActor = system.actorOf(Props(new SmartGymUserImpl("","","","")), "actorUser")
    val machine = system.actorOf(Props(classOf[MachineActor], userActor), "machineActor")
    val watchActor = system.actorOf(Props[Wristband](),"wristbandActor")
    val physicalMachineActor = system.actorOf(Props(new LegPress(machine, " ")), "physicalActor")
    watchActor ! MsgNearDevice(physicalMachineActor)

    //machine ! MsgConstructor(userActor, "USER_LOG_IN")
  }

}
