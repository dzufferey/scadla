package scadla.utils.extrusion

import scadla._
import scadla.InlineOps._
import scadla.utils._
import squants.space.Length

object Z {
  
  /*
   *  ─ ┌────┐
   *    └──┐ │
   *  h    │ │
   *       │ └──┐ ─
   *  ─    └────┘ ─ d
   *    |   b   |
   */
  def apply(b: Length, h: Length, d: Length)(length: Length): Solid = {
    val m = (b+d) / 2
    apply(m, m, h, d, d)(length)
  }

  /*
   *    | b1 |
   *  ─ ┌────┐
   *    └──┐ │
   *  h    │ │
   *       │ └──┐ ─
   *  ─    └────┘ ─ d
   *       | b2 |
   */
  def apply(b1: Length, b2: Length, h: Length, d: Length)(length: Length): Solid = {
    apply(b1, b2, h, d, d)(length)
  }

  /*
   *       |t|
   *    | b1 |
   *  ─ ┌────┐
   *    └──┐ │
   *  h    │ │
   *       │ └──┐ ─
   *  ─    └────┘ ─ d
   *       | b2 |
   */
  def apply(b1: Length, b2: Length, h: Length, d: Length, t: Length)(length: Length): Solid = {
    apply(b1, b2, h, d, d, t)(length)
  }

  /*
   *           |t|
   *        | b1 |
   *    ─ ─ ┌────┐
   * d1 ─   └──┐ │
   *      h    │ │
   *           │ └──┐ ─
   *      ─    └────┘ ─ d2
   *           | b2 |
   */
  def apply(b1: Length, b2: Length, h: Length, d1: Length, d2: Length, t: Length)(length: Length): Solid = {
    val c1 = Cube(b1, d1, length)
    val c2 = Cube( t, h, length)
    val c3 = Cube(b2, d2, length)
    c1.moveY(h-d1) + c2.moveX(b1-t) + c3.moveX(b1-t)
  }

}
