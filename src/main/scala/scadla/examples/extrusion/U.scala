package scadla.examples.extrusion

import scadla._
import scadla.InlineOps._
import scadla.utils._

object U {

  /*
   *  ─ ┌─┐   ┌─┐
   *    │ │   │ │
   *  h │ │   │ │
   *    │ └───┘ │ ─
   *  ─ └───────┘ ─ d
   *    |   b   |
   */
  def apply(b: Double, h: Double, d: Double)(length: Double): Solid = {
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
  def apply(b: Double, h: Double, d: Double, t: Double)(length: Double): Solid = {
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
  def apply(b: Double, h: Double, d: Double, t1: Double, t2: Double)(length: Double): Solid = {
    val c1 = Cube(  b, d, length)
    val c2 = Cube( t1, h, length)
    val c3 = Cube( t2, h, length)
    c1 + c2 + c3.moveX(b-t2)
  }

}
