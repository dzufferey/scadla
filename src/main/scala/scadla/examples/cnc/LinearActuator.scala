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

  val rodThread = Thread.UTS._1_4
  val rodLead = 1.0

  // motor
  val motorSocket = 4.0 //how deep the screw goes in
  val motor = Nema14(28, 0)

  // the screws holding the motors
  val screwHead = 2.0
  val screwLength = 12.0
  val motorScrew = Union(Cylinder(Thread.ISO.M3 + looseTolerance, screwLength),
                         Cylinder(2.1 * Thread.ISO.M3 + looseTolerance, screwHead))

  // BB: airsoft ∅ is 6mm, 0.177 cal ∅ is 4.5
  val bbRadius = 3.0
  val bb = Sphere(bbRadius)

  val gearHeight = 10
  val nbrTeethMotor = 8
  val nbrTeethRod = 3 * nbrTeethMotor

  val groveDepth = bbRadius - 0.5
  val groveRadiusBase = adjustGroveRadius( 2 * rodThread + 3)
  val groveRadiusSupport = adjustGroveRadius(Nema14.size/2 - 6)
  
  // to attach to the gimbal
  val gimbalWidth = Nema14.size + 4
  val gimbalKnob = 7.0

  val plateThickness = screwLength - motorSocket + screwHead

  // make sures the BBs fit nicely
  def adjustGroveRadius(radius: Double): Double = {
    assert(radius > bbRadius)
    // find n such that the circumscribed radius is the closest to the given radius
    val sideLength = 2*bbRadius + looseTolerance
    val nD = Pi / asin(sideLength / (2 *radius))
    val n = nD.toInt // rounding to nearest regular polygon
    circumscribedRadius(n, sideLength)
  }

  def grove(radius: Double, depth: Double, angle: Double = Pi/2) = {
    val width = depth / tan(Pi/2 - angle/2)
    val outer = Cylinder(radius + width, radius, depth)
    val inner = Cylinder(radius - width, radius, depth)
    outer - inner
  }
  
  val motorYOffset = 21.5
  def length = motorYOffset + Nema14.size
  def width = Nema14.size

  def basePlate(knob: Boolean = false, support: Boolean = false) = {
    val plateX = width
    val plateY = length
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
        RoundedCubeH(plateX, plateY, plateThickness, 3).move(-n14s2, -(motorYOffset + n14s2), 0),
        gimbalMount
      )
    val motorMount =
      Union(
        //motor,
        Cylinder(8, 1),
        Cylinder(8, 11+tolerance, plateThickness - 4).moveZ(1),
        Cylinder(11+tolerance, 3).moveZ(plateThickness - 3),
        Nema14.putOnScrew(motorScrew)
      )
    val rodHole = Cylinder(rodThread + 1, plateThickness)
    val gap = 0 // 0.5
    val pillarHeight = gearHeight + 2 - gap
    val pillar = {
      val p = Cylinder(3, pillarHeight)
      val h = Cylinder(1, 5) // hole for self tapping screw
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
        grove(groveRadiusBase, groveDepth)
      ),
      supportPillars
    )
  }

  val supportPlate = {
    val height = 4
    Difference(
      RoundedCubeH(width, width, height, 3).move(-width/2,-width/2,0),
      NemaStepper.putOnScrew(width - 6, Cylinder(1, height)),
      Cylinder(rodThread + 1, height),
      grove(groveRadiusSupport, groveDepth)
    )
  }
  
  val motorGearRadius = motorYOffset * nbrTeethMotor / (nbrTeethMotor + nbrTeethRod)
  val rodGearRadius =   motorYOffset * nbrTeethRod   / (nbrTeethMotor + nbrTeethRod)
  val mHelix = -0.1
  val rHelix = -mHelix * motorGearRadius / rodGearRadius
  
  lazy val motorGear = {
    val g = Gear.herringbone(motorGearRadius, nbrTeethMotor, gearHeight, mHelix, tightTolerance)
    g - Bigger(motor, looseTolerance).moveZ(-5) //clear the flange
  }

  lazy val rodGear = {
    val n = nut(rodThread + looseTolerance)
    val nh = 1.6 * rodThread //nut height
    val g = Gear.herringbone(rodGearRadius, nbrTeethRod, gearHeight, rHelix, tightTolerance)
    Difference(
      g,
      Cylinder(rodThread + 1, gearHeight),
      grove(groveRadiusBase, groveDepth),
      grove(groveRadiusSupport, groveDepth).mirror(0,0,1).moveZ(gearHeight),
      n.moveZ( gearHeight / 2 + 1),
      n.moveZ( gearHeight / 2 - 1 - nh)
    )
  }
  
  lazy val parts =  Map(
    "base"          -> basePlate(),
    "motor"         -> motorGear,
    "support"       -> supportPlate,
    "gear"          -> rodGear
  )

}
