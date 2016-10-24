package scadla.examples.cnc

import math._
import scadla._
import utils._
import utils.gear._
import InlineOps._
import scadla.examples.fastener._
import scadla.examples.GearBearing
import Common._
import scadla.examples.reach3D.SpoolHolder


//TODO need to add some springy thing on one nut to reduce the backlash (preload)

object LinearActuator {

  val rodThread = Thread.ISO.M6
  val rodLead = 1.0

  val motorYOffset = 21.5
  def length = motorYOffset + Nema14.size
  def width = Nema14.size

  // motor
  val motorSocket = 4.0 //how deep the screw goes in
  val motor = Nema14(28, 0)

  // the screws holding the motors
  val screwHead = 2.0
  val screwLength = 12.0
  val motorScrew = Union(Cylinder(Thread.ISO.M3 + looseTolerance, screwLength),
                         Cylinder(2.1 * Thread.ISO.M3 + looseTolerance, screwHead))

  val bbRadius = 3.0 // airsoft ∅ is 6mm
  val bearingGapBase = 3.5
  val bearingGapSupport = 2.5

  val gearHeight = 10
  val nbrTeethMotor = 14
  val nbrTeethRod = 2 * nbrTeethMotor

  val motorGearRadius = motorYOffset * nbrTeethMotor / (nbrTeethMotor + nbrTeethRod)
  val rodGearRadius =   motorYOffset * nbrTeethRod   / (nbrTeethMotor + nbrTeethRod)
  val mHelix = -0.1
  val rHelix = -mHelix * motorGearRadius / rodGearRadius
  
  val grooveDepthBase = bbRadius / cos(Pi/4) - bearingGapBase / 2
  val grooveDepthSupport = bbRadius / cos(Pi/4) - bearingGapSupport / 2
  val grooveRadiusBase = SpoolHolder.adjustGrooveRadius(nut.maxOuterRadius(rodThread) + grooveDepthBase + 1) // +1 for the adjust radius
  val grooveRadiusSupport = SpoolHolder.adjustGrooveRadius(rodGearRadius - Gear.addenum(rodGearRadius, nbrTeethRod) - grooveDepthSupport)

  val grooveBase = SpoolHolder.flatGroove(grooveRadiusBase, grooveDepthBase)
  val grooveSupport = SpoolHolder.flatGroove(grooveRadiusSupport, grooveDepthSupport)
  
  // to attach to the gimbal
  val gimbalWidth = Nema14.size + 4
  val gimbalKnob = 7.0

  val plateThickness = screwLength - motorSocket + screwHead
  val pillarHeight = gearHeight + bearingGapBase + bearingGapSupport

  def basePlate(knob: Boolean = false, support: Boolean = false) = {
    val n14s2 = Nema14.size/2
    val gimbalMount = if (knob) {
        val thr = Thread.ISO.M3
        val w2k = gimbalWidth+2*gimbalKnob
        val c1 = Cylinder(6, gimbalWidth)
        val c2 = Cylinder(4 - tolerance, w2k).moveZ(-gimbalKnob)
        val c3 = Cylinder(thr, gimbalWidth+20).moveZ(-10)
        val nonOriented = c1 + c2 - c3
        val oriented = nonOriented.moveZ(-gimbalWidth/2).rotateY(Pi/2).moveZ(plateThickness/2)
        val trimmed = oriented * CenteredCube.xy(w2k, w2k, plateThickness)
        if (support) {
          val beam = CenteredCube.xy(w2k, 7, plateThickness / 2 - thr).moveZ(plateThickness / 2 + thr)
          Union(
            trimmed,
            beam - Bigger(trimmed, 2*supportGap)
          )
        } else {
          trimmed
        }
      } else Empty
    val base = Union(
        RoundedCubeH(width, length, plateThickness, 3).move(-n14s2, -(motorYOffset + n14s2), 0),
        gimbalMount
      )
    val motorMount =
      Union(
        //motor,
        Cylinder(8, 1),
        Cylinder(8, 11.5, plateThickness - 4).moveZ(1),
        Cylinder(11.5, 3).moveZ(plateThickness - 3),
        Nema14.putOnScrew(motorScrew)
      )
    val rodHole = Cylinder(rodThread + 1, plateThickness)
    val pillar = {
      val p = Cylinder(3, pillarHeight)
      val h = Cylinder(1.25, pillarHeight) // hole for self tapping screw
      (p - h).moveZ(-pillarHeight)
    }
    val supportPillars = {
      val s1 = width - 6
      NemaStepper.putOnScrew(s1, pillar)
    }
    Union(
      Difference(
        base,
        motorMount.moveY(-motorYOffset),
        rodHole,
        grooveBase
      ),
      supportPillars
    ).rotateX(Pi)
  }

  val supportHeight = 4
  val supportPlate = {
    val height = supportHeight
    Difference(
      RoundedCubeH(width, width, height, 3).move(-width/2,-width/2,0),
      NemaStepper.putOnScrew(width - 6, Cylinder(1.25, height)),
      Cylinder(rodThread + 1, height),
      grooveSupport
    ).rotateX(Pi)
  }
  
  def motorGear(support: Boolean) = {
    val g = Gear.herringbone(motorGearRadius, nbrTeethMotor, gearHeight, mHelix, tightTolerance)
    val done = g - Bigger(motor, 2.2*tolerance).moveZ(-5) //clear the flange
    if (support) done.moveZ(0.2) + Cylinder(motorGearRadius + 2, 0.2)
    else done
  }

  def rodGear(support: Boolean) = {
    val n = nut(rodThread)
    val nh = 1.6 * rodThread //nut height
    val g = Gear.herringbone(rodGearRadius, nbrTeethRod, gearHeight, rHelix, tightTolerance)
    val done = Difference(
        g,
        Cylinder(rodThread + 1, gearHeight),
        grooveBase,
        grooveSupport.mirror(0,0,1).moveZ(gearHeight),
        n.moveZ( gearHeight / 2 + 1),
        n.moveZ( gearHeight / 2 - 1 - nh)
      )
    if (support) (done + Cylinder(nh+2, 0.2).moveZ(gearHeight / 2 - 1)).moveZ(0.2) + Cylinder(rodGearRadius + 2, 0.2)
    else done
  }

  def gimbal = {
    val extraLength = 3.0 // space for the wiring
    // side of the motor
    val ySide1 = Nema14.size / 2.0 + motorYOffset
    val zSide1 = plateThickness / 2.0 + 30 //nema height
    val lengthSide1 = hypot(ySide1, zSide1) + extraLength
    // side of the gear
    val ySide2 = Nema14.size / 2.0
    val zSide2 = plateThickness / 2.0 + pillarHeight + supportHeight + 2 //screw head
    val lengthSide2 = hypot(ySide2, zSide2)
    val effectiveLength = lengthSide1 + lengthSide2
    //Console.println("lengthSide1: " + lengthSide1)
    //Console.println("lengthSide2: " + lengthSide2)
    val offset = (lengthSide1 - lengthSide2) / 2.0
    Gimbal.version2inner(
      effectiveLength,  //length
      gimbalWidth - 2,  //width
      30,               //height
      offset,           //lengthOffset
      0,                //widthOffset
      8,                //maxThickness
      5,                //minThickness
      1,                //retainerThickness
      2                 //knobLength
    )
  }
  
  def parts(support: Boolean) =  Map(
    "base"          -> (() => basePlate(true, support)),
    "motor"         -> (() => motorGear(support)),
    "support"       -> (() => supportPlate),
    "gear"          -> (() => rodGear(support))
  )

}
