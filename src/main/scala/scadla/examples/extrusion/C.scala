package scadla.examples.extrusion

import scadla._
import scadla.InlineOps._
import scadla.utils._

object C {

  /*
   *       | c |
   *  ─ ┌──┐   ┌──┐
   *    │ ┌┘   └┐ │
   *  b │ │     │ │
   *    │ └─────┘ │ ─
   *  ─ └─────────┘ ─ d
   *    |    a    |
   */
  def apply(a: Double, b: Double, c: Double, d: Double)(length: Double): Solid = {
    apply(a, b, c, d, d)(length)
  }

  /*
   *    |t|| c |
   *  ─ ┌──┐   ┌──┐
   *    │ ┌┘   └┐ │
   *  b │ │     │ │
   *    │ └─────┘ │ ─
   *  ─ └─────────┘ ─ d
   *    |    a    |
   */
  def apply(a: Double, b: Double, c: Double, d: Double, t: Double)(length: Double): Solid = {
    apply(a, b, c, d, d, t, t)(length)
  }

  /*
   *     t1      t2
   *    | || c || |
   *  ─ ┌──┐   ┌──┐ ─
   *    │ ┌┘   └┐ │ ─ d2
   *  b │ │     │ │
   *    │ └─────┘ │ ─
   *  ─ └─────────┘ ─ d1
   *    |    a    |
   */
  def apply(a: Double, b: Double, c: Double, d1: Double, d2: Double, t1: Double, t2: Double)(length: Double): Solid = {
    val c1 = Cube( a, d1, length)
    val c2 = Cube(t1, b,  length)
    val c3 = Cube(t2, b,  length)
    val c4 = Cube( (a-c)/2, d2, length)
    c1 + c2 + c3.moveX(a-t2) + c4.moveY(b-d2) + c4.move(a/2+c/2, b-d2, 0)
  }

}
