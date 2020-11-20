package AverageJoes.model.customer

import AverageJoes.controller.GymController
import AverageJoes.model.customer.CustomerActor.{CustomerTrainingProgram, ExerciseCompleted,NextMachineBooking, TrainingCompleted}
import AverageJoes.model.customer.CustomerGroup.CustomerReady
import AverageJoes.model.fitness.BookWhileExercising.BookTiming
import AverageJoes.model.fitness.CustomerExercising.ExerciseTiming
import AverageJoes.model.fitness.MachineExecution.MACHINE_EQUIPMENT.RunningMachine
import AverageJoes.model.fitness.{BookWhileExercising, CustomerExercising, Exercise, TrainingProgram}
import AverageJoes.model.hardware.Device
import AverageJoes.model.machine.MachineActor
import AverageJoes.utils.DateUtils.stringToDate
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef

import scala.concurrent.duration._


class CustomerActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val c1: Customer = Customer("customer-1", "sokol", "guri", stringToDate("20/05/2020"))
  val tp: TrainingProgram =  TrainingProgram(c1)
                                .addExercise(Exercise(RunningMachine(speed = 10.0, incline = 20.0, timer = 30)))
                                .addExercise(Exercise(RunningMachine(speed = 11.0, incline = 21.0, timer = 30)))


  val deviceProbe: TestProbe[Device.Msg] = createTestProbe[Device.Msg]()
  val machineProbe: TestProbe[MachineActor.Msg] = createTestProbe[MachineActor.Msg]()
  val customerProbe: TestProbe[CustomerActor.Msg] = createTestProbe[CustomerActor.Msg]()
  val managerProbe: TestProbe[CustomerManager.Msg] = createTestProbe[CustomerManager.Msg]()
  val gymControllerProbe: TestProbe[GymController.Msg] = createTestProbe[GymController.Msg]()

  val manager: ActorRef[CustomerManager.Msg] = spawn(CustomerManager())
  val group: ActorRef[CustomerGroup.Msg] = spawn(CustomerGroup("group", manager))


  "Customer actor" should {

    "receive it's training program when instantiated" in {
      customerProbe.ref ! CustomerTrainingProgram(tp, group)
      var customerReceived = customerProbe.receiveMessage()
      assert(customerReceived.isInstanceOf[CustomerTrainingProgram])
    }

    "notify CustomerGroup that customer is ready" in {
      customerProbe.ref ! CustomerTrainingProgram(tp, group)
      var customerReceived = customerProbe.receiveMessage()
      assert(customerReceived.isInstanceOf[CustomerTrainingProgram])

      group ! CustomerReady(tp.allExercises.head, customerProbe.ref)

      customerReceived = customerProbe.receiveMessage()
      assert(customerReceived.isInstanceOf[NextMachineBooking])
    }

    "start and complete exercising" in {
      val exercising = spawn(CustomerExercising(customerProbe.ref, 4.seconds, tp))
      exercising ! ExerciseTiming
      customerProbe.expectMessage(5.seconds, ExerciseCompleted(tp))
    }

    "notify new booking during exercising" in {
      val book = spawn(BookWhileExercising(customerProbe.ref, 4.seconds, tp))
      book ! BookTiming
      customerProbe.expectMessage(5.seconds, NextMachineBooking(tp.allExercises.tail.head))
    }

    "complete training session" in {
      val tpWithOneExercise =  TrainingProgram(c1).addExercise(Exercise(RunningMachine(speed = 11.0, incline = 21.0, timer = 30)))

      val book = spawn(BookWhileExercising(customerProbe.ref, 4.seconds, tpWithOneExercise))
      book ! BookTiming
      customerProbe.expectMessage(5.seconds, TrainingCompleted())

      val exercising = spawn(CustomerExercising(customerProbe.ref, 4.seconds, tpWithOneExercise))
      exercising ! ExerciseTiming
      customerProbe.expectMessage(5.seconds, ExerciseCompleted(tp))

    }
  }
}