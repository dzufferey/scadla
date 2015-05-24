package dzufferey.scadla.examples.cnc

import math._
import dzufferey.scadla._
import utils._
import InlineOps._
import dzufferey.scadla.examples.fastener._
import Common._

//TODO
//-two gears:
//  -one for the motor (with knows)
//  -one for the bolt (with hole for the nut)
//-vertical thing to hold the bolt
//-place to hold the motor
//-chuck to put on the bolt (partially flat thread ? asymmetric thread ?)
//-...

//parameters:
//- tolerance ?uniform?
//- thickness of structure
//- bolt
//  - diameter
//  - head height
//  - space between the head and the bottom nut
//  - space between the two bottom nuts
//- motor
//  - base fixation
//  - height from base to gear: 37.3
//  - rotor diameter
//  - additional thing to make the gear hold better to the rotor
//- bearing
//  - height
//  - outer diameter (inner diameter must be the same as the bolt diameter)
//- chuck
//  - outer diameter (inner diameter is constrained by the size of the nut/bolt diameter)
//  - thread (type, lead, size)
//  - collet height
//  - height of locknut

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

object Spindle {

  var objects = Map[String, Solid]()

  ////////////////
  // parameters //
  ////////////////
  
  val motorBaseToGear = 37.3
  val bitsLength = 25
  val boltSupportTop = 86 - 10 - 5
  //10 space used by the gear at the top
  //5 space taken by the chuck at the bottom


  //derived constants
  val motorBoltDistance = (30 + 30) / 2f //depends on the size of the motorBase and boltSupport
  val motorBaseHeight = boltSupportTop + 1 - motorBaseToGear 

  ///////////
  // gears //
  ///////////

  //TODO creating the gears already does some rendering!!
  val gear1 = Gear.helical( motorBoltDistance * 2 / 3.0, 32, 10,-0.03, tolerance)
  val gear2 = Gear.helical( motorBoltDistance / 3.0    , 16, 10, 0.06, tolerance)
  val motorKnobs = {
    val c = Cylinder(3-tolerance, 2).moveZ(10)
    val u = Union(c.moveX(9), c.moveX(-9), c.moveY(9), c.moveY(-9))
    val r = (motorBoltDistance / 3.0) * (1.0 - 2.0 / 16)
    u * Cylinder(r, 20)
  }
  val nutTop = Cylinder(Thread.ISO.M8 * 3, 14) - nut.M8.moveZ(10)
  //gears
  val gearBolt = gear1 + nutTop - Cylinder(4+tolerance, 10)
  val gearMotor = gear2 - Cylinder(2.5+tolerance, 10) + motorKnobs

  objects += "gear_bolt" -> gearBolt
  objects += "gear_motor" -> gearMotor
  objects += "gear_washer" -> Tube(6, 4 + 2*tolerance, 1).moveZ(10)
  

  /////////////////////////////////
  // bolt support and motor base //
  /////////////////////////////////

  //boundind box is Cube(30, 30, motorBaseHeight)
  val motorBase = {

    val nm3 = bigger(Hull(nut.M3, nut.M3.moveX(5)), 0.4)
    val screw_hole = Cylinder(Thread.ISO.M3, 10)
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

    val shaftHole = Cylinder(10, motorBaseHeight).move(15, 15, 0) //hole for the lower part of the motor's shaft
    val breathingSpaces = Seq(
      Cylinder(20, 50).moveZ(-25).scaleX(0.30).rotateX(Pi/2).move(15,15,motorBaseHeight),
      Cylinder(20, 50).moveZ(-25).scaleY(0.30).rotateY(Pi/2).move(15,15,motorBaseHeight)
    )

    //the block on which the motor is screwed
    Cube(30, 30, motorBaseHeight) - shaftHole -- fasteners -- breathingSpaces
  }

  //centered at 0, 0
  val boltSupport = {
    val base = Cube(30, 30, boltSupportTop).move(-15, -15, 0)
    val lowerBearing = Hull(Cylinder(10, 7.5), bearing.moveZ(-0.5)) //add a small chamfer
    base - lowerBearing - bearing.moveZ(boltSupportTop - 7) - Cylinder(9, boltSupportTop)
  }

  val spindle = {
    val s = Cylinder(Thread.ISO.M3 + tolerance, 5)
    val fix = Cylinder(5, 5) + Cube(5, 10, 5).move(-5, -5, 0) - s
    Union(
      boltSupport.move(15, 15, 0),
      motorBase.moveY(30),
      fix.move(35, 5, 0),
      fix.move(35, 55, 0),
      fix.rotateZ(Pi).move(-5, 5, 0),
      fix.rotateZ(Pi).move(-5, 55, 0)
   )
  }

  objects += "spindle_body" -> spindle


  ///////////
  // chuck //
  ///////////

  val colletLength = bitsLength - 3
  val chuckHeight = 5.5 + 8 + colletLength + 15 //(5.5 + 8) represent the space for the nuts

  val innerHole = 17.5 / 2
  val chuckInner = Chuck.innerThread(13, innerHole+tolerance, chuckHeight, colletLength, 20)
  val colletInner = Collet.threaded(innerHole+1, innerHole, Thread.UTS._1_8+tolerance, colletLength,
                                    6, 1, 2, 20, tolerance, Thread.ISO.M2)
  //val chuckBlocker = Chuck.blocker( innerHole, tolerance)

  val colletWrench = Collet.wrench(innerHole, Thread.UTS._1_8, tolerance)

  objects += "chuck_wrench" -> Chuck.wrench(13, tolerance)
  //objects += "chuck_blocker" -> chuckBlocker
  objects += "chuck_inner" -> chuckInner
  objects += "collet_inner" -> colletInner
  objects += "collet_wrench" -> colletWrench

}

