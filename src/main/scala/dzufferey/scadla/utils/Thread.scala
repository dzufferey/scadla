package dzufferey.scadla.utils
  
import dzufferey.scadla._

/** Standard sizes for screw threads. */
object Thread {
  
  /* Radius of ISO metric sizes. _x_y stands of x.y mm. */
  object ISO {
    val _1   = 1    / 2.0
    val _1_2 = 1.2  / 2.0
    val _1_6 = 1.6  / 2.0
    val _2   = 2    / 2.0
    val _2_5 = 2.5  / 2.0
    val _3   = 3    / 2.0
    val _4   = 4    / 2.0
    val _5   = 5    / 2.0
    val _6   = 6    / 2.0
    val _8   = 8    / 2.0
    val _10  = 10   / 2.0
    val _12  = 12   / 2.0
    val _16  = 16   / 2.0
    val _20  = 20   / 2.0
    val _24  = 24   / 2.0
    val _30  = 30   / 2.0
    val _36  = 36   / 2.0
    val _42  = 42   / 2.0
    val _48  = 48   / 2.0
    val _56  = 56   / 2.0
    val _64  = 64   / 2.0
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
