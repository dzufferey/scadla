package scadla.examples.cnc

import math._
import scadla._
import utils._
import InlineOps._
import scadla.examples.fastener._
import Common._

/** A 2 degree of freedom joint.
 *  @param bottomNut is the size of bottom thread/nut
 */
class Joint2DOF(bottomNut: Double = Thread.ISO.M8) {

  import Thread.ISO.{M3,M8}

  //max outer radius of the nut
  val radius = nut.maxOuterRadius(bottomNut)
  // bottom wall, e.g., bellow the nut
  val botWall = 2
  // side wall, e.g., around the nut
  val sideWall = 3
  val outerRadius = radius + sideWall
  val minRadius = nut.minOuterRadius(bottomNut) + looseTolerance
  val washerThickness = 0.6

  def cross(length: Double, height: Double, screwRadius: Double): Solid = {
    //XXX hex instead of cubes ???
    val h = Hexagon(height/2, length).moveZ(-length/2)
    val c1 = h.rotateX(Pi/2)
    val c2 = h.rotateZ(Pi/6).rotateY(Pi/2)
    val s = Cylinder(screwRadius, length+2).moveZ(-length/2-1)
    val s1 = s.rotateX(Pi/2)
    val s2 = s.rotateY(Pi/2)
    c1 + c2 - s1 - s2
  }

  def cross: Solid = {
    cross(2*(minRadius - washerThickness), 2*sideWall, M3-0.5)
  }

  protected def carving(top: Double, bottom: Double) = {
    val r = outerRadius-2*M3+looseTolerance
    val h = 2*outerRadius + 2
    Cylinder(r, h).moveZ(-h/2.0).rotateY(Pi/2).moveZ(r).scaleZ((top-bottom)/r)
  }

  protected def addScrewThingy(base: Solid, height: Double) = {
    val c0 = Cylinder(2*M3,2*outerRadius).moveZ(-outerRadius).rotateY(Pi/2).moveZ(height)
    val c1 = Cylinder(2*M3,2*outerRadius-2).moveZ(-outerRadius+1).rotateY(Pi/2).moveZ(height)
    val c2 = Cylinder(2*M3+0.01,2*minRadius).moveZ(-minRadius).rotateY(Pi/2).moveZ(height)
    val c3 = Cylinder(M3, 2*outerRadius+2).moveZ(-outerRadius-1).rotateY(Pi/2).moveZ(height)
    val sh = Cylinder(2*M3+0.01, sideWall).rotateY(Pi/2).moveZ(height)
    base - c0 + c1 - c2 - c3 - sh.moveX(outerRadius - 1.5) - sh.moveX(-outerRadius-sideWall + 1.5)
  }

  def bottom(height: Double) = {
    val nutTop = botWall + bottomNut * 1.5
    val base = Difference(
      Cylinder(outerRadius, height),
      Hexagon(minRadius, height).moveZ(nutTop).rotateZ(Pi/6),
      nut(bottomNut).moveZ(botWall).rotateZ(Pi/6),
      Cylinder(bottomNut + tolerance, height),
      carving(height, nutTop).move(0,  outerRadius, nutTop - 0.5),
      carving(height, nutTop).move(0, -outerRadius, nutTop - 0.5)
    )
    addScrewThingy(base, height)
  }

  def top(height: Double, baseFull: Double) = {
    val base = Difference(
      CenteredCube.xy(2*outerRadius, 2*outerRadius, height),
      Hexagon(nut.minOuterRadius(bottomNut) + looseTolerance, height).moveZ(baseFull).rotateZ(Pi/6),
      carving(height, baseFull).move(0,  outerRadius, baseFull - 0.5),
      carving(height, baseFull).move(0, -outerRadius, baseFull - 0.5)
    )
    addScrewThingy(base, height)
  }

  def parts = Seq(
    cross,
    bottom(20),
    top(30, 18)
  )


}
