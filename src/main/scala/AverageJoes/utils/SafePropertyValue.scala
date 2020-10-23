package AverageJoes.utils

object SafePropertyValue {
  object NonNegative {

    object NonNegInt {
      def apply(v: Int): NonNegInt = { if(v < 0) throw new IllegalArgumentException
        new NonNegInt(v)
      }
      implicit def toNonNegInt(v: Int): NonNegInt = NonNegInt(v)
    }

    case class NonNegInt private (val value: Int)
    implicit def toInt(nn: NonNegInt): Int = nn.value
  }
}
