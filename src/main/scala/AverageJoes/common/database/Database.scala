package AverageJoes.common.database

import AverageJoes.common.Parser
import AverageJoes.common.database.table.CustomerImpl


object Database {
  var customerStorage: Storage[CustomerImpl] =
    Parser.parsing("src/main/resources/customer.json", new GymStorage[CustomerImpl])

}

