package scadla.examples.cnc

import math._
import scadla._
import utils._
import utils.gear._
import InlineOps._
import scadla.examples.fastener._
import scadla.examples.GearBearing
import Common._

object LinearActuator {

  //TODO what are the parameters:
  // - motor size
  // - threaded rod radius/diameter (nut size follows from that)
  // - length of planetary gears (roughtly same as motor depth + plate thickness)
  // - gear ratio between the motor gear and the rod gear
  // - base plate thickness
  // - space between plate and gears
  // - threaded rod lead/pitch (to make sure the rod goes through the three nuts without problem)

  val thread = Thread.UTS._1_4

  val motorLength = 28

  val plateThickness = 5

  val screwLead = 1.0

  val nbrTeethMotor = 20
  val nbrTeethBearing = 40

  val bearingToGearSpace = 1
  
  val motor = Nema14(motorLength, -plateThickness-1)

  val gb = GearBearing(
    15,
    motorLength + plateThickness,
    6,
    12,
    24,
    0.03,
    0,
    tightTolerance
  )

  lazy val basePlate = {
    val rc0 = roundedCubeH(40, 2*40, plateThickness, 2).move(-20, -60, 0)
    val rc1 = rc0 - motor.moveY(-40) - Cylinder(11+tolerance, plateThickness).moveY(-40)
    val rc2 = rc1 - Cylinder(19, plateThickness) + gb.outer
    rc2
  }

  val motorGearRadius = 30 * nbrTeethMotor / (nbrTeethMotor + nbrTeethBearing)
  val bearingGearRadius = 30 * nbrTeethBearing / (nbrTeethMotor + nbrTeethBearing)
  
  lazy val motorGear = {
    val g = Gear.herringbone(motorGearRadius, nbrTeethMotor, plateThickness, 0.02, tightTolerance)
    g - motor.moveZ(-5) //clear the flange
  }

  lazy val planetGear = gb.planet //needs 6 copies of that one

  def ceilStep(length: Double, step: Double): Double = {
    (length / step).ceil * step
  }

  lazy val (sunGearPart1, sunGearPart2) = {
    val axis = Cylinder(thread + 3 * looseTolerance, motorLength + plateThickness + 2).moveZ(-1)
    val sun = gb.sun
    val gbh2 = gb.height/2
    val p1 = sun * centeredCubeXY(40, 40, gbh2)
    val p2 = (sun * centeredCubeXY(40, 40, gbh2).moveZ(gbh2)).moveZ(-gbh2)
    val n = nut(thread)
    val nh2 = 0.8 * thread //nut height / 2
    val part2 = p2 - axis - n.moveZ( - nh2 ) - n.moveZ( ceilStep(gbh2 - nh2, screwLead) - nh2 ) //try to match the lead
    val sunGear = Gear.herringbone(bearingGearRadius, nbrTeethBearing, plateThickness, 0.02, tightTolerance)
    val h = plateThickness + bearingToGearSpace
    val p1g = p1.moveZ(h) + Cylinder(gb.sunRadius, h) + sunGear - axis
    val part1 = p1g - n.moveZ(h + gbh2 - nh2) - n.moveZ( h + gbh2 - nh2 - ceilStep(h + gbh2 - nh2, screwLead) )
    (part1, part2)
  }

}
