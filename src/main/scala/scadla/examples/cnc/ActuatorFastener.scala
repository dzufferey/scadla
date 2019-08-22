package scadla.examples.cnc

import math._
import scadla._
import utils._
import InlineOps._
import thread._
import Common._
import scadla.EverythingIsIn.{millimeters, radians}  

// dX, dZ are given from the center of the extrusion
class ActuatorFasterner(dX: Double, dZ: Double) {
  import backends.renderers.Renderable._
  import backends.renderers.OpenScad._

  assert(dX >= 20 || dZ >= 20, "too close")
  assert(dX >= 0 && dZ >= 0, "backward")

  var bearingPosition = 0.5 // [0, 1]
  var thickness = 6.0
  var bearingRetainer = 2.0
  var supportLength = 40.0
  var guideDepth = 2
  var thread = ISO.M3
  var beamConnectorRounding = 2
  var nbrScrewsPerSide = 2
  
  def beamConnector = {
    val guide = Trapezoid(4.5, 6.5, supportLength, guideDepth, -0.2)
    val faceBlank = Union(
        Cube(20-beamConnectorRounding, thickness, supportLength).move(-thickness+beamConnectorRounding, -thickness, 0),
        PieSlice(thickness, 0, Pi/2, supportLength).toSolid.rotateZ(-Pi/2).move(20-thickness,0,0),
        Cylinder(beamConnectorRounding, supportLength).move(-thickness+beamConnectorRounding, -thickness+beamConnectorRounding, 0),
        guide.rotateX(Pi/2).rotateZ(Pi).move(10+6.5/2, 0, 0)
    )
    val screw = Cylinder(thread, thickness + guideDepth + 2).rotateX(Pi/2).move(10, guideDepth +1, 0)
    val screwDistance = (supportLength - thickness) / nbrScrewsPerSide
    val screws = for (i <- 0 until nbrScrewsPerSide) yield screw.moveZ(thickness + screwDistance * (i + 0.5))
    val face = faceBlank -- screws
    face + face.mirror(1,-1,0)
  }

  def connector(supportDirection: Boolean) = {
    val bc1 = beamConnector.rotateZ(Pi).move(10, 10, 0)
    val bc2 = beamConnector.move( dX-10, dZ-10, 0)
    val beam0 = Hull(
      bc1 * Cube(50, 50, thickness).move(-30, -30, 0),
      bc2 * Cube(50, 50, thickness).move( dX-30, dZ-30,0),
      Cylinder(11 + thickness, thickness).move( dX*bearingPosition, dZ*bearingPosition, 0)
    )
    val beam = Difference(
      beam0,
      Cylinder(11 + looseTolerance, thickness).move( dX*bearingPosition, dZ*bearingPosition, bearingRetainer),
      Cylinder(9, thickness).move( dX*bearingPosition, dZ*bearingPosition, 0),
      Cube(40, 40, thickness).move( -30, -30, 0),
      Cube(40, 40, thickness).move( dX-10, dZ-10,0)
    )
    val support: Solid = beam + bc1 + bc2
    if (supportDirection) support
    else support.mirror(0,0,1)
  }

}
