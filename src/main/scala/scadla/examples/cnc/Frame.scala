package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import scadla.examples.extrusion._
import Common._


object Frame {

  val boltSize = Thread.ISO.M5

  val vBeamLength: Double = 400
  val hBeamLength: Double = 300
  val topAngle: Double = Pi/4
  val topLength: Double = 120 //TODO does it need to be that long
  val thickness = 3 // for the connector

  def t = T(20, 20, 3)(_)
  def l = L(20, 20, 2)(_)
  def b(length: Double) = Cube(20, 4, length)

  protected def putAtCorners(s: Solid, radius: Double) = {
    val s2 = for (i <- 0 until 2) yield s.moveX(radius).rotateZ(i * Pi/3) //linter:ignore ZeroDivideBy
    Union(s2:_*)
  }

  val screwOffsetY1 =  8
  val screwOffsetY2 = 12
  val screwOffsetX1 = 35
  val screwOffsetX2 = 95
  protected def screws = {
    val s = Cylinder(boltSize + tolerance, 30).moveZ(-10)
    Union(
      s.move(screwOffsetX1,screwOffsetY1,0),
      s.move(screwOffsetX2,screwOffsetY1,0),
      s.rotateY(Pi/2).move(0,screwOffsetY2,screwOffsetX1).rotateZ(-Pi/6),
      s.rotateY(Pi/2).move(0,screwOffsetY2,screwOffsetX2).rotateZ(-Pi/6)
    )
  }

  // vetical beam (T profile)
  def vBeam = {
    val x = t(vBeamLength).moveX(-10).rotateZ(Pi/2)
    Difference(
      x,
      screws.rotateZ(4*Pi/6),
      screws.rotateX(Pi).rotateZ(-4*Pi/6).moveZ(vBeamLength)
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
    val y = Cube(60, 4,40).move(-30, 0,-40).rotateY(-Pi/6).moveZ(offset)
    val z = Cube(60, 4,40).moveX(-30).rotateY(Pi/6).moveZ(length-offset)
    val s = Cylinder(boltSize+tolerance, 10).rotateX(-Pi/2).moveX(screwOffsetY1)
    x - y - z - s.moveZ(27+offset) - s.moveZ(length-27-offset) //XXX 27 is same as connector3
  }

  // horizontal beam (L profile)
  def hBeam = {
    val x = l(hBeamLength)
    val y = Cube(20,20,20+8.5).move(-20, 0,-20) + Cube(20, 20, 20).move(0,0,-20)
    val offsetX = 2 + 8.5 * sin(Pi/6) // 8.5 is T bottom part
    val y2 = y.rotateY(-Pi/6).moveX(offsetX)
    val p = Vector(3, 1.5, 0).rotateBy(Quaternion.mkRotation(Pi/6, Vector(0,0,1)))
    val s = screws.move(-p.y, offsetX - p.x, 0)
    Difference(
      x,
      Cylinder(1, 4).rotateX(-Pi/2).move(15, -1, hBeamLength/2), // middle mark
      y2,
      y2.mirror(0,0,1).moveZ(hBeamLength),
      s.rotateX(-Pi/2).rotateY(-Pi/2),
      s.rotateX( Pi/2).rotateY( Pi/2).moveZ(hBeamLength)
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
  def connector1 = {
    val profileRadiusT = 2
    val profileRadiusL = 4
    val c = {
      val x = Cube(100,18,100) // L are 2mm
      val y = PieSlice(profileRadiusL + 10, profileRadiusL, Pi/2, 100).rotateZ(-Pi/2).rotateY(Pi/2).move(0, profileRadiusL, profileRadiusL)
      (x - y)
    }
    val c2 = Cube(100,40,120)
    val offsetY = 8.5 * sin(Pi/6) // 8.5 is T bottom part
    val corner = {
      Union(
        c2.moveX(-100).rotateZ(-Pi/6).moveY(offsetY),
        c2.moveX(-100).rotateZ(5*Pi/6).moveY(offsetY),
        PieSlice(profileRadiusT + 10, profileRadiusT, Pi/2, 120).move(-profileRadiusT, -profileRadiusT, 0).rotateZ(5*Pi/6).moveY(offsetY)
      )
    }
    val base = Difference(
        c,
        corner,
        c2.move(10+thickness, 0, thickness),
        c2.move(thickness, 0, thickness).rotateZ(-Pi/6),
        c2.move(-50,0,thickness).rotateZ(-Pi/6).moveY(22.6)
      )
    val diagonal = {
      val blank = CenteredCube.xz(125, thickness, 125).rotateY(Pi/4)  
      val x = blank - CenteredCube.xz(100, thickness, 100).rotateY(Pi/4) 
      val p = PieSlice(50, 50 - thickness, Pi/4, thickness).rotateX(Pi/2)
      val y1 = p.rotateY(3*Pi/4).move(101,thickness,49)
      val y2 = p.rotateY(4*Pi/4).move(48, thickness, 104)
      val cy = Cylinder(5, 50)
      val h = Hull(cy, cy.moveX(10))
      val z1 = h.rotateZ(Pi/2).rotateX(Pi/4).move(68, 7, -15*sin(Pi/4) + thickness) * blank.scaleY(10)
      val z2 = h.rotate(Pi/2,0,2*Pi/6).move(0, thickness/sqrt(2), 55 + thickness) * blank.scaleY(10)
      (x + y1 + y2 + z1 + z2) * c - corner
    }
    val angle = {
      val x = 30
      CenteredCube(x,x,x).rotate(Pi/4,Pi/4,0).moveY(-5) * c - corner
    }
    val q = Quaternion.mkRotation(-2*Pi/3, Vector(0,0,1))
    val p = Vector(-3, 1.5, 0).rotateBy(q)
    base + diagonal + angle - screws.move(-p.x, 8.5 * sin(Pi/6) - p.y, -2)
  }
  
  // part to attach the T and L profiles (mirror of connector1)
  def connector2 = {
    connector1.mirror(1,0,0)
  }

  // part to attach the beams at the top to the L/T beams
  def connector3 = {
    val bottom = anglePlate
    val middle = {
      val t = 12.0
      val x = Hull(
        RoundedCubeH(t, 56, thickness-2, t/2 - 0.1).moveY(5),
        RoundedCubeH(t, 24, thickness, t/2 - 0.1).move(0, cos(topAngle) * topLength, sin(topAngle) * topLength - thickness)
      ).moveX(-t/2)
      val y = Hull(
        RoundedCubeH(t, t, topLength/2.5, t/2 - 0.1),
        RoundedCubeH(t, t, thickness-2, t/2 - 0.1).moveX( 50),
        RoundedCubeH(t, t, thickness-2, t/2 - 0.1).moveX(-50)
      ).move(-t/2, 51.5, 0)
      x + y
    }
    val top = {
      val screwOffsetX = 27
      val bt = boltSize + tolerance
      val x = Cube(50, 20, thickness) - Cylinder(bt, thickness).move(screwOffsetX, screwOffsetY1, 0)
      val y = Cube(50, 20, thickness).moveX(-50) - Cylinder(bt, thickness).move(-screwOffsetX, screwOffsetY1, 0)
      val z = Union(
        x.rotateZ( Pi/6),
        y.rotateZ(-Pi/6),
        Cylinder(10, thickness).moveY(10)
      )
      val delta = 5*tan(Pi/6)
      val a = z + CenteredCube.x(10, 20 + delta, thickness + 4).moveY(-1.5+delta)
      a.move(0, cos(topAngle) * topLength, sin(topAngle) * topLength - thickness)
    }
    bottom + middle + top
  }

  // foot that contains/cover the nuts and bolts, plastic only
  def simpleFoot = {
    val base = RoundedCubeH(80, 20, 5, 5)
    val c = Cylinder(2*boltSize + tolerance, 3) + Cylinder(boltSize + tolerance, 10)
    base.moveX(20) - c.move(screwOffsetX1, screwOffsetY1, 0) - c.move(screwOffsetX2, screwOffsetY1, 0)
  }

  // foot that contains/cover the nuts and bolts, plastic + silicone
  def foot(withMould: Boolean = false) = {
    val delta = 10
    val length = (screwOffsetX1 - screwOffsetX2).abs + 2 * delta
    val b0 = Minkowski(
      Cylinder(3, 5, 3.9),
      Cube(length - 10, 6, 0.1)
    ).move(5, 7, 0)
    val bt = boltSize + tolerance
    val bt2 = 2*boltSize + tolerance
    val c = Cylinder(bt2, 2).moveZ(3) + Cylinder(bt, 10)
    val screwPos = Seq(
        Vector(delta, screwOffsetY1, 0),
        Vector(length - delta, screwOffsetY1, 0)
      )
    val b1 = b0 -- screwPos.map(s => c.move(s))
    val t = Trapezoid(1, 1.5, 30, 1).move(-0.5, -5, 3)
    val b2 = b1 -- Range(1, 10).map(i => t.moveX(i * 10))
    if (withMould) {
      val t = 0.8
      val r = RoundedCubeH(length, 20, 6-t/2, 5)
      val outer = Bigger(r, t) - r.scaleZ(2).moveZ(0.2) -- screwPos.map(s => c.move(s).moveZ(-1))
      b2 + outer ++ screwPos.map(s => Tube(bt2+0.4, bt2, 4).moveZ(2).move(s))
    } else {
      b2
    }
  }

  // part that goes between the feet and the frame to keep the 2π/3 angle
  def anglePlate = {
    val q1 = Quaternion.mkRotation( Pi/6, Vector(0,0,1))
    val q2 = Quaternion.mkRotation(-Pi/6, Vector(0,0,1))
    val c1 = Cylinder(10, thickness)
    val c2 = Cylinder(boltSize + tolerance, thickness)
    val offsetY = 5.5
    val positions1 = Seq(
      Vector( screwOffsetX1, offsetY, 0).rotateBy(q1),
      Vector( screwOffsetX2, offsetY, 0).rotateBy(q1),
      Vector(-screwOffsetX1, offsetY, 0).rotateBy(q2),
      Vector(-screwOffsetX2, offsetY, 0).rotateBy(q2)
    )
    val positions2 = Seq(
      Vector( screwOffsetX1, screwOffsetY1, 0).rotateBy(q1),
      Vector( screwOffsetX2, screwOffsetY1, 0).rotateBy(q1),
      Vector(-screwOffsetX1, screwOffsetY1, 0).rotateBy(q2),
      Vector(-screwOffsetX2, screwOffsetY1, 0).rotateBy(q2)
    )
    val base = Hull(c1.scale(1.5, 0.5, 1).moveY(10) ++ positions1.map(p => c1.move(p)))
    base -- positions2.map(p => c2.move(p))
  }

  def assembled = {
    val x = 1.1
    val y = -3.0
    val l = effectiveFrameRadius
    val c1CornerAt0 = connector1.move(0, -8.5 * sin(Pi/6), 2)
    val c2CornerAt0 = connector2.move(0, -8.5 * sin(Pi/6), 2)
    Union(
      skeleton,
      putAtCorners(c1CornerAt0.rotateZ(2*Pi/3).move(-3, 1.5, 0), l),
      putAtCorners(c2CornerAt0.rotateZ(  Pi/3).move(-3,-1.5, 0), l),
      putAtCorners(c1CornerAt0.rotateY(Pi).rotateZ(  Pi/3).move(-3,-1.5, vBeamLength), l),
      putAtCorners(c2CornerAt0.rotateY(Pi).rotateZ(2*Pi/3).move(-3, 1.5, vBeamLength), l),
      putAtCorners(connector3.rotateZ(Pi/2).moveZ(vBeamLength), l),
      putAtCorners(anglePlate.rotateZ(Pi/2).moveZ(-thickness), l),
      putAtCorners(foot(true).rotateX(Pi).move(screwOffsetX1-10,16,-thickness).rotateZ(2*Pi/3), l),
      putAtCorners(foot(true).rotateX(Pi).move(-screwOffsetX2-10,16,-thickness).rotateZ(Pi/3), l)
      //XXX to see where the holes should be
      //putAtCorners(screws.rotateZ(2*Pi/3), l),
      //putAtCorners(screws.mirror(1,0,0).rotateZ(  Pi/3), l)
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
  //in OpenSCAD:
  //  projection(cut = false) rotate([90,0,0]) {
  //      import("hBeam.stl");
  //  }


}
