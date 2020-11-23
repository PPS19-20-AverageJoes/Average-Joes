package averageJoes.model.customer

import averageJoes.common.database.table._
import averageJoes.controller.GymController
import averageJoes.model.customer.CustomerActor.{CustomerTrainingProgram, ExerciseCompleted, NextMachineBooking, TrainingCompleted}
import averageJoes.model.customer.MachineBooker.BookMachine
import averageJoes.model.fitness.BookWhileExercising.BookTiming
import averageJoes.model.fitness.CustomerExercising.ExerciseTiming
import averageJoes.model.fitness.{BookWhileExercising, CustomerExercising, Exercise, TrainingProgram}
import averageJoes.model.hardware.{Device, PhysicalMachine}
import averageJoes.model.machine.MachineActor
import averageJoes.model.hardware.PhysicalMachine.RunningMachineParameters
import averageJoes.model.machine.MachineActor.Msg.BookingRequest
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef

import scala.collection.SortedSet
import scala.concurrent.duration._


class CustomerActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  import  averageJoes.model.fitness.ImplicitExercise.Ordering._

  val deviceProbe: TestProbe[Device.Msg] = createTestProbe[Device.Msg]()
  val machineProbe: TestProbe[MachineActor.Msg] = createTestProbe[MachineActor.Msg]()
  val managerProbe: TestProbe[CustomerManager.Msg] = createTestProbe[CustomerManager.Msg]()
  val phMachineProbe: TestProbe[PhysicalMachine.Msg] = createTestProbe[PhysicalMachine.Msg]()
  val gymControllerProbe: TestProbe[GymController.Msg] = createTestProbe[GymController.Msg]()

  val manager: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())
  val group: ActorRef[CustomerGroup.Msg] = spawn(CustomerGroup("group", manager))

  val c1: Customer = CustomerImpl("customer-1", "sokol", "guri", "20/05/2020", "customer-1")

  val tp: TrainingProgram = TrainingProgram(c1) (SortedSet(Exercise(1,RunningMachineParameters(speed = 10, incline = 20, minutes = 30)),
                                                            Exercise(2,RunningMachineParameters(speed = 11, incline = 21, minutes = 30))))

  "Customer actor" should {

    "receive it's training program when instantiated" in {
      val customerProbe: TestProbe[CustomerActor.Msg] = createTestProbe[CustomerActor.Msg]()

      customerProbe.ref ! CustomerTrainingProgram(tp, group)
      var customerReceived = customerProbe.receiveMessage()
      assert(customerReceived.isInstanceOf[CustomerTrainingProgram])
    }


    "start and complete exercising, not in training program" in {
      val customerProbe: TestProbe[CustomerActor.Msg] = createTestProbe[CustomerActor.Msg]()

      val exercising = spawn(CustomerExercising(customerProbe.ref, (Option.empty[Exercise], 4.seconds), tp))
      exercising ! ExerciseTiming
      customerProbe.expectMessage(5.seconds, ExerciseCompleted(tp))
    }

    "notify new booking during exercising" in {
      val customerProbe: TestProbe[CustomerActor.Msg] = createTestProbe[CustomerActor.Msg]()

      val book = spawn(BookWhileExercising(customerProbe.ref, (Option(Exercise(1, RunningMachineParameters(10, 20, 1))), 60 seconds), tp))
      book ! BookTiming
      customerProbe.expectMessage(32.seconds, NextMachineBooking(Exercise(1, RunningMachineParameters(10, 20, 1))))
    }

    "receive training completed for empty exercise set and exercise out-of-training-program" in {
      val customerProbe: TestProbe[CustomerActor.Msg] = createTestProbe[CustomerActor.Msg]()
      val tpWithOneExercise = TrainingProgram(c1)(SortedSet())

      spawn(BookWhileExercising(customerProbe.ref, (Option(Exercise(1, RunningMachineParameters(10, 20, 1))), 60 seconds), tpWithOneExercise)) ! BookTiming
      customerProbe.expectMessage(32.seconds, TrainingCompleted())
    }
  }



  val machine1: TestProbe[MachineActor.Msg] = createTestProbe[MachineActor.Msg]()
  val machine2: TestProbe[MachineActor.Msg] = createTestProbe[MachineActor.Msg]()
  val machine3: TestProbe[MachineActor.Msg] = createTestProbe[MachineActor.Msg]()

  "Machine booker actor" should {

    "receives a machine list" in {
      val mBookerProbe = createTestProbe[MachineBooker.Msg]()
      mBookerProbe.ref ! BookMachine(Set(machine1.ref, machine2.ref, machine3.ref))

      val bookingProbe = mBookerProbe.receiveMessage

      assert(bookingProbe.isInstanceOf[BookMachine])
      bookingProbe match { case list: BookMachine => assert(list.machines.equals(Set(machine1.ref, machine2.ref, machine3.ref))) }
    }

    "start booking the machines" in {
      val customerProbe: TestProbe[CustomerActor.Msg] = createTestProbe[CustomerActor.Msg]()
      val booker = spawn(MachineBooker(customerProbe.ref, "customer"))

      booker ! BookMachine(Set(machine1.ref, machine2.ref, machine3.ref))
      machine1.expectMessageType[BookingRequest]
      machine2.expectMessageType[BookingRequest]
      machine3.expectMessageType[BookingRequest]
    }

  }
}