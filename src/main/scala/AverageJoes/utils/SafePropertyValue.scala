package AverageJoes.utils

import AverageJoes.utils.SafePropertyValue.NonNegative.{NonEmptyString, NonNegDouble, NonNegDuration, NonNegInt, NonNegative}

import scala.concurrent.duration.FiniteDuration


object SafePropertyValue {

  trait SafePropertyVal

  object NonNegative {
    trait NonNegative

    object NonNegInt {
      def apply(v: Int): NonNegInt = { if(v < 0) throw new IllegalArgumentException
        new NonNegInt(v)
      }
      implicit def toNonNegInt(v: Int): NonNegInt = NonNegInt(v)
    }

    object NonNegDouble {
      def apply(v: Double): NonNegDouble = { if(v < 0) throw new IllegalArgumentException
        new NonNegDouble(v)
      }
      implicit def toNonNegDouble(v: Double): NonNegDouble = NonNegDouble(v)
    }

    object NonNegDuration {
      def apply(v: FiniteDuration): NonNegDuration = { if(v.length < 0) throw new IllegalArgumentException
        new NonNegDuration(v)
      }
      implicit def toNonNegDuration(v: FiniteDuration): NonNegDuration = NonNegDuration(v)
    }

    object NonEmptyString {
      def apply(v: String): NonEmptyString = { if(v.isEmpty) throw new IllegalArgumentException
        new NonEmptyString(v)
      }
      implicit def toNonEmptyString(v: String): NonEmptyString = NonEmptyString(v)
    }

    case class NonNegInt private (value: Int) extends SafePropertyVal with NonNegative
    implicit def toInt(nn: NonNegInt): Int = nn.value

    case class NonNegDouble private (value: Double) extends SafePropertyVal with NonNegative
    implicit def toDouble(nn: NonNegDouble): Double = nn.value

    case class NonNegDuration private (value: FiniteDuration) extends SafePropertyVal with NonNegative
    implicit def toLong(nn: NonNegDuration): FiniteDuration = nn.value

    case class NonEmptyString private (value: String) extends SafePropertyVal with NonNegative
    implicit def toString(nn: NonEmptyString): String = nn.value

  }
}
