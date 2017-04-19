package scadla.examples.extrusion

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scadla.EverythingIsIn.{millimeters, radians}  

object L {

  /*
   *  ─ ┌─┐
   *    │ │
   *  a │ │
   *    │ └───┐ ─
   *  ─ └─────┘ ─ d
   *    |  b  |
   */
  def apply(a: Double, b: Double, d: Double)(length: Double): Solid = {
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
  def apply(a: Double, b: Double, d: Double, t: Double)(length: Double): Solid = {
    val c1 = Cube(t, a, length)
    val c2 = Cube(b, d, length)
    c1 + c2
  }

}
