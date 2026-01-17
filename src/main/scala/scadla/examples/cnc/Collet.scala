package scadla.examples.cnc

import math.*
import scadla.*
import utils.*
import InlineOps.*
import thread.*
import Common.*
import scadla.EverythingIsIn.{millimeters, radians}  
import squants.space.Length

object Collet {

  def slits(outer: Length, height: Length, nbrSlits: Int, slitWidth: Length, wall: Length) = {
    assert(nbrSlits % 2 == 0, "number of slits must be even")
    val slit = Cube(slitWidth, outer, height - wall).moveX(-slitWidth/2)
    for(i <- 0 until nbrSlits) yield slit.rotateZ(i * 2 * Pi / nbrSlits).moveZ((i % 2) * wall)
  }

  //example: Collet(6, 7, 3, 20, 6, 1, 2)
  def apply(outer1: Length, outer2: Length, inner: Length, height: Length,
            nbrSlits: Int, slitWidth: Length, wall: Length) = {
    val base = Cylinder(outer1, outer2, height) - Cylinder(inner, height)
    base -- slits(outer1 max outer2, height, nbrSlits, slitWidth, wall)
  }

  def threaded(outer1: Length, outer2: Length, inner: Length, height: Length,
               nbrSlits: Int, slitWidth: Length, wall: Length,
               mNumber: Length, screwRadius: Length) = {
    val innerC = Cylinder(inner+tolerance, height)
    val base = Cylinder(outer1, outer2, height)
    val slts = slits(mNumber, height, nbrSlits, slitWidth, wall)
    val thread = new MetricThread(tolerance).screwThreadIsoOuter(mNumber, height, 2)
    val wrenchHoles = for(i <- 0 until nbrSlits) yield
      Cylinder(screwRadius+tolerance, height).moveX((outer2+inner)*2/3).rotateZ( (i*2+1) * Pi / nbrSlits)
    thread + base -- slts - innerC -- wrenchHoles
  }

  //TODO some more parameters
  def wrench(outer: Length, inner: Length, nbrSlits: Int, screwRadius: Length) = {
    val base = RoundedCubeH(60, 20, 5, 3).move(-30, -10, 0)
    val t = screwRadius //Thread.ISO.M2
    val screw = Cylinder(t+tolerance, 5)
    val hole = Hull(screw.moveX(inner + t), screw.moveX(outer - t))
    base - Cylinder(3, 5) -- (0 until nbrSlits).map( i => hole.rotateZ((2*i+1) * Pi / nbrSlits) )
  }

}
