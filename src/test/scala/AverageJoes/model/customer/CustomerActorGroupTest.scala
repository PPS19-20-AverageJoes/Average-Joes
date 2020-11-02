package AverageJoes.model.customer

import scala.concurrent.duration._
import AverageJoes.model.customer.CustomerActor._
import AverageJoes.model.customer.CustomerManager._
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerActorGroupTest extends ScalaTestWithActorTestKit with AnyWordSpecLike{

  "Customer group" should {

    "register a customer actor" in {
      val probe = createTestProbe[CustomerRegistered]()
      val groupActor = spawn(CustomerGroup("group"))

      /* Customer 1 */
      groupActor ! RequestCustomerLogin("customer1", probe.ref)
      val registered1 = probe.receiveMessage()
      val customerActor1 = registered1.customer

      /* Customer 2 */
      groupActor ! RequestCustomerLogin("customer2", probe.ref)
      val registered2 = probe.receiveMessage()
      val customerActor2 = registered2.customer
      customerActor1 should !== (customerActor2)


      /* Check if the customers actors are running */
      val recordProbe = createTestProbe[CustomerAliveSignal]()
      customerActor1 ! CustomerAlive(requestId = 10L, recordProbe.ref)
      recordProbe.expectMessage(CustomerAliveSignal(10L))
      customerActor2 ! CustomerAlive(requestId = 11L, recordProbe.ref)
      recordProbe.expectMessage(CustomerAliveSignal(11L))
    }

    "return the same customer actor for the same customer" in {
      val probe = createTestProbe[CustomerRegistered]()
      val groupActor = spawn(CustomerGroup("group"))

      groupActor ! RequestCustomerLogin("customer1", probe.ref)
      val registered1 = probe.receiveMessage()

      /* Registering the same customer actor again */
      groupActor ! RequestCustomerLogin("customer1", probe.ref)
      val registered2 = probe.receiveMessage()

      registered1.customer should === (registered2.customer)
    }

    "list active customers" in {
      val registeredProbe = createTestProbe[CustomerRegistered]()
      val groupActor = spawn(CustomerGroup("group"))

      groupActor ! RequestCustomerLogin("customer1", registeredProbe.ref)
      registeredProbe.receiveMessage()

      groupActor ! RequestCustomerLogin("customer2", registeredProbe.ref)
      registeredProbe.receiveMessage()

      val usersListProbe = createTestProbe[ReplyCustomerList]()
      groupActor ! RequestCustomerList(10L,  usersListProbe.ref)
      usersListProbe.expectMessage(ReplyCustomerList(10L, Set("customer1", "customer2")))
    }

    "list active customers after one shuts down" in {
      val registeredProbe = createTestProbe[CustomerRegistered]()
      val groupActor = spawn(CustomerGroup("group"))

      groupActor ! RequestCustomerLogin("customer1", registeredProbe.ref)
      val registered1 = registeredProbe.receiveMessage()
      val customerToShutDown = registered1.customer

      groupActor ! RequestCustomerLogin("customer2", registeredProbe.ref)
      registeredProbe.receiveMessage()

      val customersListProbe = createTestProbe[ReplyCustomerList]()
      groupActor ! RequestCustomerList(10L, customersListProbe.ref)
      customersListProbe.expectMessage(ReplyCustomerList(10L, Set("customer1", "customer2")))

      customerToShutDown ! Passivate
      registeredProbe.expectTerminated(customerToShutDown, registeredProbe.remainingOrDefault)

      /* using awaitAssert to retry because it might take longer for the group actor
      to notice the terminated customer actor */
      registeredProbe.awaitAssert{
        groupActor ! RequestCustomerList(10L, customersListProbe.ref)
        customersListProbe.expectMessage(ReplyCustomerList(10L, Set("customer2")))
      }
    }

  }

}
