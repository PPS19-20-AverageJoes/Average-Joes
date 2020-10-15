package AverageJoes.model.workout

sealed trait Exercise

case class LiftMachine(wight:Int, set:Int) extends Exercise
case class RunningMachine(incline:Int, speed: Int, time: Int) extends Exercise
case class CyclingMachine(resistance:Int, time: Int) extends  Exercise