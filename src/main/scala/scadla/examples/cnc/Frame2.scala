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

  protected def screws = {
    val offsetY = 12
    val s = Cylinder(boltSize + Common.tolerance, 30)
    Union(
      s.move(30,offsetY,-10),
      s.move(90,offsetY,-10),
      s.rotateY(Pi/2).move(-10,offsetY+2,30).rotateZ(-Pi/6),
      s.rotateY(Pi/2).move(-10,offsetY+2,90).rotateZ(-Pi/6)
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
      x - screws.rotateZ(Pi/2 + Pi/6)
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
      x - y2.rotateY(-Pi/6) - y2.mirror(1,0,0).rotateY(7*Pi/6).moveZ(hBeamLength)
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
    val c = Cube(100, 18,100) // L are 2mm
    val c2 = Bigger(Cube(100, 40,100), 1.5) // T are 3mm
    val corner = Union(
        c2.moveX(-100).rotateZ(-Pi/6),
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
      val y1 = PieSlice(50, 50 - thickness, Pi/4, thickness).rotateX(Pi/2).rotateY(3*Pi/4).move(102,thickness,50)
      val y2 = PieSlice(50, 50 - thickness, Pi/4, thickness).rotateX(Pi/2).rotateY(4*Pi/4).move(48, thickness, 104)
      (x + y1 + y2) * c - corner
    }
    val angle = {
      val x = sqrt(2) * 25
      CenteredCube(x,x,x).rotate(Pi/4,Pi/4,0) * c - corner
    }
    base + diagonal + angle - screws.move(0, 0, 2)
    // TODO fix to take into account the extrusion thickness!!
  }
  
  // part to attach the T and L profiles (mirror of connector1)
  def connector2 = {
    connector1.mirror(1,0,0)
  }

  // foot that contains/cover the nuts and bolts
  def foot = {
    //TODO profile to poor oogoo
    val base = RoundedCubeH(80, 20, 5, 5)
    val c = Cylinder(2*boltSize, 3)
    base.moveX(20) - screws - c.move(30, 12, 0) - c.move(90, 12, 0)
  }

  //TODO part to connect the skeleton
  //TODO from the skeleton, get template to cut the stock and mark/drill the holes

}
