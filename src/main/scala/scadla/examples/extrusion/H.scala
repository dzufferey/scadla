package scadla.examples.extrusion

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scadla.EverythingIsIn.{millimeters, radians}  

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
  def apply(b: Double, h: Double, d: Double)(length: Double): Solid = {
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
  def apply(b: Double, h: Double, d: Double, t: Double)(length: Double): Solid = {
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
  def apply(b: Double, h: Double, d1: Double, d2: Double, t: Double)(length: Double): Solid = {
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
  def apply(b1: Double, b2: Double, h: Double, d1: Double, d2: Double, t: Double)(length: Double): Solid = {
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
  def apply(b1: Double, b2: Double, b3: Double, b4: Double, h: Double, d1: Double, d2: Double, t: Double)(length: Double): Solid = {
    val c1 = Cube( b1+t+b2, d1, length)
    val c2 = Cube( b3+t+b4, d2, length)
    val c3 = Cube( t, h, length)
    c1.move(b3-b1, h-d1, 0) + c2 + c3.moveX(b3)
  }

}
