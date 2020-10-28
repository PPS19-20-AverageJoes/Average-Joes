package AverageJoes.model.customer

import AverageJoes.model.customer.CustomerManager.{Command, RequestCustomerCreation}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object CustomerObjectTester {
  def apply(): Behavior[Command] = Behaviors.setup[Command](context => {
    val customerManager: ActorRef[Command] = context.spawn(CustomerManager(), "customer-manager")
    customerManager ! RequestCustomerCreation("group1", "customer1", customerManager)
    CustomerManager()
  });
}