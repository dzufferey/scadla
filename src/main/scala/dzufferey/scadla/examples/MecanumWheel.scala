package dzufferey.scadla.examples

import math._
import dzufferey.scadla._
import utils._
import InlineOps._

class Roller(height: Double, maxOuterRadius: Double,minOuterRadius: Double, innerRadius: Double) {

  val axis = 0.5
  val h = height - 2*axis

  protected def carveAxle(s: Solid) = s - Cylinder(innerRadius,innerRadius, height)

  def outline = {
    // r * f = maxOuterRadius
    // r * f * cos(a) = minOuterRadius
    // r * sin(a) = height/2
    val a = math.acos(minOuterRadius/maxOuterRadius)
    val r = h / 2 / math.sin(a)
    val f = maxOuterRadius / r
    val s = Translate(0, 0, height/2, Scale(f, f, 1, Sphere(r)))
    val c1 = Cylinder(maxOuterRadius, maxOuterRadius, h).moveZ(axis)
    val c2 = Cylinder(minOuterRadius, minOuterRadius, height)
    (s * c1) + c2
  }

  def solid = carveAxle(outline)

  def skeleton = {
    val base =
      carveAxle(
        Union(
          Cylinder(minOuterRadius, minOuterRadius, height),
          Scale(0.8, 0.8, 1, solid)))
    val angle = math.Pi / 8
    val grooveDepth = max(maxOuterRadius - minOuterRadius, 2)
    val inner = max(maxOuterRadius - grooveDepth, (minOuterRadius + innerRadius) / 2)
    val slice = pieSlice(maxOuterRadius, inner, angle, h).moveZ(axis)
    (0 until 8).foldLeft(base)( (acc, i) => {
      val rotated = Rotate(0, 0,i*2*angle, slice)
      Difference(acc, rotated)
    })
  }

  //mold for k*l roller
  //TODO put some grove to let the additional oogoo escape
  def mold(k: Int, l: Int) = {
    val wall = 2
    val distToWall = wall + maxOuterRadius
    val step = maxOuterRadius + distToWall
    val flatRoller = Rotate(-math.Pi/2, 0, 0, outline)
    val row = {
      val rs = for (i <- 0 until k) yield Translate( distToWall + i*step, 1, distToWall, flatRoller)
      Union(rs:_*)
    }
    val rows = {
      val rs = for (j <- 0 until l) yield Translate(0,j*(2+height), 0, row)
      Union(rs:_*)
    }
    val base = Cube(k*step+wall, l*(2+height),  distToWall)
    base - rows
  }

}

class MecanumWheel(radius: Double, width: Double, angle: Double, nbrRollers: Int) {

  //TODO ideally the projection of the arc created by the roller should match the shape of the wheel

  //some more parameters
  val centerAxleRadius = 2.5
  var rollerAxleRadius1 = 1.75/2
  var rollerAxleRadius2 = 1.0
  var rollerGap = 0.0
  var mountThickness = 1.0
  var mountDepth= 1.0
  var rollerRimGap = 0.5

  //the rollers' dimensions
  //  innerR + maxR == radius
  //  2*math.Pi*innerR == 1/cos(angle) * nbrRollers * (rollerGap + 2*maxR)
  def maxR = {
    val c1 = 2 * Pi / nbrRollers
    val c2 = 2 / cos(angle)
    (c1 * radius - rollerGap) / (c1 + c2)
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
    Console.println("  mountDepth: " + mountDepth)
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
    val placed = for (i <- 0 until nbrRollers) yield oriented.rotate(0, 0, i * 2 * Pi / nbrRollers)
    Union(placed:_*)
  }
    
  protected def axleHeight = rollerHeight+2*mountThickness+20
  
  protected def rollersForCarving = {
    val r1 = Hull(roller.solid, roller.solid.moveX(2*maxR))
    val r2 = bigger(r1, rollerRimGap).moveZ(-rollerHeight/2)
    val c = Cylinder(rollerAxleRadius1, rollerAxleRadius1, axleHeight).moveZ(-axleHeight/2)
    placeOnRim(r2 + c)
  }

  protected def rollers = {
    val r = roller.solid.moveZ(-rollerHeight/2)
    //val c = Cylinder(rollerAxleRadius1, rollerAxleRadius1, axleHeight).moveZ(-axleHeight/2)
    placeOnRim(r) //+ c)
  }

  def rim = tube(innerR+minR+mountDepth, centerAxleRadius, width)

  //TODO redesign the hub to be easier to print
  def hub = Difference(rim, rollersForCarving)

  def assembled = Union(hub, rollers)

  //TODO a list of parts in a printable position
}

object Main {

  def main(args: Array[String]) {
    val wheel = new MecanumWheel(20, 15, Pi/6, 12)
    //val wheel = new MecanumWheel(30, 20, Pi/4, 12)
    //val wheel = new MecanumWheel(30, 20, -Pi/4, 12)
    wheel.rollerAxleRadius1 = 1
    wheel.rollerAxleRadius2 = 1.15
    wheel.rollerRimGap = 1.0
    wheel.printParameters
    //val obj = wheel.hub
    //val obj = wheel.roller.solid
    //val obj = wheel.roller.skeleton
    //val obj = wheel.roller.mold(4, 2)
    val obj = wheel.assembled
    //backends.OpenSCAD.view(obj, Nil, Nil, Nil)
    backends.OpenSCAD.view(obj)
  }

}
