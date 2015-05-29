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

//TODO as a class with the parameter in the ctor
object Spindle {

  var objects = Map[String, Solid]()

  ////////////////
  // parameters //
  ////////////////
  
  val motorBoltDistance = (30 + 30) / 2f //depends on the size of the motorBase and boltSupport
  
  val gearHeight = 10
  val chuckNonThreadOverlap = 10 

  val topBoltWasher = 2
  val bottomBoltWasher = 2

  val boltThreadedLength = 25 //23
  val boltNonThreadedLength = 96 //86

  val boltSupportTop = boltNonThreadedLength - gearHeight - chuckNonThreadOverlap - topBoltWasher - bottomBoltWasher

  val motorBaseToGear = 37.3
  val motorBaseHeight = boltSupportTop + topBoltWasher - motorBaseToGear 

  val bitsLength = 25
  val colletLength = bitsLength - 3
  val chuckHeight = colletLength + boltThreadedLength + chuckNonThreadOverlap
  val innerHole = 9 //17.5 / 2

  ///////////
  // gears //
  ///////////

  //TODO creating the gears already does some rendering, something moer lazy...
  val gear1 = Gear.helical( motorBoltDistance * 2 / 3.0, 32, gearHeight,-0.03, tolerance)
  val gear2 = Gear.helical( motorBoltDistance / 3.0    , 16, gearHeight, 0.06, tolerance)
  val motorKnobs = {
    val c = Cylinder(3-tolerance, 2).moveZ(gearHeight)
    val u = Union(c.moveX(9), c.moveX(-9), c.moveY(9), c.moveY(-9))
    val r = (motorBoltDistance / 3.0) * (1.0 - 2.0 / 16)
    u * Cylinder(r, 20)
  }
  val nutTop = Cylinder(Thread.ISO.M8 * 3, 14) - nut.M8.moveZ(gearHeight)
  //gears
  val gearBolt = gear1 + nutTop - Cylinder(Thread.ISO.M8 + tolerance, gearHeight)
  val gearMotor = gear2 - Cylinder(Thread.ISO.M5 + tolerance, gearHeight) + motorKnobs

  objects += "gear_bolt" -> gearBolt
  objects += "gear_motor" -> gearMotor
  objects += "bolt_washer_top" -> Tube(6, 4 + 2*tolerance, topBoltWasher)
  objects += "bolt_washer_bot" -> Tube(6, 4 + 2*tolerance, bottomBoltWasher)
  

  /////////////////////////////////
  // bolt support and motor base //
  /////////////////////////////////

  //TODO not so square ...
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

  val fixCoord = List[(Double,Double,Double)](
    (31,  4, -Pi/5.2),
    (-1,  4, Pi+Pi/5.2),
    (34, 30,  0),
    (-4, 30, Pi),
    (34, 56,  0),
    (-4, 56, Pi)
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
    val s = Cylinder(Thread.ISO.M3 + tolerance, 5)
    val fix = Cylinder(4, 4) + Cube(5, 8, 4).move(-5, -4, 0) - s
    Union(
      boltSupport.move(15, 15, 0),
      motorBase.moveY(30)
   ) ++ fixCoord.map{ case (x,y,a) => fix.rotateZ(a).move(x,y,0) }
  }

  objects += "spindle_body" -> spindle


  ///////////
  // chuck //
  ///////////


  val chuck = Chuck.innerThread(13, innerHole+tolerance, chuckHeight, colletLength, 20)
  val colletInner = Collet.threaded(innerHole+1, innerHole, Thread.UTS._1_8+tolerance, colletLength,
                                    6, 1, 2, 20, tolerance, Thread.ISO.M2)
  //val chuckBlocker = Chuck.blocker( innerHole, tolerance)

  val colletWrench = Collet.wrench(innerHole, Thread.UTS._1_8, tolerance)

  objects += "chuck_wrench" -> Chuck.wrench(13, tolerance)
  //objects += "chuck_blocker" -> chuckBlocker
  objects += "chuck" -> chuck.rotateX(Pi)
  objects += "collet_inner" -> colletInner
  objects += "collet_wrench" -> colletWrench

}

