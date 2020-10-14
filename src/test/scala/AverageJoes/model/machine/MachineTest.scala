package AverageJoes.model.machine

import AverageJoes.model.user.{SmartGymUser, SmartGymUserImpl}
import AverageJoes.model.device.Wristband
import akka.actor.{ActorSystem, Props}
import org.junit.Test


class MachineTest extends App {

  @Test
  def testExamResultsBasicBehaviour():Unit = {
    val system = ActorSystem("mySystem")
    val userActor = system.actorOf(Props(new SmartGymUserImpl("","","","")), "actorUser")
    val machine = system.actorOf(Props(classOf[MachineActor], userActor), "machineActor")
    val watchActor = system.actorOf(Props[Wristband](),"wristbandActor")
    val physicalMachineActor = system.actorOf(Props(new LegPress(machine, " ")), "physicalActor")

    //machine ! MsgConstructor(userActor, "USER_LOG_IN")
  }

}
