package scadla.examples.cnc

import scadla._
import utils._
import Trig._
import InlineOps._
import thread._
import Common._
import scadla.EverythingIsIn.{millimeters, radians}  
import squants.space.Length

//The part that holds the linear actuator connected to the frame

// outer dimensions
class Gimbal(
        length: Length,
        width: Length,
        height: Length,
        lengthOffset: Length,
        widthOffset: Length,
        maxThickness: Length,
        minThickness: Length,
        retainerThickness: Length,
        knobLength: Length) {
  import backends.renderers.OpenScad._

  protected def carveBearing(shape: Solid) = {
    val b = Cylinder(11 + looseTolerance, maxThickness)
    val b1 = Cylinder(9, width) +
             b.moveZ(-retainerThickness) +
             b.moveZ(width-maxThickness+retainerThickness)
    val b2 = b1.moveZ(-width/2).rotateX(Pi/2)
    val b3 = b2.moveX(lengthOffset)
    shape - b3
  }

  protected def addKnob(shape: Solid) = {
    val k0 = Union(
      Cylinder(4 - tolerance, knobLength + 7),
      Cylinder(6, knobLength + tolerance).moveZ(-tolerance) //tolerance guarantees interferences
    )
    val k1 = k0 - Cylinder(ISO.M3, knobLength + 7)
    shape +
      k1.rotateY(-Pi/2).move(-length/2, widthOffset, 0) +
      k1.rotateY(Pi/2).move(length/2, widthOffset, 0)
  }

  protected def halfCylinder(r1: Length, r2: Length, h: Length) = {
    val c1 = if (r1 <= r2) Cylinder(r1, h) else PieSlice(r1, 0, Pi, h).toSolid
    val c2 = if (r2 <= r1) Cylinder(r2, h) else PieSlice(r2, 0, Pi, h).toSolid.rotateZ(Pi)
    c1 + c2
  }

  val innerWidth = width - 2 * maxThickness

  protected def shapeOuter = {

    def distO(center: Length, radius: Length) = {
      val l0 = radius - center
      val l1 = hypot( l0 - maxThickness + minThickness, height/2)
      l1 min l0
    }

    def distI(center: Length, radius: Length) = {
      val l0 = radius - center
      val l1 = hypot( l0 - maxThickness, height/2)
      l1 max (l0 - minThickness)
    }
    
    val radiusWidth1 = distO(width/2 + widthOffset, width)
    val radiusWidth2 = distO(width/2 - widthOffset, width)
    
    val radiusLength1 = distI(length/2 + lengthOffset, length)
    val radiusLength2 = distI(length/2 - lengthOffset, length)

    val outer = halfCylinder( radiusWidth1, radiusWidth2, length).rotateY(Pi/2) * CenteredCube.yz(length, width, height)
    val inner = halfCylinder( radiusLength1, radiusLength2, innerWidth).
                  moveZ(-innerWidth/2).
                    rotateZ(-Pi/2).
                      rotateX(Pi/2).
                        move( length/2+lengthOffset, widthOffset, 0)
    
    val x0 = outer - inner
    val x1 = x0 - CenteredCube.yz(length-2*maxThickness, innerWidth, height).moveX(maxThickness)
    x1.moveX(-length/2) //center the object at (0,0,0)
  }

  def model = {
    val s0 = shapeOuter
    val s1 = carveBearing(s0)
    val s2 = addKnob(s1)
    s2
  }

  //split into multiple parts connected with dovetail (and screws) for easier printing
  def parts(support: Boolean = false) = {
    val screw = Cylinder(woodScrewHeadRadius, woodScrewHeadHeight+10).moveZ(-10) + Cylinder(woodScrewRadius, woodScrewLength+woodScrewHeadHeight)
    val screwOffset = woodScrewHeadRadius max(minThickness/2)
    val screwX = length/2 - screwOffset
    val screwY = width/2
    val screwZ = 0
    val m = Difference(
      model,
      screw.rotateX(-Pi/2).move(-screwX,-screwY, screwZ),
      screw.rotateX(-Pi/2).move( screwX,-screwY, screwZ),
      screw.rotateX( Pi/2).move(-screwX, screwY, screwZ),
      screw.rotateX( Pi/2).move( screwX, screwY, screwZ)
    )
    val c = Cube(length,width,height).move(-length/2, -width/2, -height/2)
    val part1 = c.moveY(maxThickness - width)
    val part2 = c.moveY(width - maxThickness)
    val part3 = c.moveX(maxThickness - length)
    val part4 = c.moveX(length - maxThickness)
    val delta = 2 //XXX fix to make 0
    val t = Trapezoid(height/3, height/2, width, maxThickness+delta).move(-height/4, -width/2, -maxThickness-delta)
    val doveTail = Difference(
      c,
      t.rotateY(-Pi/2).move(-width, 0,   height/2),
      t.rotateY(-Pi/2).move(-width, 0, - height/2),
      t.rotateY( Pi/2).move( width, 0,   height/2),
      t.rotateY( Pi/2).move( width, 0, - height/2)
    )
    val dovetail1 = doveTail.moveY(maxThickness - width)
    val dovetail2 = doveTail.moveY(width - maxThickness)
    val dovetailB1 = Bigger(dovetail1, tightTolerance)
    val dovetailB2 = Bigger(dovetail2, tightTolerance)
    val ps = Seq(
      m * dovetail1,
      m * dovetail2,
      m * part3 - dovetailB1 - dovetailB2,
      m * part4 - dovetailB1 - dovetailB2
    )
    if (support) {
      def addSupport(s: Solid) = {
        val under = Cube(height, innerWidth, maxThickness - minThickness).moveY(-width/2 + (width-innerWidth)/2)
        val sb1 = Bigger(s, 2*supportGap)
        val sb2 = Minkowski(s, Cylinder(1.5 * supportGap, 0.0625))
        s + (under - sb1 - sb2)
      }
      Seq(
        ps(0).rotateX(-Pi/2),
        ps(1).rotateX( Pi/2),
        addSupport(ps(2).move( length/2,0, height/2).rotateY( Pi/2).moveZ(maxThickness)),
        addSupport(ps(3).move(-length/2,0,-height/2).rotateY(-Pi/2).moveZ(maxThickness))
      )
    } else {
      ps
    }
  }
  
  def printDimensions {
    Console.println(
      "outer length: " + length + "\n" +
      "outer width: " + width + "\n" +
      "inner length: " + (length - 2 * maxThickness) + "\n" +
      "inner width: " + (width - 2 * maxThickness) + "\n" +
      "height: " + height
    )
  }

}

object Gimbal {

  def outer(
        length: Length,
        width: Length,
        height: Length,
        lengthOffset: Length,
        widthOffset: Length,
        maxThickness: Length,
        minThickness: Length,
        retainerThickness: Length,
        knobLength: Length) = {
    new Gimbal(length, width, height, lengthOffset, widthOffset,
               maxThickness, minThickness, retainerThickness, knobLength)
  }

  def outerDimensions(
        length: Length, 
        width: Length,
        height: Length,
        lengthOffset: Length,
        widthOffset: Length,
        maxThickness: Length,
        minThickness: Length,
        retainerThickness: Length,
        knobLength: Length) = {
    val g = new Gimbal(length, width, height, lengthOffset, widthOffset,
                       maxThickness, minThickness, retainerThickness, knobLength)
    g.model
  }

  def inner(
        length: Length,
        width: Length,
        height: Length,
        lengthOffset: Length,
        widthOffset: Length,
        maxThickness: Length,
        minThickness: Length,
        retainerThickness: Length,
        knobLength: Length) = {
    outer(
        length + 2 * maxThickness,
        width + 2 * maxThickness,
        height,
        lengthOffset,
        widthOffset,
        maxThickness,
        minThickness,
        retainerThickness,
        knobLength)
  }

  def innerDimensions(
        length: Length,
        width: Length,
        height: Length,
        lengthOffset: Length,
        widthOffset: Length,
        maxThickness: Length,
        minThickness: Length,
        retainerThickness: Length,
        knobLength: Length) = {
    outerDimensions(
        length + 2 * maxThickness,
        width + 2 * maxThickness,
        height,
        lengthOffset,
        widthOffset,
        maxThickness,
        minThickness,
        retainerThickness,
        knobLength)
  }

}
