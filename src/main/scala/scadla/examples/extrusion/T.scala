package scadla.examples.extrusion

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scadla.EverythingIsIn.{millimeters, radians}  

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
  def apply(b: Double, h: Double, d: Double)(length: Double): Solid = {
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
  def apply(b: Double, h: Double, d: Double, t: Double)(length: Double): Solid = {
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
  def apply(b1: Double, b2: Double,  h: Double, d: Double, t: Double)(length: Double): Solid = {
     val c1 = Cube(t, h, length)
     val c2 = Cube(b1+t+b2, d, length)
     c1.moveX(b1) + c2
  }

}
