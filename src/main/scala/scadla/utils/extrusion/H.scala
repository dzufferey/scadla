package scadla.utils.extrusion

import scadla.*
import scadla.InlineOps.*
import scadla.utils.*
import squants.space.Length
import scala.language.postfixOps
import squants.space.LengthConversions.*

//actually: more like a double T
object H {

  /*
   *  ─ ┌───────┐
   *    └──┐ ┌──┘
   *  h    │ │
   *    ┌──┘ └──┐ ─
   *  ─ └───────┘ ─ d
   *    |   b   |
   */
  def apply(b: Length, h: Length, d: Length)(length: Length): Solid = {
    apply(b, h, d, d)(length)
  }


  /*
   *       |t|
   *  ─ ┌───────┐
   *    └──┐ ┌──┘
   *  h    │ │
   *    ┌──┘ └──┐ ─
   *  ─ └───────┘ ─ d
   *    |   b   |
   */
  def apply(b: Length, h: Length, d: Length, t: Length)(length: Length): Solid = {
    apply(b, h, d, d, t)(length)
  }

  /*
   *       |t|
   *  ─ ┌───────┐ ─
   *    └──┐ ┌──┘ ─ d1
   *  h    │ │
   *    ┌──┘ └──┐ ─
   *  ─ └───────┘ ─ d2
   *    |   b   |
   */
  def apply(b: Length, h: Length, d1: Length, d2: Length, t: Length)(length: Length): Solid = {
    apply(b, b, h, d1, d2, t)(length)
  }

  /*
   *       |t|
   *    |   b1  |
   *  ─ ┌───────┐ ─
   *    └──┐ ┌──┘ ─ d1
   *  h    │ │
   *    ┌──┘ └──┐ ─
   *  ─ └───────┘ ─ d2
   *    |   b2  |
   */
  def apply(b1: Length, b2: Length, h: Length, d1: Length, d2: Length, t: Length)(length: Length): Solid = {
    val m1 = (b1-t) / 2
    val m2 = (b2-t) / 2
    apply(m1, m1, m2, m2, h, d1, d2, t)(length)
  }

  /*
   *    |b1|t|b2|
   *  ─ ┌───────┐ ─
   *    └──┐ ┌──┘ ─ d1
   *  h    │ │
   *    ┌──┘ └──┐ ─
   *  ─ └───────┘ ─ d2
   *    |b3|t|b4|
   */
  def apply(b1: Length, b2: Length, b3: Length, b4: Length, h: Length, d1: Length, d2: Length, t: Length)(length: Length): Solid = {
    val c1 = Cube( b1+t+b2, d1, length)
    val c2 = Cube( b3+t+b4, d2, length)
    val c3 = Cube( t, h, length)
    c1.move(b3-b1, h-d1, 0 mm) + c2 + c3.moveX(b3)
  }

}
