package scadla.examples.cnc

import scadla.*
import utils.*
import Trig.*
import utils.gear.*
import InlineOps.*
import thread.*
import Common.*
import scadla.EverythingIsIn.{millimeters, radians}  
import squants.space.{Length, Angle, Degrees, Millimeters}
import scala.language.postfixOps
import squants.space.LengthConversions.*

//parameters:
//- thickness of structure
//- bolt
//  - diameter
//  - head height
//  - non-threaded length
//  - threaded length
//- motor
//  - base fixation
//  - height from base to gear: 37.3
//  - rotor diameter
//  - additional thing to make the gear hold better to the rotor
//- chuck
//  - outer diameter (inner diameter is constrained by the size of the nut/bolt diameter)
//  - thread (type, lead, size)
//  - collet height

/*
for a parallax 1050kv outrunner brushless motor

rotary thing
    height: 2x5mm
    hole diameter: 5mm
    outer diameter: 17mm

base
    height 15mm
    space between the screw 23.5 mm
    screw diameter 3.5 mm
    base of rotary thing is 38 mm above the base
*/

//TODO as a class with the parameter in the ctor
object Spindle {

  ////////////////
  // parameters //
  ////////////////
  
  val motorBoltDistance = (30 + 30) / 2f //depends on the size of the motorBase and boltSupport
  
  val gearHeight = 10 mm
  val chuckNonThreadOverlap = 10 mm

  val topBoltWasher = 2 mm
  val bottomBoltWasher = 2 mm

  val boltThreadedLength = 25 mm //23
  val boltNonThreadedLength = 96 mm //86

  val boltSupportTop = boltNonThreadedLength - gearHeight - chuckNonThreadOverlap - topBoltWasher - bottomBoltWasher

  val motorBaseToGear = 37.3 mm
  val motorBaseHeight = boltSupportTop + topBoltWasher - motorBaseToGear 

  val bitsLength = 25 mm
  val colletLength = bitsLength - 3
  val chuckHeight = colletLength + boltThreadedLength + chuckNonThreadOverlap
  val innerHole = 9 mm //17.5 / 2

  ///////////
  // gears //
  ///////////

  lazy val gear1 = Gear.helical( motorBoltDistance * 2 / 3.0, 32, gearHeight, Twist(-0.03), tolerance)
  lazy val gear2 = Gear.helical( motorBoltDistance / 3.0    , 16, gearHeight, Twist(0.06), tolerance)
  val motorKnobs = {
    val c = Cylinder(3-tolerance, 2).moveZ(gearHeight)
    val u = Union(c.moveX(9), c.moveX(-9), c.moveY(9), c.moveY(-9))
    val r = (motorBoltDistance / 3.0) * (1.0 - 2.0 / 16)
    u * Cylinder(r, 20)
  }
  val nutTop = Cylinder(ISO.M8 * 3, 14) - nut.M8.moveZ(gearHeight)
  //gears
  lazy val gearBolt = gear1 + nutTop - Cylinder(ISO.M8 + tolerance, gearHeight)
  lazy val gearMotor = gear2 - Cylinder(ISO.M5 + tolerance, gearHeight) + motorKnobs


  /////////////////////////////////
  // bolt support and motor base //
  /////////////////////////////////

  //TODO not so square ...
  val motorBase = {

    val topZ = 3
    val bot = Trapezoid(46, 30, 5, 21).rotateX(-Pi/2).moveZ(5)
    val top = Cube(30,30,topZ).moveZ(motorBaseHeight - topZ)
    val subTtop = Cube(30,30-6.5,topZ).moveZ(motorBaseHeight - 2 * topZ)
    val base = Union(
        Hull(bot, subTtop),
        top
      )
    val nm3 = Bigger(Hull(nut.M3, nut.M3.moveX(5)), 0.4)
    val screw_hole = Cylinder(ISO.M3, 10)
    val fasteners = Seq(
      screw_hole.move( 3.25, 3.25, 0),
      screw_hole.move(26.75, 3.25, 0),
      screw_hole.move( 3.25,26.75, 0),
      screw_hole.move(26.75,26.75, 0),
      nm3.move(26.75, 3.25, 4),
      nm3.move(26.75,26.75, 4),
      nm3.rotateZ(Pi).move( 3.25, 3.25, 4),
      nm3.rotateZ(Pi).move( 3.25,26.75, 4)
    ).map(_.moveZ(motorBaseHeight - 10))

    val shaftHole = {
      val c = Cylinder( 8, motorBaseHeight).move(15, 15, 0) //hole for the lower part of the motor's shaft
      Hull(c, c.moveY(15))
    }
    val breathingSpaces = Seq(
      Cylinder(20, 50).moveZ(-25).scaleX(0.30).rotateX(Pi/2).move(15,15,motorBaseHeight)//,
      //Cylinder(20, 50).moveZ(-25).scaleY(0.30).rotateY(Pi/2).move(15,15,motorBaseHeight)
    )

    //the block on which the motor is screwed
    base - shaftHole -- fasteners -- breathingSpaces
  }

  val fixCoord = List[(Length, Length, Angle)](
    (31,  4, -Pi/5.2),
    (-1,  4, Pi+Pi/5.2),
    (34, 30,  0),
    (-4, 30, Pi),
    (34, 56, Pi/2),
    (-4, 56, Pi/2)
  )

  //centered at 0, 0
  val boltSupport = {
    val base = Hull(
      Cylinder(15, boltSupportTop),
      Cube(30, 1, motorBaseHeight).move(-15, 14, 0)
    )
    val lowerBearing = Hull(Cylinder(10, 7.5), bearing.moveZ(-0.5)) //add a small chamfer
    base - lowerBearing - bearing.moveZ(boltSupportTop - 7) - Cylinder(9, boltSupportTop)
  }

  val spindle = {
    val s = Cylinder(ISO.M3 + tolerance, 5)
    val fix = Cylinder(4, 4) + Cube(5, 8, 4).move(-5, -4, 0) - s
    Union(
      boltSupport.move(15, 15, 0),
      motorBase.moveY(30)
   ) ++ fixCoord.map{ case (x,y,a) => fix.rotateZ(a).move(x,y,0) }
  }


  ///////////
  // chuck //
  ///////////


  val chuck = Chuck.innerThread(13, innerHole+tolerance, chuckHeight, colletLength, 20)
  val slits = 4 //6
  val collet  = Collet.threaded(innerHole+1, innerHole, UTS._1_8, colletLength,
                                slits, 0.5, 1, 20, ISO.M2)
  val colletWrench = Collet.wrench(innerHole, UTS._1_8, slits, ISO.M2)

  def objects = Map(
    "gear_bolt" -> gearBolt,
    "gear_motor" -> gearMotor,
    "bolt_washer_top" -> Tube(6, (4 mm) + 2*tolerance, topBoltWasher),
    "bolt_washer_bot" -> Tube(6, (4 mm) + 2*tolerance, bottomBoltWasher),
    "spindle_body" -> spindle,
    "chuck_wrench" -> Chuck.wrench(13),
    "chuck" -> chuck.rotateX(Pi),
    "collet_inner" -> collet,
    "collet_wrench" -> colletWrench
  )

}

