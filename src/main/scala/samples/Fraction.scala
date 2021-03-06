package samples

//See http://www.scalatest.org/user_guide/property_based_testing
class Fraction(n: Int, d: Int) {

  require(d != 0)
  require(d != Integer.MIN_VALUE)
  require(n != Integer.MIN_VALUE)

  val numer = if (d < 0) -1 * n else n
  val denom = d.abs

  override def toString = s"$numer / $denom"

}
