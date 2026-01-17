package scadla.utils.thread

import scala.language.postfixOps
import squants.space.LengthConversions.*

/* Radius of UTS imperial sizes. _x_y stands of x/y inches.*/
object UTS {
  val _1_8    = (0.125 inches)      / 2.0
  val _5_32   = (5.0 inches) / 32   / 2.0
  val _1_4    = (0.25 inches)       / 2.0
  val _5_16   = (5.0 inches) / 16   / 2.0
  val _3_8    = (3 inches) * 0.125  / 2.0
  val _7_16   = (7.0 inches) / 16   / 2.0
  val _1_2    = (0.5 inches)        / 2.0
  val _9_16   = (9.0 inches) / 16   / 2.0
  val _5_8    = (5 inches) * 0.125  / 2.0
  val _11_16  = (11.0 inches) / 16  / 2.0
  val _3_4    = (0.75 inches)       / 2.0
  val _7_8    = (7 inches) * 0.125  / 2.0
  val _15_16  = (15.0 inches) / 16  / 2.0
  val _1      = (1 inches)          / 2.0
}
