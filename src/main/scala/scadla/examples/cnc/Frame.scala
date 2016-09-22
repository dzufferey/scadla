package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import scadla.examples.extrusion._2020

//TODO something is wrong with the topAngle and the topLength

class Frame(var mainBeamLength: Double = 250,
            var clearance: Double = 18, //for 20x20mm extrusions
            var topAngle: Double = Pi/4,
            var topLength: Double = 120,
            var connectorThickness: Double = 3,
            var connectorKnobs: Double = 5,
            var connectorBaseRatio: Double = 0.75,
            var connectorTolerance: Double = Common.tightTolerance) {

  val ew = _2020.width
  val ew2 = ew / 2

  def beam(length: Double) = _2020(length).moveZ(clearance)

  protected def totalLength(beamLength: Double, clearance: Double) =
    beamLength + 2 * clearance

  protected def putAtCorners(s: Solid, radius: Double) = {
    val s2 = for (i <- 0 until 6) yield s.moveX(radius).rotateZ(i * Pi/3) //linter:ignore ZeroDivideBy
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
  
  protected def beamHexaC(beamLength: Double) = {
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
  def topVBeamLength = topLength - 2 * cl
  def topHBeamLength = tl - topLength * sin(topAngle) - 2 * cl

  def skeleton = {
    Union(
      beamHexaH(mainBeamLength, cl),
      beamHexaH(mainBeamLength, cl).moveZ(tl),
      beamHexaV(mainBeamLength, cl),
      beamHexaC(mainBeamLength),
      beamHexaTop(topAngle, topVBeamLength, cl, mainBeamLength).moveZ(tl),
      beamHexaH(topHBeamLength, cl).moveZ(topHeight),
      beamHexaC(topHBeamLength).moveZ(topHeight)
    )
  }
  
  //TODO the diagonal things (rigidity) → some form of static rope

  private def ct = connectorThickness
  protected def c0 = _2020.connector(connectorThickness, connectorKnobs, connectorTolerance)
  protected def c0Base = CenteredCube.xy(ew, ew, connectorThickness).moveZ(-connectorThickness)
  protected def c0Hole = Cylinder(Thread.ISO.M5, connectorThickness+2).moveZ(-connectorThickness-1)

  protected def connectorCorner = {
    val α = atan2(ew2, cl) - 1e-6
    val β = Pi/3 - 2 * α
    val inner = (cl - ct) / cos(α)
    val outer = cl / cos(α)
    val p = PieSlice(outer, inner, β, ew)
    p.rotateZ(α).moveZ(-ew2)
  }

  protected def connectorCornerHalf = {
    val α = atan2(ew2, cl) - 1e-6
    val β = Pi/3 - 2 * α
    val inner = (cl - ct) / cos(α)
    val outer = cl / cos(α)
    val p = PieSlice(outer, inner, β/2, ew)
    (p.rotateZ(α).moveZ(-ew2),
     p.rotateZ(α + β/2 - 5e-7).moveZ(-ew2))
  }
  
  //the corners' corners
  protected def connectorCornerTriple = {
    val (ch1, ch2) = connectorCornerHalf
    val c0 = ch1.moveZ(ew2)
    val c1 = ch2.moveZ(ew2)
    val c2 = connectorCorner.moveZ(-ew2).rotateX(Pi/2)
    val c3 = connectorCorner.moveZ(ew2).rotate(Pi/2, 0, Pi/3)
    Union(c0 * c2, c1 * c3)
  }
  protected def connectorCornerDouble = {
    val c0 = connectorCorner.moveZ(ew2)
    val c1 = connectorCorner.moveZ(-ew2).rotateX(Pi/2)
    val c2 = connectorCorner.moveY(ew2 * (sqrt(2) - 1))rotateX(Pi/4)
    Intersection(c0, c1, c2)
  }


  def connectorPositions(s: Solid, idx: Int) = {
    val ta = topAngle
    val ba = topAngle + Pi
    idx match {
      case 0 => s.moveZ(cl).rotate(   0,    0,     0)  // 0 top
      case 1 => s.moveZ(cl).rotate(   0, Pi/2,     0)  // 1 middle center
      case 2 => s.moveZ(cl).rotate(   0, Pi/2,  Pi/3)  // 2 middle left
      case 3 => s.moveZ(cl).rotate(   0, Pi/2, -Pi/3)  // 3 middle right
      case 4 => s.moveZ(cl).rotate(   0,   Pi,     0)  // 4 bottom
      case 5 => s.moveZ(cl).rotate(   0,   ta,     0)  // 5 top inclined
      case 6 => s.moveZ(cl).rotate(   0,   ba,     0)  // 6 bottom inclined
      case other => sys.error("out of bounds: " + other)
    }
  }
  
  def connectorAt(idx: Int) = connectorPositions(c0, idx)
  def connectorBaseAt(idx: Int) = connectorPositions(c0Base, idx)
  def connectorHoleAt(idx: Int) = connectorPositions(c0Hole, idx)

  def cornerAt(idx: Int) = {
    val c = connectorCorner
    idx match {
      case  0 => c.rotate(    0,     0,     0)  //  0  middle left
      case  1 => c.rotate(   Pi,     0,     0)  //  1  middle right
      case  2 => c.rotate( Pi/2,     0,     0)  //  2  middle up center
      case  3 => c.rotate( Pi/2,     0,  Pi/3)  //  3  middle up left
      case  4 => c.rotate( Pi/2,     0, -Pi/3)  //  4  middle up right
      case  5 => c.rotate(-Pi/2,     0,     0)  //  5  middle down center
      case  6 => c.rotate(-Pi/2,     0,  Pi/3)  //  6  middle down left
      case  7 => c.rotate(-Pi/2,     0, -Pi/3)  //  7  middle down right
      case  8 => c.rotate(    0, -Pi/2,    Pi)  //  8  top right
      case  9 => c.rotate( Pi/2, -Pi/2,    Pi)  //  9  top center
      case 10 => c.rotate(   Pi, -Pi/2,    Pi)  // 10  top left
      case 11 => c.rotate(    0,  Pi/2,    Pi)  // 11  bottom right
      case 12 => c.rotate( Pi/2,  Pi/2,    Pi)  // 12  bottom center
      case 13 => c.rotate(   Pi,  Pi/2,    Pi)  // 13  bottom left
    }
  }
  
  def cornerDoubleAt(idx: Int) = {
    val c = connectorCornerDouble
    idx match {
      case 0 => c.rotate(   0,-Pi/2,  -Pi)  //  0  up ???
      case 1 => c.rotate(   0,-Pi/2,-Pi/2)  //  1  up ???
      case 2 => c.rotate(   0, Pi/2,  -Pi)  //  2  down ???
      case 3 => c.rotate(   0, Pi/2,-Pi/2)  //  3  down ???
      case other => sys.error("out of bounds: " + other)
    }
  }

  def cornerTripleAt(idx: Int) = {
    val c = connectorCornerTriple
    idx match {
      case 0 => c.rotate(   0,   0, -Pi/3)  //  0  middle up left
      case 1 => c.rotate(   0,   0,     0)  //  1  middle up right
      case 2 => c.rotate(  Pi,   0,     0)  //  2  middle down left
      case 3 => c.rotate(  Pi,   0,  Pi/3)  //  3  middle down right
      case other => sys.error("out of bounds: " + other)
    }
  }

  def innerBase = c0Base.scale(connectorBaseRatio,connectorBaseRatio,1).moveZ(-ct)

  def connectorBottom = {

    val base = {
      val b0 = Cube(ct, ew, ct).move(ew2, -ew2, -ct)
      val b1 = b0.moveZ(cl).rotateY(Pi/2)
      Hull(
        b0.move(-ew, 0, -ew2),
        b1,
        b1.rotateZ( Pi/3),
        b1.rotateZ(-Pi/3)
      ).moveZ(ct)
    }

    val outer = Hull(
      connectorBaseAt(0),
      connectorBaseAt(1),
      connectorBaseAt(2),
      connectorBaseAt(3),
      cornerAt(0),
      cornerAt(1),
      cornerAt(2),
      cornerAt(3),
      cornerAt(4),
      cornerAt(8),
      cornerAt(9),
      cornerAt(10),
      cornerTripleAt(0),
      cornerTripleAt(1),
      cornerDoubleAt(0),
      cornerDoubleAt(1)
    )

    val inner = {
      val i0 = Hull(
        connectorPositions(innerBase, 0),
        connectorPositions(innerBase, 1),
        connectorPositions(innerBase, 2),
        connectorPositions(innerBase, 3)
      )
      Hull(
        i0,
        i0.move(-3*ct, 0, 0),
        i0.move(-2*ct, ct, 0),
        i0.move(-2*ct,-ct, 0)
      )
    }


    val pSize = 5.0
    val p0 = {
      val c = Cube(pSize, pSize, cl+ew2)
      Intersection(c, c.rotateZ(Pi/4)).move(-pSize/2, -pSize/2, -ew2)
    }
    val pillars = Union(
      p0.move(-ew2+pSize/2, -ew2+pSize/2, 0),
      p0.move(-ew2+pSize/2,  ew2-pSize/2, 0)
    )

    Union(
      base,
      pillars,
      connectorAt(0),
      connectorAt(1),
      connectorAt(2),
      connectorAt(3),
      Difference(
        outer,
        inner,
        connectorHoleAt(0),
        connectorHoleAt(1),
        connectorHoleAt(2),
        connectorHoleAt(3)
      )
      //TODO to attach cables
      //TODO hole at bottom for screwdriver
    )
  }
  
  def connectorMiddle = {
    val outer = Hull(
      connectorBaseAt(2),
      connectorBaseAt(3),
      connectorBaseAt(4),
      connectorBaseAt(5),
      cornerAt(5),
      cornerAt(6),
      cornerAt(7),
      cornerTripleAt(2),
      cornerTripleAt(3),
      cornerAt(11),
      cornerAt(12),
      cornerAt(13),
      cornerDoubleAt(2),
      cornerDoubleAt(3)
    )
    val inner = {
      val i0 = Hull(
        connectorPositions(innerBase, 2),
        connectorPositions(innerBase, 3),
        connectorPositions(innerBase, 4),
        connectorPositions(innerBase, 5)
      )
      Hull(
        i0,
        i0.move(-3*ct, 0, 0),
        i0.move(-2*ct, ct, 0),
        i0.move(-2*ct,-ct, 0),
        i0.move(-ct*cos(topAngle), 0, ct*sin(topAngle))
      )
    }
    Union(
      connectorAt(2),
      connectorAt(3),
      connectorAt(4),
      connectorAt(5),
      Difference(
        outer,
        inner,
        connectorHoleAt(2),
        connectorHoleAt(3),
        connectorHoleAt(4),
        connectorHoleAt(5)
      )
    )
  }

  def connectorTop = {
    //the part marked with X should be replaced by a cable hook if we decide to replace the top beams with a cable
    val inner = {
      val i0 = Hull(
        connectorPositions(innerBase, 1), // X
        connectorPositions(innerBase, 2),
        connectorPositions(innerBase, 3),
        connectorPositions(innerBase, 6)
      )
      Hull(
        i0,
        i0.move(0, 0, 3*ct),
        i0.move(-ct*cos(topAngle), 0, ct*sin(topAngle))
      )
    }

    Union(
      connectorAt(1), // X
      connectorAt(2),
      connectorAt(3),
      connectorAt(6),
      Difference(
        Hull(
          connectorBaseAt(1), // X
          connectorBaseAt(2),
          connectorBaseAt(3),
          connectorBaseAt(6),
          cornerAt(0),
          cornerAt(1),
          cornerAt(5),
          cornerAt(6),
          cornerAt(7),
          cornerTripleAt(2),
          cornerTripleAt(3)
        ),
        connectorHoleAt( 1), // X
        connectorHoleAt( 2),
        connectorHoleAt( 3),
        connectorHoleAt( 6),
        inner
      )
    )
  }

  def connectorCenter = {
    val c1 = c0.rotateY( Pi/2)
    putAtCorners(c1, cl) + putAtCorners(connectorCorner, 0)
  }

  def connections = {
    Union(
      connectorCenter,
      connectorCenter.moveZ(topHeight),
      putAtCorners(connectorBottom,-tl),
      putAtCorners(connectorMiddle,-tl).moveZ(tl),
      putAtCorners(connectorTop,-topHBeamLength-2*cl).moveZ(topHeight)
    )
  }

  def full = skeleton + connections


}

object Frame {

  def apply(mainBeamLength: Double,
            clearance: Double,
            topAngle: Double,
            topLength: Double) = {
    val f = new Frame(mainBeamLength, clearance, topAngle, topLength)
    //f.connections
    //f.skeleton
      f.full
  }

}
