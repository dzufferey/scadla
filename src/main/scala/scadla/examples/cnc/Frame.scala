package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import scadla.examples.extrusion._
import Common._

// TODO
// - the parts for the cable to improve the rigidity

// idea for a simpler frame:
// - the main part is just a box
// - only the top has the hex part
//      -----
//      |/ \|
//      |\ /|
//      -----

object Frame {

  val boltSize = Thread.ISO.M5

  val vBeamLength: Double = 600
  val hBeamLength: Double = 300
  val topAngle: Double = Pi/4
  val topLength: Double = 80
  val laOffset = 0 // downward offset the LA from the center of topLength
  val thickness = 4 // for the connector

  def t = T(20, 20, 3)(_)
  def l = L(20, 20, 2)(_)
  def b(length: Double) = Cube(20, 4, length)

  protected def putAtCorners(s: Solid, radius: Double) = {
    val s2 = for (i <- 0 until 2) yield s.moveX(radius).rotateZ(i * Pi/3) //linter:ignore ZeroDivideBy
    Union(s2:_*)
  }

  val screwOffsetY1 =  8.0
  val screwOffsetY2 = 12.0
  val screwOffsetX1 = 35.0
  val screwOffsetX2 = 95.0
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

  // top horizontal beam (flat)
  val tBeamOffset = 12.2
  val tBeamScrewOffset = 37
  val tBeamLength = {
    val l1 = effectiveFrameRadius
    val l2 = effectiveFrameRadius - cos(topAngle) * topLength
    effectiveFrameRadius * l2 / l1 - 2*tBeamOffset
  }
  def tBeam = {
    val x = b(tBeamLength)
    val s = Cylinder(boltSize+tolerance, 10).rotateX(-Pi/2).moveX(10)
    x - s.moveZ(tBeamScrewOffset-tBeamOffset) - s.moveZ(tBeamLength-tBeamScrewOffset+tBeamOffset)
  }

  def skeleton = {
    val z1 = vBeamLength
    val z2 = z1 + topLength * sin(topAngle)
    val l1 = effectiveFrameRadius
    val l2 = l1 - cos(topAngle) * topLength
    val hBeamZeroAtCorner = hBeam.moveX(- 2 - 8.5 * sin(Pi/6))
    val tBeamPositionned = tBeam.moveZ(tBeamOffset)
    Union(
      putAtCorners(vBeam, l1),
      putAtCorners(hBeamZeroAtCorner.rotateX(Pi/2).rotateZ(7*Pi/6).move(-3, 1.5, 0), l1),
      putAtCorners(hBeamZeroAtCorner.moveZ(-hBeamLength).rotateY(-Pi/2).rotateX(-Pi/2).rotateZ(4*Pi/6).move(-3, 1.5, z1), l1),
      putAtCorners(tBeamPositionned.rotateX(Pi/2).rotateZ(7*Pi/6).moveZ(z2), l2)
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
      val t2 = 8.0
      val x = Hull(
        RoundedCubeH(t, 56, thickness, t/2 - 0.1).moveY(5),
        RoundedCubeH(t, 17, thickness, t/2 - 0.1).move(0, cos(topAngle) * topLength + t/2, sin(topAngle) * topLength - thickness)
      ).moveX(-t/2)
      val y = Hull(
        RoundedCubeH(t2, t2, topLength/2.65, t2/2 - 0.1),
        RoundedCubeH(t2, t2, thickness, t2/2 - 0.1).moveX( 63),
        RoundedCubeH(t2, t2, thickness, t2/2 - 0.1).moveX(-63)
      ).move(-t2/2, 54, 0)
      val z = Hull(
        RoundedCubeH(40, t2, thickness, t2/2 - 0.1).move(-20, 5, 0),
        RoundedCubeH(t2, t2, thickness, t2/2 - 0.1).move(-t2/2, 5-20*sin(topAngle), -20)
      ).move(0, cos(topAngle) * topLength + t2, sin(topAngle) * topLength - thickness)
      x + y + z
    }
    val top = {
      val bt = boltSize + tolerance
      val d = 1.0
      val l = 45
      val o = 20 * sin(Pi/6)
      val c = Cube(l-o-10, 20, thickness)
      val cx = c + Cylinder(10, thickness).move(l-o-10, 10, 0)
      val cy = c.moveX(10) + Cylinder(10, thickness).move(10, 10, 0)
      val cbt = Cylinder(bt, thickness)
      val x = cx.moveX(o) - cbt.move(tBeamScrewOffset, 10, 0)
      val y = cy.moveX(-l) - cbt.move(-tBeamScrewOffset, 10, 0)
      val z = Bigger(PieSlice(20-d,0,Pi/3,thickness+3).rotateZ(-4*Pi/6), d)
      val res = Union(
        x.rotateZ( Pi/6),
        y.rotateZ(-Pi/6),
        z.move(0, 20 + 3.2 - d/2, d/2) //XXX 3.2, fix that
      )
      res.move(0, cos(topAngle) * topLength, sin(topAngle) * topLength - thickness)
    }
    bottom + middle + top
  }

  // part that goes between the feet and the frame to keep the 2π/3 angle
  protected def anglePlate = {
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

  // foot that contains/cover the nuts and bolts, plastic + silicone
  def foot(withMould: Boolean = false) = {
    val base = anglePlate
    val pegRadius = 1.5
    val pegSpacingX = 10.5
    val pegSpacingY = 10
    val siliconeThickness = 6.0 //washer is 1, screw head is 3
    val nozzleRadius = 0.4
    val locations = Union(
      RoundedCubeH(136, 26, siliconeThickness, 5).move(-68, 36, 0),
      RoundedCubeH(30, 50, siliconeThickness, 10).move(-15, 6, 0)
    )
    val peg = Cylinder(pegRadius, thickness + siliconeThickness)
    val pegs = Union( (-7 to 7).flatMap( i => (0 to 6).map( j => peg.move(i * pegSpacingX, j * pegSpacingY, 0) )) :_* )
    val withPegsHoles = base - (pegs * locations)
    if (withMould) {
      val bigger = Minkowski(
        locations.scaleZ((siliconeThickness - 1) / siliconeThickness),
        Cylinder(nozzleRadius, 1)
      )
      withPegsHoles + (bigger - locations.scaleZ(2)).moveZ(thickness)
    } else {
      withPegsHoles
    }
  }

  def actuatorConnector1 = {
    val bottomY = -15.5
    val bottomZ = thickness
    val topY = -cos(topAngle) * topLength - 12.5
    val topZ = sin(topAngle) * topLength -4 // 4 to account for the top profile
    val laY = -cos(topAngle) * (topLength - laOffset)/2
    val laZ = sin(topAngle) * (topLength - laOffset)/2
    val bearingThickness = 7
    val retainerThickness = LinearActuator.retainerThickness
    val totalThickness = bearingThickness + retainerThickness
    val c = Cube(totalThickness, 20, thickness)
    val threadFactor = 1.6
    val screwOffset = bottomY + screwOffsetY1 - 0.5 //XXX 0.5
    val body0 = Hull(
        c.move(0, bottomY, bottomZ),
        c.move(0, topY, topZ),
        Cylinder(11 + thickness, totalThickness).rotateY(Pi/2).move(0, laY, laZ)
      )
    val spaceForM3 = 3 + 2 * Thread.ISO.M3
    val body1 = Union(
        body0,
        // part to attach on top
        Cube(totalThickness, 20 + thickness + spaceForM3, thickness).move(0, topY - spaceForM3, topZ),
        Cube(totalThickness, 20 + thickness + spaceForM3, thickness).move(0, topY - spaceForM3, topZ + thickness + 4),
        Cube(totalThickness, thickness, 4 + thickness + 10).move(0, topY + 20, topZ - 10)
      )
    val body2 = Difference(
        body1,
        // space for the bearing
        Cylinder(11 + looseTolerance, totalThickness).rotateY(Pi/2).move(retainerThickness, laY, laZ),
        Cylinder(8 , totalThickness).rotateY(Pi/2).move(0, laY, laZ),
        // for the bottom screw
        CenteredCube.y(totalThickness, threadFactor * 2 * boltSize + looseTolerance, (threadFactor + 0.2) * boltSize).move(0, screwOffset, bottomZ + thickness),
        CenteredCube.y(totalThickness, 2 * (boltSize + tolerance), thickness).move(0, screwOffset, bottomZ),
        // for the top screw
        Cylinder(Thread.ISO.M3 + tightTolerance, 4 + 2 * thickness).move(totalThickness/2, topY - 1 - Thread.ISO.M3, topZ)
      )
    body2
  }
  
  def actuatorConnector2 = {
    actuatorConnector1.mirror(1,0,0)
  }

  def cableAttachPoint = {
    // length 65mm
    // 45⁰, 3mm
    val knob = Difference(
        Cube(6, 2, thickness).moveX(-3) + Cylinder(3, thickness).moveY(2),
        Cylinder(woodScrewRadius, thickness).moveY(2)
      ) - Cube(6, 2, thickness).move(-3, -2, 0)
    val wireStopper = Difference(
        Hull(
          Trapezoid(6, 10, thickness, 3).moveX(-5),
          Cylinder(3, thickness).rotateX(-Pi/2).moveZ(3)
        ),
        Cylinder(Thread.ISO.M3, thickness+2).rotateX(-Pi/2).moveZ(3),
        Cylinder(0.6, thickness+2).rotateX(-Pi/2).moveZ(3-Thread.ISO.M3)
      )
    val body = Union(
        Cube(65, 12, thickness),
        knob.move(3, 12, 0),
        knob.move(62, 12, 0),
        Cylinder(4, 6, 3).move(32.5 - 4, 6, thickness),
        wireStopper.move(43, 4, thickness)
      )
    val stopper1 = Union(
        knob,
        knob.moveZ(thickness/2),
        (Cube(6, 6, thickness/2).move(-3, -6, 0) + Cylinder(3, thickness/2).moveY(-6)).moveX(3).rotateZ(Pi/4).moveX(-3),
        PieSlice(3, 0, 3*Pi/4, 3*thickness/2).moveX(-3)
      ) - Cylinder(woodScrewRadius, 2 * thickness).moveY(2)
    val stopper2 = stopper1.mirror(1,0,0)
    Seq(body, stopper1, stopper2)
  }

  def cableTensioner = {
    val ri = 4.0
    val ro = 5.0
    val ch = 5.5 / 2
    val c0 = Cylinder(ro, ri, ch) + Cylinder(ri, ro, ch).moveZ(ch)
    val c = c0.rotateX(-Pi/2)
    val pusher = (c * CenteredCube.x(2*ro + 1, 2*ch, 2*ro + 1)) - Cylinder(Thread.ISO.M3 + looseTolerance, 3).moveY(ch)
    val bottom = 4*ro + 1
    val top = 2*ro + 2 + 2*(ro-ri)
    val y = 2*ch + 4
    val outline = Hull(Cube(bottom, y, 1), Trapezoid(top, bottom, y, ro).moveZ(4 + (ro-ri)))
    val sides = outline - Cube(bottom, 2*ch - 0.2, bottom).moveY(2.1)
    val screwPlate = Difference(
        CenteredCube.x(2*ro, y, 4),
        Cylinder(Thread.ISO.M3 + tightTolerance, 4).moveY(y/2),
        nut(Thread.ISO.M3).move(0, y/2, 2.4)
      )
    val body = Union(
        sides,
        Cube(2,y,2),
        Cube(2,y,2).moveX(bottom-2),
        c.move(0, 2, ro+4) * outline,
        c.move(bottom, 2, ro+4) * outline,
        screwPlate.moveX(bottom / 2)
      )
    Seq(body, pusher)
  }


  def assembled = {
    val x = 1.1
    val y = -3.0
    val l = effectiveFrameRadius
    val c1CornerAt0 = connector1.move(0, -8.5 * sin(Pi/6), 2)
    val c2CornerAt0 = connector2.move(0, -8.5 * sin(Pi/6), 2)
    val la = LinearActuator.assembled(false, true).rotateZ(Pi/2).rotateX(-Pi/4)
    val laX = -hBeamLength/2 - 3
    val laY = -cos(topAngle) * (topLength - laOffset)/2
    val laZ = sin(topAngle) * (topLength - laOffset)/2
    Union(
      // metal profiles
      skeleton,
      // plastic parts
      putAtCorners(c1CornerAt0.rotateZ(2*Pi/3).move(-3, 1.5, 0), l),
      putAtCorners(c2CornerAt0.rotateZ(  Pi/3).move(-3,-1.5, 0), l),
      putAtCorners(c1CornerAt0.rotateY(Pi).rotateZ(  Pi/3).move(-3,-1.5, vBeamLength), l),
      putAtCorners(c2CornerAt0.rotateY(Pi).rotateZ(2*Pi/3).move(-3, 1.5, vBeamLength), l),
      putAtCorners(connector3.rotateZ(Pi/2).moveZ(vBeamLength), l),
      putAtCorners(foot(true).rotateY(Pi).rotateZ(Pi/2), l),
      // motors and stuff
      putAtCorners(la.move(laX, laY, vBeamLength + laZ).rotateZ(-Pi/3), l),
      putAtCorners(actuatorConnector1.move(laX + 53, 0, vBeamLength).rotateZ(-Pi/3), l),
      putAtCorners(actuatorConnector2.move(laX - 53, 0, vBeamLength).rotateZ(-Pi/3), l)
      // to see where the holes should be
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

  def connector1WithSupport= {
    val c = connector1.rotateX(Pi/2)
    val base = Union(
      Cube(98.5, 2, 2).move(1.5, 0.25, 0),
      Cube(3.8, 99.74, 4).move(1.5, 0.25, 0)
    ).mirror(0, 1, 0)
    c + (base - c.moveZ(- 1.3 * supportGap))
  }

  def connector2WithSupport = {
    connector1WithSupport.mirror(1,0,0)
  }

  //TODO from the skeleton, get template to cut the stock and mark/drill the holes
  //in OpenSCAD:
  //  projection(cut = false) rotate([90,0,0]) {
  //      import("hBeam.stl");
  //  }


}
