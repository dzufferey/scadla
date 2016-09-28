package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import scadla.examples.extrusion._


object Frame2 {

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

  def skeleton = {
    val z1 = vBeamLength
    val z2 = vBeamLength + topLength * sin(topAngle)
    val l1 = hBeamLength
    val l2 = hBeamLength - cos(topAngle) * topLength
    val offset = 5
    val v = t(vBeamLength).moveX(-10).rotateZ(Pi/2) // vertical beam
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

  //TODO part to connect the skeleton

}
