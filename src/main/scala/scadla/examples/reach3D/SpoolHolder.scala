package scadla.examples.reach3D

import scadla._
import utils._
import Trig._
import InlineOps._
import thread.{ISO, StructuralNutPlaceHolder}
import squants.space.{Length, Angle}
import scala.language.postfixOps
import squants.space.LengthConversions._
import scala.collection.parallel.CollectionConverters._

object SpoolHolder {

  val t = ISO.M6
  
  val looseTolerance = 0.2 mm

  val bbRadius = 3.0 mm
  val bearingGap = 1.0 mm
  val grooveDepth = bbRadius / cos(Pi/4)

  // make sures the BBs fit nicely
  def adjustGrooveRadius(radius: Length): Length = {
    assert(radius > bbRadius)
    // find n such that the circumscribed radius is the closest to the given radius
    val sideLength = 2*bbRadius + looseTolerance
    val nD = Pi / asin(sideLength / radius / 2)
    val n = nD.toInt // rounding to nearest regular polygon
    circumscribedRadius(n, sideLength)
  }

  def flatGroove(radius: Length, depth: Length, angle: Angle = Pi/2, undercut: Length = 0.5 mm) = {
    val width = depth / tan(Pi/2 - angle/2)
    val outer = Cylinder(radius + width, radius, depth)
    val inner = Cylinder(radius - width, radius, depth)
    (outer - inner) * Cylinder(radius + width, depth - undercut)
  }

  def radialGroove(radius: Length, depth: Length, angle: Angle = Pi/2, undercut: Length = 0.5 mm) = {
    val width = (depth / tan(Pi/2 - angle/2)).abs
    val middleRadius = radius - depth
    val body = Cylinder(radius, 2*width)
    val top = Cylinder(middleRadius, radius, width).moveZ(width)
    val bot = Cylinder(radius, middleRadius, width)
    val shape =
      if (depth >= (0 mm)) body - top - bot - Cylinder(middleRadius + undercut, 2*width)
      else (top + bot - body) * Cylinder(middleRadius - undercut, 2*width)
    shape.moveZ(-width)
  }

  def steppedCone(baseRadius: Length, steps: Int, stepRadiusDec: Length, stepHeight: Length) = {
    Union((0 until steps).map( i => Cylinder(baseRadius - i * stepRadiusDec, stepHeight).moveZ(i * stepHeight) ): _*)
  }

  def stemShape(radius1: Length, height1: Length, radius2: Length, height2: Length, bevel: Length) = {
    val c1 = Cylinder(radius1, height1)
    val c2 = Cylinder(radius2, height2).moveZ(height1)
    val bev = if (radius1 > radius2) Cylinder(radius2 + bevel, radius2, bevel).moveZ(height1)
              else Cylinder(radius1, radius1 + bevel, bevel).moveZ(height1 - bevel)
    c1 + c2 + bev - Cylinder(t, height1 + height2)
  }

  ////////////////////////
  // hard-coded numbers //
  ////////////////////////

  // bearings
  val radialBearingRadius = adjustGrooveRadius(t + (6 mm) + bearingGap / 2)
  val flatBearingRadius = adjustGrooveRadius(25 mm)
  val groove1 = flatGroove(flatBearingRadius, grooveDepth - bearingGap / 2)
  val groove2a = radialGroove(radialBearingRadius,  grooveDepth)
  val groove2b = radialGroove(radialBearingRadius, -grooveDepth)

  // cone dimensions
  val coneLength = 18 mm
  val coneMaxRadius = 45 mm
  val coneSteps = 14
  val stepHeight = 1.2 mm //1.1171875
  val stepRadius = 2 mm

  val radialBearingPos1 = 4.8 mm //3.6 // or 4.8
  val radialBearingPos2 = coneLength - radialBearingPos1

  val stem = {
    val base = stemShape(30 mm, 5 mm, radialBearingRadius - bearingGap / 2, coneLength + bearingGap, 1 mm)
    val grooves = List(
      groove1.mirror(0,0,1),
      groove2a.moveZ(bearingGap + radialBearingPos1),
      groove2a.moveZ(bearingGap + radialBearingPos2)
    )
    base -- grooves.map(_.moveZ(5 mm)) - (new StructuralNutPlaceHolder).M6
  }

  val cone = {
    val screw = (Cylinder(1.25 mm, 16 mm) + Cylinder(3 mm, 8 mm).moveZ(16 mm)).moveZ(1 mm)
    val screws = (0 until 3).map( i => { screw.moveX(radialBearingRadius + grooveDepth + (2 mm)).rotateZ(2 * Pi / 3 * i) })
    val toRemove = screws ++ Seq(
        Cylinder(radialBearingRadius + bearingGap/2, coneLength),
        groove1,
        groove2b.moveZ(radialBearingPos1),
        groove2b.moveZ(radialBearingPos2)
      )
    val baseHeight = coneLength - coneSteps * stepHeight
    val base = Cylinder(coneMaxRadius, baseHeight + (0.001 mm)) + steppedCone(coneMaxRadius, coneSteps, stepRadius, stepHeight).moveZ(baseHeight)
    base -- toRemove
  }

  val conePart1 = cone * Cylinder(coneMaxRadius, radialBearingPos1)
  val conePart2 = cone * Cylinder(coneMaxRadius, radialBearingPos2 - radialBearingPos1 - (0.01 mm)).moveZ(radialBearingPos1 + (0.005 mm))
  val conePart3 = cone * Cylinder(coneMaxRadius, radialBearingPos1).moveZ(radialBearingPos2 + (0.005 mm))
  
  def main(args: Array[String]): Unit = {
    //backends.Renderer.default.view(stem)
    Seq(
      stem -> "stem.stl",
      conePart1 -> "conePart1.stl",
      conePart2 -> "conePart2.stl",
      conePart3 -> "conePart3.stl"
    ).par.foreach{ case (obj, name) =>
      backends.Renderer.default.toSTL(obj, name)
    }
  }

}
