package scadla.examples

import scadla._
import utils._
import Trig._
import InlineOps._
import scadla.EverythingIsIn.{millimeters, radians}  
import squants.space.{Length, Angle, Degrees, Millimeters}
import scala.language.postfixOps // for mm notation
import squants.space.LengthConversions._ // for mm notation

/** A class for the small rollers in the mecanum wheel */
class Roller(height: Length, maxOuterRadius: Length, minOuterRadius: Length, innerRadius: Length) {
  import backends.renderers.OpenScad._

  val axis = 0.5 mm
  val h = height - 2*axis

  protected def carveAxle(s: Solid) = s - Cylinder(innerRadius, height)

  def outline = {
    // r * f = maxOuterRadius
    // r * f * cos(a) = minOuterRadius
    // r * sin(a) = height/2
    val a = acos(minOuterRadius/maxOuterRadius)
    val r = h / 2 / sin(a)
    val f = maxOuterRadius / r
    val s = Sphere(r).scale(f, f, 1).moveZ(height/2)
    val c1 = Cylinder(maxOuterRadius, h).moveZ(axis)
    val c2 = Cylinder(minOuterRadius, height)
    (s * c1) + c2
  }

  def solid = carveAxle(outline)

  //to make only the "skeleton" of a roller,
  //then it can be coated with oogoo to get better friction
  def skeleton = {
    val base =
      carveAxle(
        Cylinder(minOuterRadius, height) +
        solid.scale(0.8, 0.8, 1)
      )
    val angle = 22.5° // π / 8
    val grooveDepth = (maxOuterRadius - minOuterRadius) max 2
    val inner = (maxOuterRadius - grooveDepth) max ((minOuterRadius + innerRadius) / 2)
    val slice = PieSlice(maxOuterRadius, inner, angle, h).toSolid.moveZ(axis)
    (0 until 8).foldLeft(base)( (acc, i) => acc - slice.rotateZ(i*2*angle) )
  }

  //mold for k*l roller
  def mold(k: Int, l: Int) = {
    val wall = 2 mm
    val distToWall = wall + maxOuterRadius
    val step = maxOuterRadius + distToWall
    val flatRoller = Rotate(-90°, 0, 0, outline)
    val row = {
      val rs = for (i <- 0 until k) yield Translate( distToWall + i*step, 1, distToWall, flatRoller)
      Union(rs:_*)
    }
    val rows = {
      val rs = for (j <- 0 until l) yield row.moveY(j*(2+height))
      Union(rs:_*)
    }
    val grooves = {
      val w = wall * 0.4
      val groove = CenteredCube.xz(w,l*(2+height),w).rotateY(Pi/4).moveZ(distToWall)
      val gs = for (i <- 0 until (k-1)) yield groove.moveX( distToWall + (i+0.5)*step)
      Union(gs:_*)
    }
    val base = Cube((k * step : Length) + wall, l*(2+height), distToWall)
    base - grooves - rows
  }

}


class MecanumWheel(radius: Length, width: Length, angle: Angle, nbrRollers: Int) {

  //TODO ideally the projection of the arc created by the roller should match the shape of the wheel

  //some more parameters
  val tolerance = 0.15 mm

  var centerAxleRadius = (2.5 mm) + tolerance
  var shaftFlat = 0.45 mm

  var rollerAxleRadius1 = (1.75 mm) / 2 + tolerance
  var rollerAxleRadius2 = (1.0 mm) + tolerance

  var rollerGap = 0.0 mm
  var rollerRimGap = 0.5 mm

  var mountThickness = 1.0 mm

  //the rollers' dimensions
  //  innerR + maxR == radius
  //  2*π*innerR == 1/cos(angle) * nbrRollers * (rollerGap + 2*maxR)
  def maxR = {
    val c1 = 2 * math.Pi / nbrRollers // circumference not angle
    val c2 = 2 / cos(angle)
    (radius * c1 - rollerGap) / (c1 + c2)
  }
  def innerR = radius - maxR
  def minR = rollerAxleRadius2 + mountThickness 
  //  width == cos(angle)*rollerHeight + 2*sin(angle) * minR + 2*cos(angle)*mountThickness
  def rollerHeight = (width - 2*sin(angle.abs) * minR - 2*cos(angle.abs)*mountThickness) / cos(angle.abs)

  def printParameters {
    Console.println("base parameters:")
    Console.println("  radius: " + radius)
    Console.println("  width: " + width)
    Console.println("  angle: " + angle)
    Console.println("  nbrRollers: " + nbrRollers)
    Console.println("  rollerAxleRadius1: " + rollerAxleRadius1)
    Console.println("  rollerAxleRadius2: " + rollerAxleRadius2)
    Console.println("  rollerGap: " + rollerGap)
    Console.println("  rollerRimGap: " + rollerRimGap)
    Console.println("  mountThickness: " + mountThickness)
    Console.println("derived parameters:")
    Console.println("  maxR: " + maxR)
    Console.println("  innerR: " + innerR)
    Console.println("  minR: " + minR)
    Console.println("  rollerHeight: " + rollerHeight)
  }

  def roller = new Roller(rollerHeight, maxR, minR, rollerAxleRadius2)

  //assumes it is centered at (0,0,0)
  protected def placeOnRim(s: Solid) = {
    val oriented = s.rotate(angle, 0, 0).translate(innerR, 0, width/2)
    val placed = for (i <- 0 until nbrRollers) yield oriented.rotate(0, 0, i * 2 * π / nbrRollers)
    Union(placed:_*)
  }
    
  protected def axleHeight = rollerHeight+2*mountThickness+20
  
  protected def rollersForCarving = {
    val r1 = Hull(roller.solid, roller.solid.moveX(2*maxR))
    val r2 = Bigger(r1, rollerRimGap).moveZ(-rollerHeight/2)
    val c = Cylinder(rollerAxleRadius1, axleHeight).moveZ(-axleHeight/2)
    placeOnRim(r2 + c)
  }

  protected def rollers = {
    val r = roller.solid.moveZ(-rollerHeight/2)
    //val c = Cylinder(rollerAxleRadius1, axleHeight).moveZ(-axleHeight/2)
    placeOnRim(r) //+ c)
  }

  def rim = {
    //TODO
    import backends.renderers.OpenScad._
    import backends.renderers.Renderable._

    val base = Tube(innerR-maxR-rollerRimGap, centerAxleRadius, width).toSolid
    val shaft = Translate(centerAxleRadius - shaftFlat, -centerAxleRadius/2, 0, Cube(2*centerAxleRadius, centerAxleRadius, width))

    val op = width * tan(angle.abs) / 2
    val ad = innerR
    val hyp = hypot(op, ad)
    val rth = minR*sin(angle.abs)*2 + mountThickness

    val lowerRing = Tube(hyp + rollerAxleRadius1 + mountThickness, centerAxleRadius, rth).toSolid
    val upperRing = lowerRing.moveZ(width - rth)

    base + shaft + lowerRing + upperRing
  }

  def hub = Difference(rim, rollersForCarving)

  //the hub in two halfs, easier to print
  def hubHalves(nbrHoles: Int) = {
    val angle = π * 2 / nbrHoles

    val holeOffsetX = innerR / 2
    val holeOffsetA = angle / 2

    val holes = for(i <- 0 until nbrHoles) yield
        Cylinder(rollerAxleRadius1, width).moveX(holeOffsetX).rotate(0, 0, holeOffsetA + i*angle)

    val withHoles = hub -- holes
    val lowerHalf = withHoles * Cylinder(innerR + maxR, width/2)
    val upperHalf = withHoles * Cylinder(innerR + maxR, width/2).moveZ(width/2)

    val kHeight = (width / 2 - 2) min 5
    val kRadius = 1.5 mm
    val knobX = holeOffsetX + kRadius - 1
    val knob = Cylinder(kRadius, kHeight)
    val knobs = for(i <- 0 until nbrHoles) yield knob.move(knobX, 0, width/2).rotate(0, 0, i*angle)

    val lowerWithKnobs = lowerHalf ++ knobs
    val upperWithKnobs = upperHalf -- knobs.map(Bigger(_, 2*tolerance))

    (lowerWithKnobs, upperWithKnobs)
  }

  def hubHalvesPrintable(nbrHoles: Int) = {
    val (l, h) = hubHalves(nbrHoles)
    (l, h.rotate(π, 0, 0).moveZ(-width/2))
  }

  def assembled = Union(hub, rollers)

  def assembly = {
    import scadla.assembly._
    val rollerP = new Part("roller", roller.solid)
    val axleHeight = width / cos(angle)
    val axle = new Part("filament, 1.75mm", Cylinder(rollerAxleRadius2, axleHeight))
    axle.vitamin = true
    val (lower,upper) = hubHalves(8)
    val lowerP = new Part("hub, lower half", lower)
    val upperP = new Part("hub, upper half", upper, Some(upper.rotate(π, 0, 0).moveZ(-width/2)))
    val asmbl0 = Assembly("Mecanum wheel")
    def place(as: Assembly, c: Assembly, w: Vector) = {
      val jt = Joint.revolute(0,0,1,Millimeters)
      val f0 = Frame(Vector(innerR.toMillimeters,0,width/2,Millimeters), Quaternion.mkRotation(angle, Vector(1,0,0,Millimeters)))
      (0 until nbrRollers).foldLeft(as)( (acc, i) => {
        val f1 = Frame(Vector(0,0,0,Millimeters), Quaternion.mkRotation(i * 2 * π / nbrRollers, Vector(0,0,1,Millimeters)))
        val frame = f0.compose(f1)
        acc + (frame, jt, c, w)
      })
    }
    val asmbl1 = asmbl0 +
                (Joint.fixed(0,0,-1,Millimeters), lowerP) +
                (Joint.fixed(0,0, 1,Millimeters), upperP)
    val asmbl2 = place(asmbl1, rollerP, Vector(0,0,-rollerHeight/2,Millimeters))
    place(asmbl2, axle, Vector(0,0, -axleHeight.toMillimeters, Millimeters))
  }

}

object MecanumWheel {

  def main(args: Array[String]) {
    //a small version
    val r = 25 mm
    val w = 18 mm
    val n = 12
    val a = π / 6
    val wheel1 = new MecanumWheel(r, w, a, n)
    val wheel2 = new MecanumWheel(r, w,-a, n)
    
    wheel1.printParameters
    
  ///* the parts */
  //val (lower1, upper1) = wheel1.hubHalvesPrintable(8)
  //val (lower2, upper2) = wheel2.hubHalvesPrintable(8)
  //val roller = wheel1.roller.skeleton
  //val mold = wheel1.roller.mold(6, 2)
  ///* save to files */
  //backends.OpenSCAD.toSTL(lower1, "lower1.stl")
  //backends.OpenSCAD.toSTL(upper1, "upper1.stl")
  //backends.OpenSCAD.toSTL(lower2, "lower2.stl")
  //backends.OpenSCAD.toSTL(upper2, "upper2.stl")
  //backends.OpenSCAD.toSTL(roller, "roller.stl")
  //backends.OpenSCAD.toSTL(mold,   "mold.stl")

    /* view the full wheel */
    val obj = wheel1.assembled
    backends.Renderer.default.view(obj)
    //backends.OpenSCAD.view(obj, Nil, Nil, Nil) //this version renders in a faster but with less details
  }

}
