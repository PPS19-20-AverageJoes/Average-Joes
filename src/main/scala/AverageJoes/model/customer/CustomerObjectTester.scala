package AverageJoes.model.customer

import AverageJoes.controller.GymController
import AverageJoes.model.customer.CustomerManager.{Msg, RequestCustomerList}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object CustomerObjectTester {
  def apply(): Behavior[Msg] = Behaviors.setup[Msg](context => {
    val customerManager: ActorRef[CustomerManager] =
      context.spawn(new CustomerManager(context), "customer-manager")
      val controller =  context.spawn(GymController(), "gym-contoller")
    customerManager ! RequestCustomerList(controller)

    CustomerManager()
  })
}