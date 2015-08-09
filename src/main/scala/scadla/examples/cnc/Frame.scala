package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._

object Frame {

  protected def beam(length: Double) = Extrusion(length)
  //protected def beam(length: Double) = Extrusion.placeHolder(length)

  protected def totalLength(beamLength: Double, clearance: Double) =
    beamLength + 2 * clearance

  protected def putAtCorners(s: Solid, radius: Double) = {
    val s2 = for (i <- 0 until 6) yield s.moveX(radius).rotateZ(i * Pi/3)
    Union(s2:_*)
  }

  protected def beamHexaH(beamLength: Double, clearance: Double) = {
    val b = beam(beamLength).moveZ(clearance).rotateX(-Pi/2) //horizontal toward Y axis
    val beamAtCorner = b.rotateZ(Pi/6)
    putAtCorners(beamAtCorner, totalLength(beamLength, clearance))
  }

  protected def beamHexaV(beamLength: Double, clearance: Double) = {
    val b = beam(beamLength).moveZ(clearance)
    putAtCorners(b, totalLength(beamLength, clearance))
  }
  
  protected def beamHexaC(beamLength: Double, clearance: Double) = {
    val b = beam(beamLength).moveZ(clearance).rotateY(-Pi/2) //horizontal toward X axis
    putAtCorners(b, 0)
  }

  protected def beamHexaTop(tiltAngle: Double,
                            beamLength: Double,
                            clearance: Double,
                            mainBeamLength: Double) = {
    val b = beam(beamLength).moveZ(clearance).rotateY(-tiltAngle) //tilting toward center
    putAtCorners(b, totalLength(mainBeamLength, clearance))
  }

  def apply(mainBeamLength: Double,
            clearance: Double,
            topAngle: Double,
            topLength: Double) = {
    val tl = totalLength( mainBeamLength, clearance)
    val topVBeamLength = topLength - 2 * clearance
    val topHBeamLength = tl - topLength * sin(topAngle) - 2 * clearance
    val topHeight = tl + cos(topAngle) * topLength
    Union(
      beamHexaH(mainBeamLength, clearance),
      beamHexaH(mainBeamLength, clearance).moveZ(tl),
      beamHexaV(mainBeamLength, clearance),
      beamHexaC(mainBeamLength, clearance),
      beamHexaTop(topAngle, topVBeamLength, clearance, mainBeamLength).moveZ(tl),
      beamHexaH(topHBeamLength, clearance).moveZ(topHeight),
      beamHexaC(topHBeamLength, clearance).moveZ(topHeight)
    )
  }

}


//place holder for 20x20mm aluminium extrusions
object Extrusion {

  def apply(length: Double) = {
    val base = roundedCubeH(20,20, length, 1.5).move(-10, -10, 0)
    val shell = Difference(
      base,
      centeredCubeXY(16, 16, length + 1).moveZ(-1),
      centeredCubeXY(22, 6.5, length + 1).moveZ(-1),
      centeredCubeXY(6.5, 22, length + 1).moveZ(-1)
    )
    val withInner = Union(
      shell,
      centeredCubeXY(8, 8, length), //center
      centeredCubeXY(3, 3, length).move(-7.5, -7.5, 0), //corner
      centeredCubeXY(3, 3, length).move(-7.5,  7.5, 0), //corner
      centeredCubeXY(3, 3, length).move( 7.5, -7.5, 0), //corner
      centeredCubeXY(3, 3, length).move( 7.5,  7.5, 0), //corner
      centeredCubeXY(1.75, 25, length).rotateZ( Pi/4), //cross
      centeredCubeXY(1.75, 25, length).rotateZ(-Pi/4)  //cross
    )
    val centerHole = Cylinder(2.1, length+2).moveZ(-1)
    withInner - centerHole
  }

  def placeHolder(length: Double) = centeredCubeXY(20, 20, length)

}
