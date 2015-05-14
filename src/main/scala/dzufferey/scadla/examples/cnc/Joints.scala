package dzufferey.scadla.examples.cnc

import math._
import dzufferey.scadla._
import utils._
import InlineOps._
import dzufferey.scadla.examples.fastener._
import Common._

/** A 2 degree of freedom joint which connects to a threaded rod.
 *  @param radius is the radius of the threaded rod
 *  vitamins:
 *      1 * threaded rod
 *      2 * nut for threaded rod
 *      2 * 608 bearing
 *      2 * M3 x ?? screw
 *      2 * M2 x ?? screw
 *      2 * M3 nut
 *      2 * M2 nut
 *      4 * M3 washer
 *      4 * M2 washer
 */
class Joint2DOF(radius: Double = Thread.UTS._1_4) {

  import Thread.ISO.{M2,M3}
  protected val m2t = M2 + tolerance
  protected val m3t = M3 + tolerance

  protected val xThreadHolder = radius * 2 + 2
  protected val yThreadHolder = (Hexagon.maxRadius(radius) * 2 + 1) + 6
  protected val zThreadHolder = 15.0

  protected val zOffset = 2.0
  
  protected val yBase = 17.0

  val part1a = {
    val t = Cylinder(15,15,7) - bearing
    val n = nut(radius + tolerance).moveZ(3)
    val s = Hexagon(radius, zThreadHolder).moveZ(2).rotateZ(Pi/6)
    val x = xThreadHolder
    val y = yThreadHolder
    val f = Cube(x,y,zThreadHolder).move(-x/2,-y/2,0) - s - n.moveX(-radius+1) - n.moveX(radius-1)
    t + f.rotateY(Pi/2).move( 13, 0, x/2) - t.moveZ(7)
  } 

  val part1b = {
    val t = tolerance
    val t2 = 2*t
    val c1 = Cube(13, 14, 5).move(-6.5,-7,0)
    val c2 = Cube(9+t2, 14, 5).move(-t-4.5, -7, 3)
    val n = nut(radius + tolerance).rotateZ(Pi/6)
    c1 - c2 - n
  }

  protected val dovetailP = Trapezoid(3-tolerance, 2-tolerance, 3, 3-tolerance).moveX(tolerance/2 +1.5)
  protected val dovetailN = Trapezoid(3+tolerance, 2+tolerance, 3.1, 3+tolerance).move(-tolerance/2 +1.5,-0.05,0)
  //assumes s is centered in the middle of [0,step]
  protected def chain(s: Solid, start: Double, step: Double, n: Int) = {
    val elts = for (i <- 0 until n) yield s.moveX(start + i * step)
    Union(elts:_*).rotateZ(Pi/2)
  }
  protected def chainYBase(s: Solid, step: Double) = {
    val k = floor(yBase / step).toInt
    val start = (yBase - k * step) / 2
    chain(s, start, step, k)
  }

  val part2a = {
    val c1 = Cylinder(m3t, m3t, 15+zOffset)
    val c4 = Cylinder(m2t, m2t, 10)
    val c5 = Cube(4, m2t*2+2, m2t*2+2)

    val offset = (xThreadHolder-7) / 2
    val pos = Union(
      Cube(8,yBase,3),
      c5.move(2,0,3),
      c5.move(2,yBase-c5.depth,3),
      chainYBase(dovetailP, 5),
      chainYBase(dovetailP, 5).moveX(11)
    )
    val neg = List(
      c1.move(4-offset,yBase/2,-c1.height+3),
      c4.rotateY(Pi/2).move(0,m2t+1 ,4+m2t),
      c4.rotateY(Pi/2).move(0,yBase-1-m2t,4+m2t)
    )

    pos -- neg
  }

  val part2b = {
    val c1 = Cylinder(m3t, m3t, 10)
    val c2 = Cylinder(4, 4, 3.5)
    val c3 = Cylinder(6, 6, 0.5)
    val c4 = Cylinder(m2t, m2t, 10)
    val c5 = Cube(m2t*2+2, m2t*2+2, 2)
    
    val sideLenght = 22

    val pos = Union(
      Hull(Cube(m2t*2+5,yBase,3), Cylinder(6,6,3).move(sideLenght,yBase/2,0)),
      (c2.moveZ(3.5) + c3.moveZ(3)).move(sideLenght,yBase/2,0),
      c5.move(3,0,3),
      c5.move(3,yBase-c5.depth,3)
    )

    val neg = List(
      c1.move(sideLenght,yBase/2,0),
      c4.move(4+m2t,m2t+1, 0),
      c4.move(4+m2t,yBase-1-m2t,0),
      chainYBase(dovetailN, 5).rotateY(Pi/2)
    )
    pos -- neg
  }

  val part2c = {
    val s1 = Cylinder(m3t, m3t, 10)
    val s3 = Cylinder(6, 6, 3)
    s3 - s1
  }
  
  val part2d = {
    val c1 = Cylinder(m3t, m3t, 10+zOffset)
    val c2 = Cylinder(4, 4, 7-tolerance)
    val c3 = Cylinder(6, 6, zOffset)
    c2.moveZ(zOffset) + c3 - c1
  }

  val parts = List(
    part1a,
    part1b,
    part2a,
    part2b,
    part2c,
    part2d
  )

}

object Gimbal2DOF {
}
