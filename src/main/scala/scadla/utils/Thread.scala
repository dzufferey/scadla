package scadla.utils
  
import squants.space.Length
import scala.language.postfixOps
import squants.space.LengthConversions._

/** Standard sizes for screw threads. */
object Thread {
  
  /* Radius of ISO metric sizes. _x_y stands of x.y mm. */
  object ISO {
    val M1   = (1   mm) / 2.0
    val M1_2 = (1.2 mm) / 2.0
    val M1_6 = (1.6 mm) / 2.0
    val M2   = (2   mm) / 2.0
    val M2_5 = (2.5 mm) / 2.0
    val M3   = (3   mm) / 2.0
    val M4   = (4   mm) / 2.0
    val M5   = (5   mm) / 2.0
    val M6   = (6   mm) / 2.0
    val M8   = (8   mm) / 2.0
    val M10  = (10  mm) / 2.0
    val M12  = (12  mm) / 2.0
    val M16  = (16  mm) / 2.0
    val M20  = (20  mm) / 2.0
    val M24  = (24  mm) / 2.0
    val M30  = (30  mm) / 2.0
    val M36  = (36  mm) / 2.0
    val M42  = (42  mm) / 2.0
    val M48  = (48  mm) / 2.0
    val M56  = (56  mm) / 2.0
    val M64  = (64  mm) / 2.0
  }

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

}
