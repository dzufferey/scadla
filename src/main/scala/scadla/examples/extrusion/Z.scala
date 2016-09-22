package scadla.examples.extrusion

import scadla._
import scadla.InlineOps._
import scadla.utils._

object Z {
  
  /*
   *  ─ ┌────┐
   *    └──┐ │
   *  h    │ │
   *       │ └──┐ ─
   *  ─    └────┘ ─ d
   *    |   b   |
   */
  def apply(b: Double, h: Double, d: Double)(length: Double): Solid = {
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
  def apply(b1: Double, b2: Double, h: Double, d: Double)(length: Double): Solid = {
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
  def apply(b1: Double, b2: Double, h: Double, d: Double, t: Double)(length: Double): Solid = {
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
  def apply(b1: Double, b2: Double, h: Double, d1: Double, d2: Double, t: Double)(length: Double): Solid = {
    val c1 = Cube(b1, d1, length)
    val c2 = Cube( t, h, length)
    val c3 = Cube(b2, d2, length)
    c1.moveY(h-d1) + c2.moveX(b1-t) + c3.moveX(b1-t)
  }

}
