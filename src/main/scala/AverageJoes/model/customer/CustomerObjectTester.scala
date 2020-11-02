package AverageJoes.model.customer

import AverageJoes.model.customer.CustomerManager.{Msg, RequestCustomerLogin}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object CustomerObjectTester {
  def apply(): Behavior[Msg] = Behaviors.setup[Msg](context => {
    val customerManager: ActorRef[Msg] = context.spawn(CustomerManager(), "customer-manager")

    customerManager ! RequestCustomerLogin("customer1", customerManager)

    CustomerManager()
  })
}