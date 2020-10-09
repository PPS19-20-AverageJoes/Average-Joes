package AverageJoes.model.workout

object Exercise {
    case class LiftMachine(wight:Int, set:Int)
    case class RunningMachine(incline:Int, speed: Int, time: Int)
    case class CyclingMachine(resistance:Int, time: Int)
}
