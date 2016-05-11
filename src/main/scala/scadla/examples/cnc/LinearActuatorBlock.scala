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
    1,              //retainerThickness
    2               //knobLength
  )

  def actuatorBase = LinearActuator.basePlate(true)

  def g = gimbal.moveZ(height/2).rotateZ(Pi/2).moveY(-lengthOffset)

  def support = {
    val simpler = g + LinearActuator.baseKnobs + Cylinder(LinearActuator.gb.externalRadius, LinearActuator.height)
    val b = Bigger(simpler, 2*supportGap)
    val c1 = CenteredCube.xy(7, 9, height/2 - 1.5)
    val c2 = CenteredCube.xy(8, 15, height/2 + 11) - CenteredCube(10,4,4).moveZ(height/2)
    val cs = Union(
      c1.moveY(  lengthO/2.0 - lengthOffset + 4.5),
      c1.moveY(- lengthO/2.0 - lengthOffset - 4.5),
      c2.moveX(  width/2.0 + 3.5),
      c2.moveX(- width/2.0 - 3.5)
    )
    cs - b
  }

  def apply(_support: Boolean = false) = {
    val a = actuatorBase
    val ag = a + g
    if (_support) {
      ag + support
    } else {
      ag
    }
  }

}
