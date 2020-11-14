package AverageJoes.model.customer

import java.util.Date

/**
 * TODO: check input length
 */


trait Customer  {
  def CF: String
  def name: String
  def surname: String
  def birthday: Date
}

object Customer {
  def apply(CF: String, name: String, surname: String, birthday: Date): Customer = new CustomerImpl(CF, name, surname, birthday)

  private class CustomerImpl( override val CF: String,
                              override val name: String,
                              override val surname: String,
                              override val birthday: Date) extends Customer
}
