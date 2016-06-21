package scadla.examples.cnc

import math._
import scadla._
import utils._
import InlineOps._
import scadla.examples.fastener._
import Common._

//The part that holds the linear actuator connected to the frame
//TODO split in multiple parts connected with dovetail (and screw) for easier printing
object Gimbal {

  def carveBearing(shape: Solid, retainerThickness: Double,
                   width: Double, lengthOffset: Double,
                   maxThickness: Double) = {
    val b = Cylinder(11 + looseTolerance, maxThickness)
    val b1 = Cylinder(9, width) +
             b.moveZ(-retainerThickness) +
             b.moveZ(width-maxThickness+retainerThickness)
    val b2 = b1.moveZ(-width/2).rotateX(Pi/2)
    val b3 = b2.moveX(lengthOffset)
    shape - b3
  }

  def addKnob(shape: Solid, knobLength: Double,
              length: Double, widthOffset: Double) = {
    val k = Cylinder(4 - tolerance, knobLength + 7) + Cylinder(6, knobLength)
    shape +
      k.rotateY(-Pi/2).move(-length/2, widthOffset, 0) +
      k.rotateY(Pi/2).move(length/2, widthOffset, 0)
  }

  def shapeOuter(maxThickness: Double, minThickness: Double,
                 length: Double, lengthOffset: Double,
                 width: Double, widthOffset: Double,
                 height: Double) = {

    def halfCylinder(r1: Double, r2: Double, h: Double) = {
      val c1 = if (r1 <= r2) Cylinder(r1, h) else PieSlice(r1, 0, Pi, h)
      val c2 = if (r2 <= r1) Cylinder(r2, h) else PieSlice(r2, 0, Pi, h).rotateZ(Pi)
      c1 + c2
    }

    def distO(center: Double, radius: Double) = {
      val l0 = radius - center
      val l1 = hypot( l0 - maxThickness + minThickness, height/2)
      min(l1, l0)
    }

    def distI(center: Double, radius: Double) = {
      val l0 = radius - center
      val l1 = hypot( l0 - maxThickness, height/2)
      max(l1, l0 - minThickness)
    }
    
    val innerWidth = width - 2 * maxThickness

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
  
  def shapeInner(maxThickness: Double, minThickness: Double,
                 length: Double, lengthOffset: Double,
                 width: Double, widthOffset: Double,
                 height: Double) = {
    shapeOuter(maxThickness, minThickness,
               length + 2 * maxThickness, lengthOffset,
               width + 2 * maxThickness, widthOffset,
               height)
  }

  def version2(
        length: Double, 
        width: Double,
        height: Double,
        lengthOffset: Double,
        widthOffset: Double,
        maxThickness: Double,
        minThickness: Double,
        retainerThickness: Double,
        knobLength: Double) = {
    val s0 = shapeOuter(maxThickness, minThickness,
                        length, lengthOffset,
                        width, widthOffset,
                        height)
    val s1 = carveBearing(s0, retainerThickness, width, lengthOffset, maxThickness)
    val s2 = addKnob(s1, knobLength, length, widthOffset)
    s2
  }
  
  def version2inner(
        length: Double, 
        width: Double,
        height: Double,
        lengthOffset: Double,
        widthOffset: Double,
        maxThickness: Double,
        minThickness: Double,
        retainerThickness: Double,
        knobLength: Double) = {
    version2(
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
