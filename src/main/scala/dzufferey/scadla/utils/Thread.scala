package dzufferey.scadla.utils
  
import dzufferey.scadla._

/** Standard sizes for screw threads. */
object Thread {
  
  /* Radius of ISO metric sizes. _x_y stands of x.y mm. */
  object ISO {
    val M1   = 1    / 2.0
    val M1_2 = 1.2  / 2.0
    val M1_6 = 1.6  / 2.0
    val M2   = 2    / 2.0
    val M2_5 = 2.5  / 2.0
    val M3   = 3    / 2.0
    val M4   = 4    / 2.0
    val M5   = 5    / 2.0
    val M6   = 6    / 2.0
    val M8   = 8    / 2.0
    val M10  = 10   / 2.0
    val M12  = 12   / 2.0
    val M16  = 16   / 2.0
    val M20  = 20   / 2.0
    val M24  = 24   / 2.0
    val M30  = 30   / 2.0
    val M36  = 36   / 2.0
    val M42  = 42   / 2.0
    val M48  = 48   / 2.0
    val M56  = 56   / 2.0
    val M64  = 64   / 2.0
  }

  /* Radius of UTS imperial sizes. _x_y stands of x/y inches.*/
  object UTS {
    val _1_8    = inch2mm(0.125)      / 2.0
    val _5_32   = inch2mm(5.0 / 32)   / 2.0
    val _1_4    = inch2mm(0.25)       / 2.0
    val _5_16   = inch2mm(5.0 / 16)   / 2.0
    val _3_8    = inch2mm(3 * 0.125)  / 2.0
    val _7_16   = inch2mm(7.0 / 16)   / 2.0
    val _1_2    = inch2mm(0.5)        / 2.0
    val _9_16   = inch2mm(9.0 / 16)   / 2.0
    val _5_8    = inch2mm(5 * 0.125)  / 2.0
    val _11_16  = inch2mm(11.0 / 16)  / 2.0
    val _3_4    = inch2mm(0.75)       / 2.0
    val _7_8    = inch2mm(7 * 0.125)  / 2.0
    val _15_16  = inch2mm(15.0 / 16)  / 2.0
    val _1      = inch2mm(1)          / 2.0
  }

}
