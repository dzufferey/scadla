package dzufferey.scadla.examples.cnc

import math._
import dzufferey.scadla._
import utils._
import InlineOps._
import dzufferey.scadla.examples.fastener._
import Common._

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
               mNumber: Double, screwRadius: Double) = {
    val innerC = Cylinder(inner+tolerance, height)
    val base = Cylinder(outer1, outer2, height)
    val slts = slits(mNumber, height, nbrSlits, slitWidth, wall)
    val thread = new MetricThread(tolerance).screwThreadIsoOuter(mNumber, height, 2)
    val wrenchHoles = for(i <- 0 until nbrSlits) yield
      Cylinder(screwRadius+tolerance, height).moveX((outer2+inner)*2/3).rotateZ(i * 2 * Pi / nbrSlits)
    thread + base -- slts - innerC -- wrenchHoles
  }

  //TODO some more parameters
  def wrench(outer: Double, inner: Double, nbrSlits: Int, screwRadius: Double) = {
    val base = roundedCubeH(60, 20, 5, 3).move(-30, -10, 0)
    val t = Thread.ISO.M2
    val screw = Cylinder(t+tolerance, 5)
    val hole = Hull(screw.moveX(inner + t), screw.moveX(outer - t))
    base - Cylinder(3, 5) -- (0 until nbrSlits).map( i => hole.rotateZ(2 * i * Pi / nbrSlits) )
  }

}
