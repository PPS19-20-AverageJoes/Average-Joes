package averageJoes.model.fitness

import java.util.Date

case class ExecutionValues(heartRateMax: Int, heartRateMin: Int, heartRateAvg: Int, date: Date = new Date())
