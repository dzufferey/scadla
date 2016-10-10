package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import scadla.examples.extrusion._


object Frame {

  val boltSize = Thread.ISO.M5

  val vBeamLength: Double = 400
  val hBeamLength: Double = 300
  val topAngle: Double = Pi/4
  val topLength: Double = 120 //TODO does it need to be that long
  val thickness = 5 // for the connector

  def t = T(20, 20, 3)(_)
  def l = L(20, 20, 2)(_)
  def b(length: Double) = Cube(20, 4, length)

  protected def putAtCorners(s: Solid, radius: Double) = {
    val s2 = for (i <- 0 until 2) yield s.moveX(radius).rotateZ(i * Pi/3) //linter:ignore ZeroDivideBy
    Union(s2:_*)
  }

  val screwOffsetY1 = 12
  val screwOffsetY2 = 14
  protected def screws = {
    val s = Cylinder(boltSize + Common.tolerance, 30)
    Union(
      s.move(30,screwOffsetY1,-10),
      s.move(90,screwOffsetY1,-10),
      s.rotateY(Pi/2).move(-10,screwOffsetY2,30).rotateZ(-Pi/6),
      s.rotateY(Pi/2).move(-10,screwOffsetY2,90).rotateZ(-Pi/6)
    )
  }

  // vetical beam (T profile)
  def vBeam = {
    val x = t(vBeamLength).moveX(-10).rotateZ(Pi/2)
    Difference(
      x,
      screws.moveY(-2.2).rotateZ(4*Pi/6), //XXX 2.2 ?
      screws.moveY(-2.2).rotateX(Pi).rotateZ(-4*Pi/6).moveZ(vBeamLength)
    )
  }

  // top horizontal beam (flat)
  //TODO could we modify the top conector and the offset to make the cut 90°
  def tBeam = {
    val offset = 5
    val l1 = hBeamLength
    val l2 = hBeamLength - cos(topAngle) * topLength
    val length = hBeamLength * l2 / l1
    val x = b(length)
    val y = Cube(60, 4,40).move(-30, 0,-40).rotateY(-Pi/6).moveZ(offset-1) //XXX 1 ?
    val z = Cube(60, 4,40).moveX(-30).rotateY(Pi/6).moveZ(length-offset+1) //XXX 1 ?
    val s = Cylinder(boltSize+Common.tolerance, 10).rotateX(-Pi/2).moveX(screwOffsetY1)
    x - y - z - s.moveZ(27+offset-1) - s.moveZ(length-27-offset+1) //XXX 1 ? (27 is same as connector3)
  }

  // horizontal beam (L profile)
  def hBeam = {
    val x = l(hBeamLength)
    val y = Cube(20,20,20+8.5).move(-20, 0,-20) + Cube(20, 20, 20).move(0,0,-20)
    val y2 = y.rotateY(-Pi/6).moveX(2 + 8.5 * sin(Pi/6)) // 8.5 is T bottom part
    Difference(
      x,
      y2,
      y2.mirror(0,0,1).moveZ(hBeamLength),
      screws.rotateX(-Pi/2).rotateY(-Pi/2).moveZ(1), //XXX 1 ?
      screws.rotateX( Pi/2).rotateY( Pi/2).moveZ(hBeamLength-1) //XXX 1 ?
    )
  }

  val effectiveFrameRadius = hBeamLength + 3 + 1.5 * tan(Pi/6) + 1.5 / cos(Pi/6) // additional factor because the hBeams are offset

  def skeleton = {
    val z1 = vBeamLength
    val z2 = z1 + topLength * sin(topAngle)
    val l1 = effectiveFrameRadius
    val l2 = l1 - cos(topAngle) * topLength
    val hBeamZeroAtCorner = hBeam.moveX(- 2 - 8.5 * sin(Pi/6))
    Union(
      putAtCorners(vBeam, l1),
      putAtCorners(hBeamZeroAtCorner.rotateX(Pi/2).rotateZ(7*Pi/6).move(-3, 1.5, 0), l1),
      putAtCorners(hBeamZeroAtCorner.moveZ(-hBeamLength).rotateY(-Pi/2).rotateX(-Pi/2).rotateZ(4*Pi/6).move(-3, 1.5, z1), l1),
      putAtCorners(tBeam.moveX(-3).rotateX(Pi/2).rotateZ(7*Pi/6).moveZ(z2), l2)
    )
  }
    
  // part to attach the T and L profiles
  // XXX this has a thickness of 3 instead of 5 !!!
  // XXX does not touch the back of the L profile (only the bottom)
  def connector1 = {
    val offsetY = 10 * sin(Pi/6)
    val profileRadiusT = 2
    val profileRadiusL = 4
    val c = {
      val x = Cube(100, 18,100)
      val y = PieSlice(profileRadiusL + 10, profileRadiusL, Pi/2, 100).rotateZ(-Pi/2).rotateY(Pi/2).move(0, profileRadiusL, profileRadiusL)
      (x - y).move(0, 2*cos(Pi/3), 2) // L are 2mm, XXX close but not quite right
    }
    val c2 = Cube(100,40,120)
    val corner = {
      val b1 = 3.0 // T are 3mm
      val b2 = 1.5 // T has 1mm more than L, XXX 1.5 ???
      Union(
        Bigger(c2,b1).moveX(-100).rotateZ(-Pi/6),
        Bigger(c2,b2).moveX(-100).rotateZ(5*Pi/6),
        PieSlice(profileRadiusT + 10, profileRadiusT, Pi/2, 120).move(-b1/2-profileRadiusT, -b2/2-profileRadiusT, 0).rotateZ(5*Pi/6)
      ).moveY(offsetY)
    }
    val base = Difference(
        c,
        corner,
        c2.move(10+thickness, 0, thickness),
        c2.move(thickness, 0, thickness).rotateZ(-Pi/6).moveY(offsetY),
        c2.move(-50,0,thickness).rotateZ(-Pi/6).moveY(20+offsetY)
      )
    val diagonal = {
      val x = CenteredCube.xz(125, thickness, 125).rotateY(Pi/4) - CenteredCube.xz(100, thickness, 100).rotateY(Pi/4) 
      val y1 = PieSlice(50, 50 - thickness, Pi/4, thickness).rotateX(Pi/2).rotateY(3*Pi/4).move(101,thickness,49)
      val y2 = PieSlice(50, 50 - thickness, Pi/4, thickness).rotateX(Pi/2).rotateY(4*Pi/4).move(48, thickness, 104)
      val z1 = CenteredCube(30, 30, 30).rotate(Pi/4,Pi/4,0).move(68, thickness-1,-10)
      val z2 = CenteredCube(30, 30, 30).rotate(Pi/4,Pi/4,0).move(-7, thickness-1, 65)
      (x + y1 + y2 + z1 + z2) * c - corner
    }
    val angle = {
      val x = sqrt(2) * 25
      CenteredCube(x,x,x).rotate(Pi/4,Pi/4,0) * c - corner
    }
    base + diagonal + angle - screws
  }
  
  // part to attach the T and L profiles (mirror of connector1)
  def connector2 = {
    connector1.mirror(1,0,0)
  }

  // part to attach the beams at the top to the L/T beams
  def connector3 = {
    //TODO make the bottom part take the 2 screws
    val bottom = {
      val screwOffsetX = 27
      val x = Cube(50, 20, thickness) - Cylinder(boltSize+Common.tolerance, thickness).move(screwOffsetX, screwOffsetY1, 0)
      val y = Cube(50, 20, thickness).moveX(-50) - Cylinder(boltSize+Common.tolerance, thickness).move(-screwOffsetX, screwOffsetY1, 0)
      Union(
        x.moveY(-10).rotateZ( Pi/6),
        y.moveY(-10).rotateZ(-Pi/6),
        Cylinder(10, thickness)
      )
    }
    val middle = {
      val t = 12.0
      Hull(
        RoundedCubeH(t, 24, thickness, t/2 - 0.1),
        RoundedCubeH(t, 24, thickness, t/2 - 0.1).move(0, cos(topAngle) * topLength, sin(topAngle) * topLength - thickness)
      ).move(-t/2, -10, 0)
    }
    val top = {
      val delta = 5*tan(Pi/6)
      val x = bottom + CenteredCube.x(10, 20 + delta, thickness + 4).moveY(-11.5+delta)
      x.move(0, cos(topAngle) * topLength, sin(topAngle) * topLength - thickness)
    }
    (bottom + middle + top).moveY(10)
  }

  // foot that contains/cover the nuts and bolts
  def foot = {
    val base = RoundedCubeH(80, 20, 5, 5)
    val c = Cylinder(2*boltSize, 3)
    base.moveX(20) - screws - c.move(30, screwOffsetY1, 0) - c.move(90, screwOffsetY1, 0)
  }

  //TODO make a part that goes between the feet and the frame to keep the 2π/3 angle

  def assembled = {
    val x = 1.1
    val y = -3.0
    val l = effectiveFrameRadius
    Union(
      skeleton,
      putAtCorners(connector1.move( x,y,0).rotateZ(2*Pi/3), l),
      putAtCorners(connector2.move(-x,y,0).rotateZ(  Pi/3), l),
      putAtCorners(connector1.rotateY(Pi).move(-x,y,vBeamLength).rotateZ(  Pi/3), l),
      putAtCorners(connector2.rotateY(Pi).move( x,y,vBeamLength).rotateZ(2*Pi/3), l),
      putAtCorners(connector3.move( 0, -2, vBeamLength).rotateZ(Pi/2), l)
      //TODO add feets
    )
  }

  def hBeamJig1 = {
    Difference(
      Cube(30, 30, 20).move(-5, -5, 0),
      Bigger(l(20), 0.6),
      PieSlice(5, 2.7, Pi/2, 20).rotateZ(Pi).move(5,5,0),
      hBeam.scaleY(100).move(0, -5, 5),
      Cube(5,30,15).move(-5, -5, 11.2),
      Cube(5,30,15).move(20, -5, 12.92)
    )
  }

  def hBeamJig2 = {
    Difference(
      Cube(30, 30, 20).move(-5, -5, 0),
      hBeamJig1,
      Bigger(l(40), 0.6),
      PieSlice(5, 2.7, Pi/2, 40).rotateZ(Pi).move(5,5,0)
    ).rotateY(Pi)
  }

  //TODO from the skeleton, get template to cut the stock and mark/drill the holes

}
