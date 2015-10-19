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

/*
-transmission
    teeth are borderline small
-does the small one fit the motor
    a bit too snug
*/

  //TODO
  //-part to attach the gimbal

  /** motor to bearing distance */
  val mtb = 45
  val bearingRadius = 16
  val gimbalWidth = 48
  val gimbalKnob = 4

  val thread = Thread.UTS._1_4

  val motorLength = 28
  val motorSocket = 5 //how deep the screw goes in

  val rodLead = 1.0

  //for the motor screw
  val screwHead = 1.5
  val screwLength = 12

  val plateThickness = screwLength - motorSocket + screwHead

  val nbrTeethMotor = 10
  val nbrTeethTransmission = 26
  val nbrTeethBearing = 20

  val tScrew = Thread.ISO.M4
  val gearThickness = 7
  val bearingToGearSpace = 0.6
  val transmissionOffest = -mtb * (nbrTeethTransmission + nbrTeethBearing) / (nbrTeethMotor + nbrTeethBearing + 2*nbrTeethTransmission)
  
  val motor = Nema14(motorLength, -plateThickness-1)

  val gb = GearBearing(
    bearingRadius,
    motorLength + plateThickness,
    6,
    12,
    24,
    0.03,
    0,
    tightTolerance
  )
  
  val basePlate1 = {
    val plateX = Nema14.size
    val plateY = mtb + Nema14.size/2 + gb.externalRadius
    val rc0 = roundedCubeH(plateX, plateY, plateThickness, 2).move(-Nema14.size/2, -(mtb + Nema14.size/2), 0)
    val toRemove = Union(
        Union(
          motor,
          Cylinder(11+tolerance, plateThickness),
          Nema14.putOnScrew(Cylinder(2 * Thread.ISO.M3, screwHead))
        ).moveY(-mtb),
        Cylinder(gb.externalRadius - Gear.baseThickness/2, plateThickness)
      )
    rc0 - toRemove
  }
  
  lazy val basePlate2 = gb.outer

  lazy val basePlate = {
    val transmissionScrew = Cylinder(tScrew - tolerance, plateThickness + 1).moveY(transmissionOffest)
    val gimbalBase = {
      val c1 = Cylinder(6, gimbalWidth)
      val c2 = Cylinder(4, gimbalWidth+2*gimbalKnob).moveZ(-gimbalKnob)
      val c3 = Cylinder(Thread.ISO.M3, gimbalWidth+20).moveZ(-10)
      c1 + c2 -c3
    }
    val gimbalConnection = Difference(
      gimbalBase.moveZ(-gimbalWidth/2).rotateY(Pi/2).moveZ((plateThickness+motorLength)/2),
      Cylinder(gb.externalRadius - Gear.baseThickness/2, plateThickness+motorLength)
    )
    basePlate1 + basePlate2 + gimbalConnection -transmissionScrew
  }

  val motorGearRadius =        mtb * nbrTeethMotor        / (nbrTeethMotor + nbrTeethBearing + 2*nbrTeethTransmission)
  val transmissionGearRadius = mtb * nbrTeethTransmission / (nbrTeethMotor + nbrTeethBearing + 2*nbrTeethTransmission)
  val bearingGearRadius =      mtb * nbrTeethBearing      / (nbrTeethMotor + nbrTeethBearing + 2*nbrTeethTransmission)
  val tHelix = -0.02
  val mHelix = -tHelix * transmissionGearRadius / motorGearRadius
  val bHelix = -tHelix * transmissionGearRadius / bearingGearRadius
  
  lazy val motorGear = {
    val g = Gear.herringbone(motorGearRadius, nbrTeethMotor, gearThickness, mHelix, tightTolerance)
    g - bigger(motor, tolerance).moveZ(-5) //clear the flange
  }

  lazy val transmissionGear = {
    val g = Gear.herringbone(transmissionGearRadius, nbrTeethTransmission, gearThickness, tHelix, tightTolerance)
    val m = 11+looseTolerance
    val i = 0.5
    val inner = Cylinder(m, gearThickness).moveZ(i) + Cylinder(m-i, m, i)
    val top = for (k <- 0 until 12) yield PieSlice(m+i, m, Pi/12, i).moveZ(gearThickness).rotateZ(k*Pi/6)
    g - inner ++ top
  }

  val transmissionWasher = Tube(6, 4 + looseTolerance, bearingToGearSpace)
  
  val transmissionAxle = {
    val c1 = Cylinder(4 - tightTolerance/2, gearThickness + bearingToGearSpace + 1)
    val c2 = Cylinder(6, 1)
    val c3 = Cylinder(tScrew + looseTolerance, gearThickness + bearingToGearSpace + 2)
    c1 + c2 - c3
  }

  lazy val planetGear = gb.planet

  def ceilStep(length: Double, step: Double): Double = {
    (length / step).ceil * step
  }

  lazy val (sunGearPart1, sunGearPart2) = {
    val axis = Cylinder(thread + 3 * looseTolerance, motorLength + plateThickness + 2).moveZ(-1)
    val sun = gb.sun
    val gbh2 = gb.height/2
    val add = Gear.addenum(gb.sunRadius, gb.nbrTeethSun)
    val o = gb.sunRadius + add + 0.1
    val i = gb.sunRadius - add
    val chamfer = Union(
        Cylinder(o, gbh2 - 4*add).moveZ(2*add), 
        Cylinder(i, o, 2*add),
        Cylinder(o, i, 2*add).moveZ(gbh2-2*add)
    )
    val p1 = sun * chamfer
    val p2 = (sun * chamfer.moveZ(gbh2)).moveZ(-gbh2)
    val n = nut(thread + tolerance)
    val nh2 = 0.8 * thread //nut height / 2
    val part2 = p2 - axis - n.moveZ( ceilStep(gbh2 - nh2, rodLead) - nh2 ) //try to match the lead
    val sunGear = Gear.herringbone(bearingGearRadius, nbrTeethBearing, gearThickness, bHelix, tightTolerance)
    val h = gearThickness + bearingToGearSpace
    val p1g = p1.moveZ(h) + Cylinder(i, h) + sunGear - axis
    val part1 = p1g - n.moveZ( h + gbh2 - nh2 - ceilStep(h + gbh2 - nh2, rodLead) )
    (part1, part2)
  }

  val planetHelper = gb.planetHelper(1, looseTolerance)

  lazy val parts =  Map(
    "base"          -> basePlate,
    "motor"         -> motorGear,
    "transmission"  -> transmissionGear,
    "axle"          -> transmissionAxle,
    "washer"        -> transmissionWasher,
    "planet"        -> planetGear, //need 6 copies of that one
    "sun1"          -> sunGearPart1,
    "sun2"          -> sunGearPart2,
    "helper"        -> planetHelper
  )

}
