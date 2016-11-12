package scadla.examples.cnc

import math._
import scadla._
import utils._
import InlineOps._
import Common._

//TODO redo without bearing but with bushings
// bushing is brass tube of 10mm outer ∅ and 8mm inner ∅
// shaft is brass/steel of 8mm ∅ (sanded down for a tight fit in the tube)
// to hold the bushing, use epoxy

//a platform to put hold the spindle
object Platform {

  /*
  protected def bearings(space: Double) = {
    Union(
      bearing.moveZ(-tolerance/2),
      bearing.moveZ(7 + space + tolerance/2),
      Cylinder(9, space).moveZ(7),
      Cylinder(11 + tolerance,         11 + tolerance - space, space).moveZ(7),
      Cylinder(11 + tolerance - space, 11 + tolerance,         space).moveZ(7)
    )
  }

  protected def spindleMount(height: Double, gap: Double) = {
    val screw = Cylinder(Thread.ISO.M3+tolerance, Thread.ISO.M3+tolerance, height)
    Cylinder(15+gap, height).move(15,15,0) ++ Spindle.fixCoord.map{ case (x,y,_) => screw.move(x,y,0) }
  }

  protected def bearingsTest(height: Double, s: Double) = {
    val base = Cylinder(14, height)
    val hole = Cube(15,3,height-3).move(5,-1.5,1.5)
    base - hole - bearings(s).moveZ(height/2 - 7 - s/2)
  }

  //space should be ~ zBearingSpace + 2*tolerance
  def old(wall: Double = 4, bearingGap: Double = 10, height: Double = 10, space: Double = 1.6) = {
    val bNeg = bearings(space).moveZ(height/2 - 7 - space/2) 
    val bHolder = Cylinder(11 + wall, height)
    val radius = 43
    val offset = max(wall/2, bearingGap/2)
    def place(s: Solid) = {
      val paired = Union(
        s.move( 11 +offset, radius, 0),
        s.move(-11 -offset, radius, 0)
      )
      for (i <- 0 until 3) yield paired.rotateZ(2 * i * Pi / 3) //linter:ignore ZeroDivideBy
    }
    val base = Hull(place(bHolder): _*) -- place(bNeg)
    base - spindleMount(height, 0.8).move(-15, -30, 0)
  }
  */

  //bushing:
  // - brass tube of length: height + 2
  // - shaft of length: height + 2 + 2 * washer thickness + 2 * 0.8 * radius (for the thread)
  // - use smaller washer and enlarge them until it is a tight fit around the shaft
  // - M4/5 hole in the center of the shaft for attaching stuff on top
  // - sand (& polish) the shaft until it slides into the tube, add some grease
  // - on the outside, use threadlocker or solder to hold the nuts in place
  //
  // with my current setup
  // * horizontal bushing
  //   - brass tube: ∅ 10mm, length 12 mm
  //   - brass shaft: ∅ 8mm, length 22 mm ≅ 12 + 3.2 + 6.4
  // * vertical bushing (simpler)
  //   - brass tube: ∅ (8 - ε) mm
  //   - brass shaft: ∅ 8mm

  val bushingRadius = 5
  val mountScrews = Thread.ISO.M4

  def apply(radius: Double = 50, height: Double = 10,
            wall: Double = 4, bearingGap: Double = 10,
            spindleHoleRadius: Double = 25): Solid = {
    val offset = bushingRadius + max(wall/2, bearingGap/2)
    def place(s: Solid) = {
      val paired = Union(
        s.move(+offset, radius, 0),
        s.move(-offset, radius, 0)
      )
      for (i <- 0 until 3) yield paired.rotateZ(2 * i * Pi / 3) //linter:ignore ZeroDivideBy
    }
    val bNeg = Cylinder(bushingRadius, height)
    val bHolder = Cylinder(bushingRadius + wall, height)
    val base = Hull(place(bHolder): _*) -- place(bNeg)
    val spindle = Union(
        Cylinder(spindleHoleRadius + looseTolerance, height),
        Cylinder(mountScrews, height).move( spindleHoleRadius, spindleHoleRadius, 0),
        Cylinder(mountScrews, height).move( spindleHoleRadius,-spindleHoleRadius, 0),
        Cylinder(mountScrews, height).move(-spindleHoleRadius, spindleHoleRadius, 0),
        Cylinder(mountScrews, height).move(-spindleHoleRadius,-spindleHoleRadius, 0)
      ).rotateZ(Pi/4)
    base - spindle
  }

  def verticalBushing2Rod(wall: Double = 4,
                          length: Double = 50,
                          brassThickness: Double = 8,
                          slit: Double = 2) = {
    val bw = bushingRadius + wall
    val n = nut(Thread.ISO.M6).rotateY(Pi/2)
    val base = Cylinder(bw, brassThickness) + CenteredCube.y(length, 1.5 * bw, brassThickness)
    Difference(
      base,
      Cylinder(bushingRadius + tolerance, brassThickness),
      CenteredCube.y(bw + 10, slit, brassThickness),
      Hull(n.moveX(bw + 10 + wall), n.move(bw + 10 + wall, 0, brassThickness)),
      Cylinder(Thread.ISO.M6 + tolerance, length).rotateY(Pi/2).move(bw + 10 + wall, 0, brassThickness/2),
      Cylinder(Thread.ISO.M3 + tolerance, 2*bw).rotateX(Pi/2).move(bw + 3, bw, brassThickness/2),
      nut(Thread.ISO.M3).rotateX(Pi/2).move(bw + 3, 0.8*bw, brassThickness/2)
    )
  }

  def verticalBushing2Platform(wall: Double = 4,
                               brassThickness: Double = 8) = {
    val washerM6Inner = 3.2
    val washerM6Thickness = 1.6
    val b2w = brassThickness + 2*wall
    val bw = bushingRadius + wall
    val h =  bw + 4 + wall // 4 to allow an M4 screw head + washer XXX check that ...
    val base = Hull(
      CenteredCube.x(2*bw, b2w, 1),
      Cylinder(2*Thread.ISO.M6, b2w).rotateX(-Pi/2).moveZ(h)
    )
    val innerDelta = wall - 0.5
    val w = Tube(2*Thread.ISO.M6 + looseTolerance, washerM6Inner - tolerance, washerM6Thickness)
    Difference(
      base,
      Cylinder(mountScrews + tightTolerance, wall).moveY(b2w/2),
      CenteredCube.x(2*bw, b2w - 2*innerDelta, h + bw).move(0, innerDelta, wall),
      w.rotateX( Pi/2).move(0, innerDelta + 0.01, h),
      w.rotateX(-Pi/2).move(0, b2w - innerDelta - 0.01, h),
      Cylinder(mountScrews + tightTolerance, b2w).rotateX(-Pi/2).moveZ(h)
    )
  }

}
