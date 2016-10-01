package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import scadla.examples.extrusion._


object Frame2 {

  val boltSize = Thread.ISO.M5

  val vBeamLength: Double = 400
  val hBeamLength: Double = 300
  val topAngle: Double = Pi/4
  val topLength: Double = 120 //TODO does it need to be that long

  def t = T(20, 20, 3)(_)
  def l = L(20, 20, 2)(_)
  def b(length: Double) = Cube(20, 2, length)

  protected def putAtCorners(s: Solid, radius: Double) = {
    val s2 = for (i <- 0 until 6) yield s.moveX(radius).rotateZ(i * Pi/3) //linter:ignore ZeroDivideBy
    Union(s2:_*)
  }

  val screwOffsetY = 12
  protected def screws = {
    val s = Cylinder(boltSize + Common.tolerance, 30)
    Union(
      s.move(30,screwOffsetY,-10),
      s.move(90,screwOffsetY,-10),
      s.rotateY(Pi/2).move(-10,screwOffsetY+2,30).rotateZ(-Pi/6),
      s.rotateY(Pi/2).move(-10,screwOffsetY+2,90).rotateZ(-Pi/6)
    )
  }

  def skeleton = {
    val z1 = vBeamLength
    val z2 = vBeamLength + topLength * sin(topAngle)
    val l1 = hBeamLength
    val l2 = hBeamLength - cos(topAngle) * topLength
    val offset = 5
    val v = { // vertical beam
      val x = t(vBeamLength).moveX(-10).rotateZ(Pi/2)
      Difference(
        x,
        screws.moveY(-1.85).rotateZ(Pi/2 + Pi/6),
        screws.moveY(-1.85).rotateX(Pi).rotateZ(-Pi/2 -Pi/6).moveZ(vBeamLength)
      )
    }
    val s = { // top horizontal beam
      val length = hBeamLength * l2 / l1
      val x = b(length)
      val y = Cube(60, 3,40).move(-30, 0,-40).rotateY(-Pi/6).moveZ(offset)
      val z = Cube(60, 3,40).moveX(-30).rotateY(Pi/6).moveZ(length-offset)
      x - y - z
    }
    val h = {
      val x = l(hBeamLength) // horizontal L beam
      val y = Cube(20,20,40).move(-20, 0,-20) + Cube(20, 20, 20).move(0,0,-20)
      val y2 = y.moveX(offset+0.5)
      Difference(
        x,
        y2.rotateY(-Pi/6),
        y2.mirror(1,0,0).rotateY(7*Pi/6).moveZ(hBeamLength),
        screws.rotateX(-Pi/2).rotateY(-Pi/2).moveZ(2),
        screws.rotateX( Pi/2).rotateY( Pi/2).moveZ(hBeamLength-2)
      )
    }
    Union(
      putAtCorners(v, l1),
      putAtCorners(h.rotateX(Pi/2).moveX(-3).rotateZ(7*Pi/6), l1),
      putAtCorners(h.rotateY(-Pi/2).rotateX(-Pi/2).move(hBeamLength,-3,0).rotateZ(4*Pi/6).moveZ(z1), l1),
      putAtCorners(s.rotateX(Pi/2).rotateZ(7*Pi/6).moveZ(z2), l2)
    )
  }

  // part to attach the T and L profiles
  def connector1 = {
    val thickness = 5
    val offsetY = 10 * sin(Pi/6)
    val c = Cube(100, 18,100).move(0, 2, 2) // L are 2mm
    val c2 = Cube(100, 40,120)
    val corner = Union(
        Bigger(c2, 1.5).moveX(-100).rotateZ(-Pi/6), // T are 3mm
        c2.moveX(-100).rotateZ(5*Pi/6)
      ).moveY(offsetY)
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

  // foot that contains/cover the nuts and bolts
  def foot = {
    val base = RoundedCubeH(80, 20, 5, 5)
    val c = Cylinder(2*boltSize, 3)
    base.moveX(20) - screws - c.move(30, screwOffsetY, 0) - c.move(90, screwOffsetY, 0)
  }

  def assembled = {
    Union(
      skeleton,
      putAtCorners(connector1.move( 2,-3,0).rotateZ(2*Pi/3), hBeamLength),
      putAtCorners(connector2.move(-2,-3,0).rotateZ(  Pi/3), hBeamLength),
      putAtCorners(connector1.rotateY(Pi).move(-2,-3,vBeamLength).rotateZ(  Pi/3), hBeamLength),
      putAtCorners(connector2.rotateY(Pi).move( 2,-3,vBeamLength).rotateZ(2*Pi/3), hBeamLength)
    )
  }

  //TODO part to connect the skeleton
  //TODO from the skeleton, get template to cut the stock and mark/drill the holes

}
