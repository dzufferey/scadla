package dzufferey.scadla.examples.cnc

import math._
import dzufferey.scadla._
import utils._
import InlineOps._
import Common._

//TODO a platform to put hold the spindle
object Platform {

  protected def bearings(space: Double) = {
    Union(
      bearing.moveZ(-tolerance/2),
      bearing.moveZ(7 + space + tolerance/2),
      Cylinder(9, space).moveZ(7),
      Cylinder(11 + tolerance,         11 + tolerance - space, space).moveZ(7),
      Cylinder(11 + tolerance - space, 11 + tolerance,         space).moveZ(7)
    )
  }

  protected def spindleMount(height: Double, gap: Double) = {
    val screw = Cylinder(Thread.ISO.M3+tolerance, Thread.ISO.M3+tolerance, height)
    Cylinder(15+gap, height).move(15,15,0) ++ Spindle.fixCoord.map{ case (x,y,_) => screw.move(x,y,0) }
  }

  def bearingsTest(height: Double, s: Double) = {
    val base = Cylinder(14, height)
    val hole = Cube(15,3,height-3).move(5,-1.5,1.5)
    base - hole - bearings(s).moveZ(height/2 - 7 - s/2)
  }

  //space should be ~ zBearingSpace + 2*tolerance
  def apply(wall: Double, bearingGap: Double, height: Double, space: Double) = {
    val bNeg = bearings(space).moveZ(height/2 - 7 - space/2) 
    val bHolder = Cylinder(11 + wall, height)
    val radius = 43
    val offset = max(wall/2, bearingGap/2)
    def place(s: Solid) = {
      val paired = Union(
        s.move( 11 +offset, radius, 0),
        s.move(-11 -offset, radius, 0)
      )
      for (i <- 0 until 3) yield paired.rotateZ(2 * i * Pi / 3)
    }
    val base = Hull(place(bHolder): _*) -- place(bNeg)
    base - spindleMount(height, 0.8).move(-15, -30, 0)
  }

}
