package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._

class Frame(var mainBeamLength: Double = 250,
            var clearance: Double = 20,
            var topAngle: Double = Pi/4,
            var topLength: Double = 100,
            var connectorThickness: Double = 3,
            var connectorKnobs: Double = 5,
            var connectorTolerance: Double = Common.tightTolerance) {

  val ew = Extrusion.width
  val ew2 = ew / 2

  def beam(length: Double) = Extrusion(length).moveZ(clearance)
  //protected def beam(length: Double) = Extrusion.placeHolder(length).moveZ(clearance)

  protected def totalLength(beamLength: Double, clearance: Double) =
    beamLength + 2 * clearance

  protected def putAtCorners(s: Solid, radius: Double) = {
    val s2 = for (i <- 0 until 6) yield s.moveX(radius).rotateZ(i * Pi/3)
    Union(s2:_*)
  }

  protected def beamHexaH(beamLength: Double, clearance: Double) = {
    val b = beam(beamLength).rotateX(-Pi/2) //horizontal toward Y axis
    val beamAtCorner = b.rotateZ(Pi/6)
    putAtCorners(beamAtCorner, totalLength(beamLength, clearance))
  }

  protected def beamHexaV(beamLength: Double, clearance: Double) = {
    putAtCorners(beam(beamLength), totalLength(beamLength, clearance))
  }
  
  protected def beamHexaC(beamLength: Double, clearance: Double) = {
    val b = beam(beamLength).rotateY(-Pi/2) //horizontal toward X axis
    putAtCorners(b, 0)
  }

  protected def beamHexaTop(tiltAngle: Double,
                            beamLength: Double,
                            clearance: Double,
                            mainBeamLength: Double) = {
    val b = beam(beamLength).rotateY(-tiltAngle) //tilting toward center
    putAtCorners(b, totalLength(mainBeamLength, clearance))
  }

  private def cl = clearance
  def tl = totalLength( mainBeamLength, clearance)
  def topHeight = tl + cos(topAngle) * topLength

  def skeleton = {
    val topVBeamLength = topLength - 2 * cl
    val topHBeamLength = tl - topLength * sin(topAngle) - 2 * cl
    Union(
      beamHexaH(mainBeamLength, cl),
      beamHexaH(mainBeamLength, cl).moveZ(tl),
      beamHexaV(mainBeamLength, cl),
      beamHexaC(mainBeamLength, cl),
      beamHexaTop(topAngle, topVBeamLength, cl, mainBeamLength).moveZ(tl),
      beamHexaH(topHBeamLength, cl).moveZ(topHeight),
      beamHexaC(topHBeamLength, cl).moveZ(topHeight)
    )
  }

  protected def c0 = Extrusion.connector(connectorThickness, connectorKnobs, connectorTolerance)

  protected def connectorCorner = {
    //TODO
    ???
  //val ct = connectorThickness
  //val ci = cl - ct
  //val inner = sqrt(ci*ci + ct*ct) - ci
  //val outer = inner + ct
  //val p = PieSlice(outer, inner, Pi/3, ew)
  //p.move(-outer, ew2, -ew2)
  }

  def connectorBottom = {
    val b1 = centeredCubeXY(ew, ew, connectorThickness).moveZ(cl-connectorThickness)
    val b2 = b1.rotateY( Pi/2)
    //TODO corners using pieSlice instead of Hexagon/Hull
    val block = Hull(
      b1,
      b2,
      b2.rotateZ( Pi/3),
      b2.rotateZ(-Pi/3),
      b1.moveZ(connectorThickness-cl -ew2)
    )
    val c1 = c0.moveZ(cl)
    val c2 = c1.rotateY( Pi/2)
    Union(
      c1,
      c2,
      c2.rotateZ( Pi/3),
      c2.rotateZ(-Pi/3),
      block
    )
  }
  
  def connectorMiddle = {
    ???
  }

  def connectorTop = {
    ???
  }

  def connectorCenter = {
    val c1 = c0.rotateY( Pi/2) + connectorCorner
    putAtCorners(c1, cl)
  }

  def connections = {
    Union(
      connectorCenter,
      connectorCenter.moveZ(topHeight)
    )
  }

  def full = skeleton + connections

  //TODO the different types of connector
  //TODO the diagonal things (rigidity) → some form of static rope


}

object Frame {

  def apply(mainBeamLength: Double,
            clearance: Double,
            topAngle: Double,
            topLength: Double) = {
    val f = new Frame(mainBeamLength, clearance, topAngle, topLength)
    //f.connections
    //f.full
    //f.connectorBottom
    f.connectorCenter
  }

}


//place holder for 20x20mm aluminium extrusions
object Extrusion {

  val width = 20

  protected def centerHole(length: Double) = Cylinder(2.1, length+2).moveZ(-1)

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
    withInner - centerHole(length)
  }
  
  def placeHolder(length: Double) = centeredCubeXY(20, 20, length)

  def connector(plateThicknesss: Double,
                knobHeight: Double,
                tolerance: Double = Common.tightTolerance) = {
    val base = centeredCubeXY(20, 20, plateThicknesss + knobHeight).moveZ(-plateThicknesss)
    val negative = bigger(apply(knobHeight), tolerance).moveZ(tolerance/2)
    val hole = centerHole(knobHeight + plateThicknesss).moveZ(-plateThicknesss)
    Difference(
      base,
      negative,
      hole,
      centeredCubeXY(4,4,knobHeight+1).move( 10, 10, 0),
      centeredCubeXY(4,4,knobHeight+1).move( 10,-10, 0),
      centeredCubeXY(4,4,knobHeight+1).move(-10, 10, 0),
      centeredCubeXY(4,4,knobHeight+1).move(-10,-10, 0)
    )
  }

}
