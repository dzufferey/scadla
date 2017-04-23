package scadla.examples.reach3D

import scadla._
import utils._
import Trig._
import InlineOps._
import squants.space.{Length, Angle}
import scala.language.postfixOps
import squants.space.LengthConversions._

/** Some legs for the Reach3D printer */
object ReachLegs {

  // two long legs attaching on the side of the 2040 extrusion
  // one short leg attaching on the end/bottom of the 2020 extrusion

  // base shape
  // t-nuts (M4)
  // springy thing

  val thickness = 4.0 mm
  val padWidth = 12.5 mm
  val width = 2 * padWidth
  val length = 100.0 mm
  
  val height = (40 mm) + thickness
  val padSpace = 6.0 mm
  val meetingPoint = 50.0 mm
  val damperThickness = 1.4 mm

  def base1 = {
    import scadla.EverythingIsIn.millimeters
    val b = Hull(
      Cube(width, thickness, height),
      Cube(width, length, thickness),
      Cylinder(thickness/2, width).scale(1,0.5,1).rotateY(Pi/2).move(0, length, thickness/2)
    )
    // ideal dimensions:
    //  Trapezoid(6, 9, width, 2)
    //  Trapezoid(6.75, 9, width, 1.5)
    //  Trapezoid(7.5, 9, width, 1)
    val slot = Trapezoid(6.75, 9, width, 1.5).moveX(-4.5).rotateY(-Pi/2).rotateZ(Pi/2).moveX(width)
    val added = Union(
      b - b.move(thickness, thickness, thickness),
      slot.moveZ(thickness + 10),
      slot.moveZ(thickness + 30),
      PieSlice(thickness, 0, Pi/2, width).rotateY(Pi/2).rotateX(-Pi/2).moveZ(thickness)
    )
    val screw = Cylinder(Thread.ISO.M4 + 0.1, thickness + 3).rotateX(Pi/2).move(width/2, thickness + 1, 0)
    Difference(
      added,
      screw.moveZ(thickness + 10),
      screw.moveZ(thickness + 30)
    )
  }

  def vibrationDamper(baseWidth: Length) = {
    import scadla.EverythingIsIn.millimeters
    val offset = (baseWidth - width) / 2
    val c = Cylinder(damperThickness/2, width).rotateY(Pi/2)
    val pad = Union(
      Cube(width, padWidth + 2 * damperThickness, damperThickness),
      c.moveY(damperThickness/2),
      c.moveY(damperThickness*3/2 + padWidth)
    )
    val leg = Hull(
      c.move(offset, 0, damperThickness/2),
      Cylinder(damperThickness/2, baseWidth).rotateY(Pi/2).move(0, -meetingPoint, damperThickness*3/2 + padSpace)
    )
    (pad.moveX(offset) + leg).moveZ(-damperThickness)
  }

  def leg1 = {
    base1 + vibrationDamper(width).move(0 mm, length - padWidth - 2*damperThickness, - padSpace)
  }

  val beamLength = meetingPoint + padWidth + 2*damperThickness + (20 mm)

  def endcap2040 = {
    import scadla.EverythingIsIn.millimeters
    //ideal slot: Cube(6,10,2).moveX(-3) + Cube(11, 10, 1.5).move(-5.5,0,2) + Trapezoid(6, 11, 10,2.5).move(-5.5,0,3.5)
    val slot = Cube(5.8,10,2.1).moveX(-2.9) + Cube(10.8, 10, 1.3).move(-5.4,0,2.1) + Trapezoid(5.8, 10.8, 10, 2.3).move(-5.4,0,3.4)
    Cube(20, thickness, 40) +
    slot.move(10, 0,  0) +
    slot.move(10, 0, 20) +
    slot.rotateY( Pi/2).move( 0, 0, 10) +
    slot.rotateY(-Pi/2).move(20, 0, 10) +
    slot.rotateY( Pi/2).move( 0, 0, 30) +
    slot.rotateY(-Pi/2).move(20, 0, 30) +
    slot.rotateY( Pi).move(10, 0, 20) +
    slot.rotateY( Pi).move(10, 0, 40)
  }

  def base2 = {
    import scadla.EverythingIsIn.millimeters
    // beam that continues below the extrusion
    val beam = Cube(20, beamLength, thickness) +
               PieSlice(thickness, 0, Pi/2, 20).rotateY(Pi/2).move(0, beamLength, thickness) +
               PieSlice(thickness, 0, Pi/2, 20).rotateY(Pi/2).rotateX(-Pi/2).moveZ(thickness) +
               Trapezoid(6.75, 9, beamLength, 1.5).move(-4.5 + 10, 0, thickness) -
               Cylinder(Thread.ISO.M4 + 0.1, thickness + 3).move(10, 10, 0)
    beam + endcap2040.rotateZ(Pi).move(20, beamLength + thickness, thickness)
  }

  def leg2 = {
    base2 + vibrationDamper(20 mm).move(0 mm, beamLength - padWidth - 2*damperThickness, - padSpace)
  }

  def main(args: Array[String]) {
    import scadla.EverythingIsIn.{millimeters, radians}
    Seq(
      leg1.rotateY(-Pi/2) -> "leg1a.stl",
      leg1.mirror(0,0,1).rotateY(-Pi/2) -> "leg1b.stl",
      leg2.rotateY(-Pi/2) -> "leg2.stl"
    ).par.foreach{ case (obj, name) =>
      backends.Renderer.default.toSTL(obj, name)
    }
  }

}
