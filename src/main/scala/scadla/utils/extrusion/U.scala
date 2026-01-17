package scadla.utils.extrusion

import scadla.*
import scadla.InlineOps.*
import scadla.utils.*
import squants.space.Length

object U {

  /*
   *  ─ ┌─┐   ┌─┐
   *    │ │   │ │
   *  h │ │   │ │
   *    │ └───┘ │ ─
   *  ─ └───────┘ ─ d
   *    |   b   |
   */
  def apply(b: Length, h: Length, d: Length)(length: Length): Solid = {
    apply(b, h, d, d)(length)
  }

  /*
   *    |t|
   *  ─ ┌─┐   ┌─┐
   *    │ │   │ │
   *  h │ │   │ │
   *    │ └───┘ │ ─
   *  ─ └───────┘ ─ d
   *    |   b   |
   */
  def apply(b: Length, h: Length, d: Length, t: Length)(length: Length): Solid = {
    apply(b, h, d, t, t)(length)
  }

  /*
   *    t1    t2
   *    | |   | |
   *  ─ ┌─┐   ┌─┐
   *    │ │   │ │
   *  h │ │   │ │
   *    │ └───┘ │ ─
   *  ─ └───────┘ ─ d
   *    |   b   |
   */
  def apply(b: Length, h: Length, d: Length, t1: Length, t2: Length)(length: Length): Solid = {
    val c1 = Cube(  b, d, length)
    val c2 = Cube( t1, h, length)
    val c3 = Cube( t2, h, length)
    c1 + c2 + c3.moveX(b-t2)
  }

}
