package scadla.examples.cnc

import math._
import scadla._
import utils._
import InlineOps._
import scadla.examples.fastener._
import Common._

//The part that holds the linear actuator connected to the frame
object Gimbal {

  //TODO parameters for the bearing size

  //TODO make that smaller, require less material

  //TODO round to reduce the footprint

  def roundedSquare(length: Double,
                    width: Double,
                    height: Double,
                    rounding: Double,
                    thickness: Double,
                    lengthOffset: Double,
                    widthOffset: Double,
                    heightOffset: Double,
                    knobLength: Double,
                    retainerThickness: Double
                   ): Solid = {
    val t = thickness
    val roundingI = min( (length - 2*t) / length,
                         (width - 2*t) / width )
    val base = Difference(
      roundedCubeH(length, width, height, rounding),
      roundedCubeH(length-2*t, width-2*t, height, roundingI).move(t, t, 0)
    )
    val rLenght = length/2 + lengthOffset
    val rWidth = width/2 + widthOffset
    val rHeight = height/2 + heightOffset
    val rRemove = {
      val c1 = Cylinder(11+tolerance,t).moveZ(retainerThickness)
      val c2 = Cylinder(9,t+1).moveZ(-1)
      (c1 + c2)
    }
    val base2 = Difference(
      base,
      rRemove.rotateY(-Pi/2).move(t,rWidth,rHeight),
      rRemove.rotateY( Pi/2).move(length-t,rWidth,rHeight)
    )
    val knob = {
      val c1 = Cylinder(6, knobLength+1).moveZ(-1)
      val c2 = Cylinder(4, knobLength+7)
      val c3 = Cylinder(Thread.ISO.M3, knobLength+7)
      c1 + c2 - c3
    }
    val base3 = Union(
      base2,
      knob.rotateX(-Pi/2).move(rLenght, width, rHeight),
      knob.rotateX( Pi/2).move(rLenght,     0, rHeight)
    )
    base3
  }
  
  def apply(length: Double,
            width: Double,
            height: Double,
            rounding: Double,
            thickness: Double,
            lengthOffset: Double,
            widthOffset: Double,
            heightOffset: Double,
            spacing: Double,
            retainerThickness: Double
           ): Solid = {
    val outer = roundedSquare(
      length, width, height, rounding, thickness,
      lengthOffset, widthOffset, heightOffset,
      spacing + retainerThickness, retainerThickness
    )
    val st = spacing + thickness
    val rf = min(
      (length - 2*st) / length,
      (width - 2*st) / width
    )
    val inner = roundedSquare(
      width - 2*st, length - 2*st, height, rounding * rf, thickness,
      widthOffset, lengthOffset, heightOffset,
      spacing + retainerThickness, retainerThickness
    )
    outer + inner.rotateZ(-Pi/2).move(st, st + width - 2*st, 0) //TODO
  }
  
  def inner(length: Double,
            width: Double,
            height: Double,
            rounding: Double,
            thickness: Double,
            lengthOffset: Double,
            widthOffset: Double,
            heightOffset: Double,
            spacing: Double,
            retainerThickness: Double
           ): Solid = {
    val st2 = 2*(spacing + thickness)
    val rf = max(
      (length + st2) / length,
      (width + st2) / width
    )
    apply( length + st2,
           width + st2,
           height,
           rounding * rf,
           thickness,
           lengthOffset,
           widthOffset,
           heightOffset,
           spacing,
           retainerThickness
    )
  }


  //example:
  //val spacing = 5
  //r.toSTL(Gimbal.inner(
  //  LinearActuator.length + 2*spacing + 10,                 //length
  //  LinearActuator.gimbalWidth + 2*spacing,                 //width
  //  28,                                                     //height
  //  2,                                                      //rounding
  //  5,                                                      //thickness
  //  LinearActuator.length/2 - LinearActuator.bearingCenter, //lengthOffset
  //  0,                                                      //widthOffset
  //  0,                                                      //heightOffset
  //  spacing,                                                //spacing
  //  1                                                       //retainerThickness
  //), "gimbal.stl")

  protected def halfCylinder(r1: Double, r2: Double, h: Double) = {
    val c1 = PieSlice(r1, 0, Pi, h)
    val c2 = PieSlice(r2, 0, Pi, h).rotateZ(Pi)
    c1 + c2
  }

  protected def shape(maxThickness: Double, minThickness: Double,
                      length: Double, lengthOffset: Double,
                      width: Double, widthOffset: Double,
                      height: Double) = {
    def dist(center: Double, radius: Double) = {
      val l0 = radius - center
      val l1 = sqrt( pow(l0 - maxThickness, 2.0) + pow(height/2, 2.0))
      min(l1, l0 - minThickness)
    }
    val radiusLength1 = dist(length/2 + lengthOffset, length)
    val radiusLength2 = dist(length/2 - lengthOffset, length)
    val radiusWidth1 = dist(width/2 + widthOffset, width)
    val radiusWidth2 = dist(width/2 - widthOffset, width)
    ???
  }

}
