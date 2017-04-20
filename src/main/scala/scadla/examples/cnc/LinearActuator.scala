package scadla.examples.cnc

import scadla._
import utils._
import utils.gear._
import Trig._
import InlineOps._
import scadla.examples.fastener._
import scadla.examples.GearBearing
import Common._
import scadla.examples.reach3D.SpoolHolder
import scadla.EverythingIsIn.{millimeters, radians}  
import squants.space.{Length, Angle, Degrees, Millimeters}
import scala.language.postfixOps
import squants.space.LengthConversions._


//TODO need to add some springy thing on one nut to reduce the backlash (preload)

object LinearActuator {

  val rodThread = Thread.ISO.M6
  val rodLead = 1.0 mm

  val motorYOffset = 21.5 mm
  def length = motorYOffset + Nema14.size
  def width = Nema14.size

  // motor
  val motorSocket = 4.0 mm //how deep the screw goes in
  val motor = Nema14(28, 0)

  // the screws holding the motors
  val screwHead = 2.0 mm
  val screwLength = 12.0 mm
  val motorScrew = Union(Cylinder(Thread.ISO.M3 + looseTolerance, screwLength),
                         Cylinder(Thread.ISO.M3 * 2.1 + looseTolerance, screwHead))

  val bbRadius = 3.0 mm // airsoft ∅ is 6mm
  val bearingGapBase = 3.5 mm
  val bearingGapSupport = 2.5 mm

  val gearHeight = 10 mm
  val nbrTeethMotor = 14
  val nbrTeethRod = 2 * nbrTeethMotor

  val motorGearRadius = motorYOffset * nbrTeethMotor / (nbrTeethMotor + nbrTeethRod)
  val rodGearRadius =   motorYOffset * nbrTeethRod   / (nbrTeethMotor + nbrTeethRod)
  val mHelix = Twist(-0.1)
  val rHelix = -mHelix * (motorGearRadius / rodGearRadius)
  
  val grooveDepthBase = bbRadius / cos(Pi/4) - bearingGapBase / 2
  val grooveDepthSupport = bbRadius / cos(Pi/4) - bearingGapSupport / 2
  val grooveRadiusBase = SpoolHolder.adjustGrooveRadius(nut.maxOuterRadius(rodThread) + grooveDepthBase + 1) // +1 for the adjust radius
  val grooveRadiusSupport = SpoolHolder.adjustGrooveRadius(rodGearRadius - Gear.addenum(rodGearRadius, nbrTeethRod).toMillimeters - grooveDepthSupport)

  val grooveBase = SpoolHolder.flatGroove(grooveRadiusBase, grooveDepthBase)
  val grooveSupport = SpoolHolder.flatGroove(grooveRadiusSupport, grooveDepthSupport)
  
  // to attach to the gimbal
  val gimbalWidth = Nema14.size + 4
  val gimbalKnob = 7.0 mm

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
    val nh = rodThread * 1.6 //nut height
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

  val gimbalLength1 = {
    val extraLength = 3.0 // space for the wiring
    // side of the motor
    val ySide1 = Nema14.size / 2.0 + motorYOffset
    val zSide1 = plateThickness / 2.0 + 30 //nema height
    hypot(ySide1, zSide1) + extraLength
  }

  val gimbalLength2 = {
    // side of the gear
    val ySide2 = Nema14.size / 2.0
    val zSide2 = plateThickness / 2.0 + pillarHeight + supportHeight + 2 //screw head
    hypot(ySide2, zSide2)
  }

  val gimbalLength = gimbalLength1 + gimbalLength2

  val gimbalOffset = (gimbalLength1 - gimbalLength2) / 2.0

  val retainerThickness = 1 mm

  def gimbal = {
    Gimbal.inner(
      gimbalLength, //length
      gimbalWidth - retainerThickness * 2,  //width
      30,   //height
      gimbalOffset, //lengthOffset
      0,    //widthOffset
      8,    //maxThickness
      5,    //minThickness
      retainerThickness,    //retainerThickness
      2     //knobLength
    )
  }
  
  def parts(support: Boolean) =  Map(
    "base"          -> (() => basePlate(true, support)),
    "motor"         -> (() => motorGear(support)),
    "support"       -> (() => supportPlate),
    "gear"          -> (() => rodGear(support))
  )

  // ¬centered → axis of the actuator is at 0
  //  centered → center of mass is at 0
  def assembled(withGears: Boolean = true, centered: Boolean = false) = {
    val block = Union(
      basePlate(true, false),
      supportPlate.rotateX(Pi).moveZ(pillarHeight),
      if (withGears) motorGear(false).move(0, motorYOffset, bearingGapBase) else Empty,
      if (withGears) rodGear(false).moveZ(bearingGapBase) else Empty,
      gimbal.model.rotateZ(-Pi/2).move(0,gimbalOffset,-plateThickness/2)
    ).moveZ(plateThickness/2)
    if (centered) {
      block.moveY(-gimbalOffset)
    } else {
      block
    }
  }


}
