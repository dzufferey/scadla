package dzufferey.scadla.examples.cnc

import math._
import dzufferey.scadla._
import utils._
import InlineOps._
import dzufferey.scadla.examples.fastener._

object Collet {

  def slits(outer: Double, height: Double, nbrSlits: Int, slitWidth: Double, wall: Double) = {
    assert(nbrSlits % 2 == 0, "number of slits must be even")
    val slit = Cube(slitWidth, outer, height - wall).moveX(-slitWidth/2)
    for(i <- 0 until nbrSlits) yield slit.rotateZ(i * 2 * Pi / nbrSlits).moveZ((i % 2) * wall)
  }

  //example: Collet(6, 7, 3, 20, 6, 1, 2)
  def apply(outer1: Double, outer2: Double, inner: Double, height: Double,
            nbrSlits: Int, slitWidth: Double, wall: Double) = {
    val base = Cylinder(outer1, outer2, height) - Cylinder(inner, height)
    base -- slits(max(outer1, outer1), height, nbrSlits, slitWidth, wall)
  }

  def threaded(outer1: Double, outer2: Double, inner: Double, height: Double,
               nbrSlits: Int, slitWidth: Double, wall: Double,
               mNumber: Double, tolerance: Double, screwRadius: Double) = {
    val innerC = Cylinder(inner, height)
    val base = Cylinder(outer1, outer2, height)
    val slts = slits(mNumber, height, nbrSlits, slitWidth, wall)
    val thread = new MetricThread(tolerance).screwThreadIsoOuter(mNumber, height, 2)
    val wrenchHoles = for(i <- 0 until nbrSlits) yield
      Cylinder(screwRadius+tolerance, height).moveX((outer2+inner)*2/3).rotateZ(i * 2 * Pi / nbrSlits)
    thread + base -- slts - innerC -- wrenchHoles
  }

  def withPulloutScrew(outer1: Double, outer2: Double, inner: Double, height: Double,
                       nbrSlits: Int, slitWidth: Double, wall: Double,
                       screwRadius: Double, screwLength: Double, screwHead: Double, screwOut: Double, tolerance: Double) = {
    val nut = new NutPlaceHolder(tolerance).apply(screwRadius)
    val nutHole = Hull(nut, nut.moveX(max(outer1, outer2)))
    val screw = Union(
      Cylinder(screwRadius + tolerance, screwLength + screwHead),
      Cylinder(screwRadius * 2, screwHead).moveZ(screwLength),
      nutHole.moveZ(screwOut)
    ).moveZ(height - screwLength - screwHead)

    val nbrScrew = nbrSlits / 2
    val angleOffset = Pi / nbrSlits
    val xOffset = (inner + min(outer1, outer2)) * 2 / 3
    val screws = for(i <- 0 until nbrScrew) yield screw.moveX(xOffset).rotateZ(i * 2 * Pi / nbrScrew )

    val base = apply(outer1, outer2, inner, height, nbrSlits, slitWidth, wall)
    base -- screws

  }

  //TODO some more parameters
  def wrench(outer: Double, inner: Double, tolerance: Double) = {
    val base = roundedCubeH(60, 20, 5, 3).move(-30, -10, 0)
    val t = Thread.ISO.M2
    val screw = Cylinder(t+tolerance, 5)
    val hole = Hull(screw.moveX(inner + t), screw.moveX(outer - t))
    base - Cylinder(3, 5) -- (0 until 6).map( i => hole.rotateZ(i * Pi / 3) )
  }

}
