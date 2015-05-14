package dzufferey.scadla.examples.cnc

import math._
import dzufferey.scadla._
import utils._
import InlineOps._
import dzufferey.scadla.examples.fastener._
import Common._

object Chuck {
  
  //TODO some more parameters
  def innerThread( innerHole: Double,
                   chuckHeight: Double,
                   colletLength: Double,
                   mNumber: Double ) = {
    val body = Cylinder(12.6, 12.6, chuckHeight - 10)
    val hexTop = (body * Hexagon(Hexagon.minRadius(13), 10)).moveZ(chuckHeight-10)
    val thread = threading.screwThreadIsoInner(mNumber, colletLength)
    val toRemove = List(
      thread,
      nut.M8.moveZ(chuckHeight -4.5),
      Cylinder( 4, 4, chuckHeight),
      Cylinder( innerHole, innerHole, chuckHeight -4.5 -8),
      Cylinder( innerHole+1, innerHole, chuckHeight -4.5 -8 -8),
      Cylinder( innerHole, innerHole - 6, 6).moveZ(chuckHeight -4.5 -8)
    )
    body + hexTop --toRemove
  }

  //TODO some more parameters
  def outerThread(innerHole: Double,
                  chuckHeight: Double,
                  colletLength: Double,
                  mNumber: Double) = {
    val body = Cylinder(12.6, 12.6, chuckHeight - 10)
    val hexTop = (body * Hexagon(Hexagon.minRadius(13), 10)).moveZ(chuckHeight-10)
    val thread = threading.screwThreadIsoOuter(mNumber, colletLength, 2)
    val toRemove = List(
      nut.M8.moveZ(chuckHeight -4.5),
      Cylinder( 4, 4, chuckHeight),
      Cylinder( innerHole, innerHole, chuckHeight -4.5 -8),
      Cylinder( innerHole+1, innerHole, chuckHeight -4.5 -8 -8),
      Cylinder( innerHole, innerHole - 6, 6).moveZ(chuckHeight -4.5 -8)
    )
    body + hexTop + thread -- toRemove
  }

  //TODO some more parameters
  def outerThreadCap(chuckHeight: Double, colletLength: Double) = {
    val body = Cylinder(18, 18, colletLength + 3)
    val thread = threading.screwThreadIsoInner(30, chuckHeight)
    val grove = Cylinder(1, 1, colletLength).scaleX(0.5).moveX(18)
    val groves = for (i <- 0 until 12) yield grove.rotateZ(i * 2 * Pi / 12)
    body - thread.moveZ(2) - Cylinder(3, 3, colletLength) -- groves
  }
  
  //TODO some more parameters
  def blocker(innerHole: Double, tolerance: Double) = {
    Cylinder( innerHole-tolerance, innerHole-6-tolerance, 6) - Cylinder( 4+tolerance, 4+tolerance, 6)
  }

  //TODO some more parameters
  //TODO better wrench, U shaped with the handles at the end of the U
  def wrench = {
    val hRadius = Hexagon.minRadius(12.6)
    val inner = Hexagon(hRadius + tolerance * 4, 8)
    val body = roundedCubeH(60, 16, 8, 3).move(-60, -8, 0) + roundedCubeH(25, 36, 8, 12).move(-18, -18, 0)
    body - inner
  }

}
