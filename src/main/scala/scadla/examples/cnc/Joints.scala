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
  
  //println("size:" + 2*outerRadius) → 21.28823512814129

  def cross(length: Double, height: Double, screwRadius: Double): Solid = {
    val h = Hexagon(height/2, length).moveZ(-length/2)
    val c1 = h.rotateX(Pi/2)
    val c2 = h.rotateZ(Pi/6).rotateY(Pi/2)
    val s = Cylinder(screwRadius, length+2).moveZ(-length/2-1)
    val s1 = s.rotateX(Pi/2)
    val s2 = s.rotateY(Pi/2)
    c1 + c2 - s1 - s2
  }

  def cross: Solid = {
    cross(2*(minRadius - washerThickness - tightTolerance), 2*sideWall+1, M3-0.5)
  }

  protected def carving(top: Double, bottom: Double) = {
    val r = outerRadius-2*M3+looseTolerance
    val h = 2*outerRadius + 2
    Cylinder(r, h).moveZ(-h/2.0).rotateY(Pi/2).moveZ(r).scaleZ((top-bottom)/r)
  }

  protected def addScrewThingy(base: Solid, height: Double) = {
    val r = 2*M3 + 2
    val l = 2 * outerRadius
    val l2 = outerRadius
    val c0 = Cylinder(r-0.01,l+10).moveZ(-l2-5).rotateY(Pi/2).moveZ(height)
    val c1 = Cylinder(r,l-2).moveZ(-l2+1).rotateY(Pi/2).moveZ(height)
    val c2 = Cylinder(r+0.01,2*minRadius).moveZ(-minRadius).rotateY(Pi/2).moveZ(height)
    val c3 = Cylinder(M3, l+10).moveZ(-l2-5).rotateY(Pi/2).moveZ(height)
    val sh = Cylinder(r+looseTolerance, sideWall).rotateY(Pi/2).moveZ(height)
    base - c0 + c1 - c2 - c3 - sh.moveX(outerRadius - 1) - sh.moveX(-outerRadius-sideWall + 1)
  }

  def bottom(height: Double) = {
    val nutTop = botWall + bottomNut * 1.5
    val base = Difference(
      Cylinder(outerRadius, height),
      Hexagon(minRadius, height).moveZ(botWall).rotateZ(Pi/6),
      Cylinder(bottomNut + tolerance, height),
      carving(height, nutTop).move(0,  outerRadius, nutTop - 0.5),
      carving(height, nutTop).move(0, -outerRadius, nutTop - 0.5)
    )
    addScrewThingy(base, height)
  }

  def top(height: Double, baseFull: Double, screwRadius: Double) = {
    val size = math.ceil(2*outerRadius)
    val s = Cylinder(screwRadius, size+2).moveZ(-size/2-1)
    val delta = screwRadius + 0.1
    val base = Difference(
      RoundedCubeH(size, size, height, 2.5).move(-size/2.0, -size/2.0, 0),
      Hexagon(minRadius, height).moveZ(baseFull).rotateZ(Pi/6),
      carving(height, baseFull).move(0,  outerRadius, baseFull - 0.5),
      carving(height, baseFull).move(0, -outerRadius, baseFull - 0.5),
      //holes for mounting screws
      s.rotateX(Pi/2).moveZ(4-delta),
      s.rotateX(Pi/2).moveZ(baseFull-3-delta),
      s.rotateY(Pi/2).moveZ(4+delta),
      s.rotateY(Pi/2).moveZ(baseFull-3+delta)
    )
    addScrewThingy(base, height)
  }

  def parts = Seq(
    cross,
    bottom(20),
    top(30, 16, M3-0.15)
  )


}
