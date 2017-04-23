package scadla.examples.extrusion

import scadla._
import scadla.InlineOps._
import scadla.utils._
import squants.space.Length

object T {

  /*
   *       |d|
   *  ─    ┌─┐
   *       │ │
   *  h    │ │
   *    ┌──┘ └──┐
   *  ─ └───────┘
   *    |   b   |
   */
  def apply(b: Length, h: Length, d: Length)(length: Length): Solid = {
     apply(b, h, d, d)(length)
  }

  /*
   *       |t|
   *  ─    ┌─┐
   *       │ │
   *  h    │ │
   *    ┌──┘ └──┐ ─
   *  ─ └───────┘ ─ d
   *    |   b   |
   */
  def apply(b: Length, h: Length, d: Length, t: Length)(length: Length): Solid = {
     val m = (b-t)/2
     apply(m, m, h, d, t)(length)
  }

  /*
   *       |t|
   *  ─    ┌─┐
   *       │ │
   *  h    │ │
   *    ┌──┘ └──┐ ─
   *  ─ └───────┘ ─ d
   *    |b1|t|b2|
   */
  def apply(b1: Length, b2: Length,  h: Length, d: Length, t: Length)(length: Length): Solid = {
     val c1 = Cube(t, h, length)
     val c2 = Cube(b1+t+b2, d, length)
     c1.moveX(b1) + c2
  }

}
