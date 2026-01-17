package scadla.examples.cnc

import math.*
import scadla.*
import utils.*
import InlineOps.*
import Common.*
import scadla.EverythingIsIn.{millimeters, radians}  

object Pulley {
  
  /** A pulley for a 5mm D shaft (stepper motor shaft)
   * @param radiusO outer radius
   * @param radiusI inner radius (groove)
   * @param n number of grooves
   * @param h0 height of the outer discs
   * @param h1 height of the transition cones
   * @param h2 height of the inner discs
   */
  def apply(radiusO: Double, radiusI: Double, n: Int, h0: Double, h1: Double, h2: Double) = {
    val outerDisc = Cylinder(radiusO, h0)
    val innerDisc = Cylinder(radiusI, h2)
    val cone1 = Cylinder(radiusO, radiusI, h1)
    val cone2 = Cylinder(radiusI, radiusO, h1)
    val h = h0+h1+h2+h1
    val groove = Union(
      outerDisc,
      cone1.moveZ(h0),
      innerDisc.moveZ(h0+h1),
      cone2.moveZ(h0+h1+h2),
      outerDisc.moveZ(h)
    )
    val grooves = (0 until n).map( i => groove.moveZ(i * h))
    Union(grooves: _*) - Bigger(Nema17.axis(n * h + h0), looseTolerance)
  }

}
