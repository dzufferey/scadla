package scadla.examples.cnc

import scadla._
import utils._
import Trig._
import InlineOps._
import Common._
import thread.ISO
import squants.space.Length
import scadla.EverythingIsIn.{millimeters, radians}  

//a platform to put hold the spindle
object Platform {

  protected def bearings(space: Length) = {
    Union(
      bearing.moveZ(-tolerance/2),
      bearing.moveZ(7 + space + tolerance/2),
      Cylinder(9, space).moveZ(7),
      Cylinder(11 + tolerance,         11 + tolerance - space, space).moveZ(7),
      Cylinder(11 + tolerance - space, 11 + tolerance,         space).moveZ(7)
    )
  }

  protected def oldSpindleMount(height: Length, gap: Length) = {
    val screw = Cylinder(ISO.M3+tolerance, ISO.M3+tolerance, height)
    Cylinder(15+gap, height).move(15,15,0) ++ Spindle.fixCoord.map{ case (x,y,_) => screw.move(x,y,0) }
  }

  protected def spindleMount(spindleHoleRadius: Length, height: Length) = {
    Union(
      Cylinder(spindleHoleRadius + looseTolerance, height),
      Cylinder(mountScrews, height).move( spindleHoleRadius, spindleHoleRadius, 0),
      Cylinder(mountScrews, height).move( spindleHoleRadius,-spindleHoleRadius, 0),
      Cylinder(mountScrews, height).move(-spindleHoleRadius, spindleHoleRadius, 0),
      Cylinder(mountScrews, height).move(-spindleHoleRadius,-spindleHoleRadius, 0)
    )
  }

  //For each hinge, I need:
  // - 2x 608 bearing
  // - 2x M8 nut
  // - 1x M8 washer
  // - 40mm M8 threaded rod

  //For the assembly:
  // - M8 rod
  //   * file away 10mm of the thread on the M8 rod
  //   * cut a 2mm vertical slot in the M8 rod
  //   * drill a 3mm hole perpendicular to the slot
  // - M6 rod
  //   * file the side until a 2mm tab is left
  //   * drill a 3mm hole in the tab
  // - M4x10 screw
  //   * file the thread away to get a M3 rod
  //   * make a M3 thread on the last few mm

  //space should be ~ zBearingSpace + 2*tolerance
  def with608Bearings(radius: Length = 50,
                      wall: Length = 5,
                      bearingGap: Length = 10,
                      height: Length = 10,
                      space: Length = 1.6,
                      spindleHoleRadius: Length = 25) = {
    val bNeg = bearings(space).moveZ(height/2 - 7 - space/2) 
    val bHolder = Cylinder(11 + wall, height)
    val offset = (wall/2) max (bearingGap/2)
    def place(s: Solid) = {
      val paired = Union(
        s.move( 11 +offset, radius, 0),
        s.move(-11 -offset, radius, 0)
      )
      for (i <- 0 until 3) yield paired.rotateZ(2 * i * Pi / 3) //linter:ignore ZeroDivideBy
    }
    val base = Hull(place(bHolder): _*) -- place(bNeg)
    base - spindleMount(spindleHoleRadius, height).rotateZ(Pi/4)
  }

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
  val mountScrews = ISO.M4

  def withBushing(radius: Length = 50, height: Length = 10,
                  wall: Length = 4, bearingGap: Length = 10,
                  spindleHoleRadius: Length = 25): Solid = {
    val offset = bushingRadius + (wall/2) max (bearingGap/2)
    def place(s: Solid) = {
      val paired = Union(
        s.move( offset, radius, 0),
        s.move(-offset, radius, 0)
      )
      for (i <- 0 until 3) yield paired.rotateZ(2 * i * Pi / 3) //linter:ignore ZeroDivideBy
    }
    val bNeg = Cylinder(bushingRadius, height)
    val bHolder = Cylinder(bushingRadius + wall, height)
    val base = Hull(place(bHolder): _*) -- place(bNeg)
    val spindle = spindleMount(spindleHoleRadius, height).rotateZ(Pi/4)
    base - spindle
  }

  def verticalBushing2Rod(wall: Length = 4,
                          length: Length = 50,
                          brassThickness: Length = 8,
                          slit: Length = 2) = {
    val bw = bushingRadius + wall
    val n = nut(ISO.M6).rotateY(Pi/2)
    val base = Cylinder(bw, brassThickness) + CenteredCube.y(length, 1.5 * bw, brassThickness)
    Difference(
      base,
      Cylinder(bushingRadius + tolerance, brassThickness),
      CenteredCube.y(bw + 10, slit, brassThickness),
      Hull(n.moveX(bw + 10 + wall), n.move(bw + 10 + wall, 0, brassThickness)),
      Cylinder(ISO.M6 + tolerance, length).rotateY(Pi/2).move(bw + 10 + wall, 0, brassThickness/2),
      Cylinder(ISO.M3 + tolerance, 2*bw).rotateX(Pi/2).move(bw + 3, bw, brassThickness/2),
      nut(ISO.M3).rotateX(Pi/2).move(bw + 3, 0.8*bw, brassThickness/2)
    )
  }

  def verticalBushing2Platform(wall: Length = 4,
                               brassThickness: Length = 8) = {
    val washerM6Inner = 3.2
    val washerM6Thickness = 1.6
    val b2w = brassThickness + wall*2
    val bw = bushingRadius + wall
    val h =  bw + 4 + wall // 4 to allow an M4 screw head + washer XXX check that ...
    val base = Hull(
      CenteredCube.x(bw*2, b2w, 1),
      Cylinder(ISO.M6 * 2, b2w).rotateX(-Pi/2).moveZ(h)
    )
    val innerDelta = wall - 0.5
    val w = Tube(ISO.M6 * 2+ looseTolerance, washerM6Inner - tolerance, washerM6Thickness)
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
