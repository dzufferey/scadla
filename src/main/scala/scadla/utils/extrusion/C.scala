package scadla.utils.extrusion

import scadla.*
import scadla.InlineOps.*
import scadla.utils.*
import squants.space.Length
import scala.language.postfixOps
import squants.space.LengthConversions.*

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
  def apply(a: Length, b: Length, c: Length, d: Length)(length: Length): Solid = {
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
  def apply(a: Length, b: Length, c: Length, d: Length, t: Length)(length: Length): Solid = {
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
  def apply(a: Length, b: Length, c: Length, d1: Length, d2: Length, t1: Length, t2: Length)(length: Length): Solid = {
    val c1 = Cube( a, d1, length)
    val c2 = Cube(t1, b,  length)
    val c3 = Cube(t2, b,  length)
    val c4 = Cube( (a-c)/2, d2, length)
    c1 + c2 + c3.moveX(a-t2) + c4.moveY(b-d2) + c4.move(a/2+c/2, b-d2, 0 mm)
  }

}
