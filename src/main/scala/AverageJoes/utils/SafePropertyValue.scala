package AverageJoes.utils

object SafePropertyValue {

  trait SafePropertyVal
  object NonNegative {

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

    object NonEmptyString {
      def apply(v: String): NonEmptyString = { if(v.isEmpty) throw new IllegalArgumentException
        new NonEmptyString(v)
      }
      implicit def toNonEmptyString(v: String): NonEmptyString = NonEmptyString(v)
    }

    case class NonNegInt private (value: Int) extends SafePropertyVal
    implicit def toInt(nn: NonNegInt): Int = nn.value

    case class NonNegDouble private (value: Double) extends SafePropertyVal
    implicit def toDouble(nn: NonNegDouble): Double = nn.value

    case class NonEmptyString private (value: String) extends SafePropertyVal
    implicit def toString(nn: NonEmptyString): String = nn.value
  }
}
