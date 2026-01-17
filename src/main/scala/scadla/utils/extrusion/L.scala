package scadla.utils.extrusion

import scadla.*
import scadla.InlineOps.*
import scadla.utils.*
import squants.space.Length

object L {

  /*
   *  ─ ┌─┐
   *    │ │
   *  a │ │
   *    │ └───┐ ─
   *  ─ └─────┘ ─ d
   *    |  b  |
   */
  def apply(a: Length, b: Length, d: Length)(length: Length): Solid = {
    apply(a, b, d, d)(length)
  }

  /*
   *    |t|
   *  ─ ┌─┐
   *    │ │
   *  a │ │
   *    │ └───┐ ─
   *  ─ └─────┘ ─ d
   *    |  b  |
   */
  def apply(a: Length, b: Length, d: Length, t: Length)(length: Length): Solid = {
    val c1 = Cube(t, a, length)
    val c2 = Cube(b, d, length)
    c1 + c2
  }

}
