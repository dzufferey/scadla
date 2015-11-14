package scadla.examples.cnc

import math._
import scadla._
import utils._
import InlineOps._
import Common._

/** LinearActuator + Gimbal and optional support */
object LinearActuatorBlock {


  def lengthOffset = LinearActuator.length/2 - LinearActuator.bearingCenter - 2
  def height = LinearActuator.height
  def length = LinearActuator.length + 10
  def width = LinearActuator.gimbalWidth - 2.0
  def lengthO = length + 16.0

  def gimbal = Gimbal.version2inner(
    length,         //length
    width,          //width
    height,         //height
    lengthOffset,   //lengthOffset
    0,              //widthOffset
    8,              //maxThickness
    5,              //minThickness
    2,              //knobLength
    1               //retainerThickness
  )

  def actuatorBase = LinearActuator.basePlate(true)

  def apply(support: Boolean = false) = {
    val g = gimbal.moveZ(height/2).rotateZ(Pi/2).moveY(-lengthOffset)
    val a = actuatorBase
    val ag = a + g
    if (support) {
      val b = biggerS(ag, looseTolerance)
      val c1 = centeredCubeXY(7, 9, height/2 - 1.5)
      val c2 = centeredCubeXY(9, 11, height/2 + 11) - centeredCubeXY(10,4,4).moveZ(height/2)
      val cs = Union(
        c1.moveY(  lengthO/2.0 - lengthOffset + 4.5),
        c1.moveY(- lengthO/2.0 - lengthOffset - 4.5),
        c2.moveX(  width/2.0 + 4.5),
        c2.moveX(- width/2.0 - 4.5)
      )
      val s = cs - b
      ag + s
    } else {
      ag
    }
  }

}
