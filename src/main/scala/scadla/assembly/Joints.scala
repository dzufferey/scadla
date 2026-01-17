package scadla.assembly

import scadla.*
import squants.space.LengthUnit
import squants.motion.AngularVelocity
import squants.time.Time
import squants.motion.Velocity
import squants.space.Length
import squants.space.Millimeters
import squants.time.Seconds
import squants.motion.MetersPerSecond
import squants.motion.RadiansPerSecond

case class Joint(direction: Vector,
                 linearSpeed: Velocity,
                 angularSpeed: AngularVelocity) {

  //TODO need to know the bounding box of the two objects connected by this joint to scale appropriately

  //TODO override that for putting bounds on the rotation, e.g., hinges
  def effectiveTime(time: Time) = time

  def expandAt(expansion: Length, time: Time): Frame = {
    val effectiveT = effectiveTime(time)
    val r = Quaternion.mkRotation(angularSpeed * effectiveT, direction)
    val l = direction * (linearSpeed * effectiveT + expansion).in(direction.unit).value
    Frame(l, r)
  }
  
  def expandAt(expansion: Length, time: Time, s: Solid): Solid = {
    val effectiveT = effectiveTime(time)
    val r = Quaternion.mkRotation(angularSpeed * effectiveT, direction)
    val l = direction * (linearSpeed * effectiveT + expansion).in(direction.unit).value
    Translate(l, Rotate(r, s))
  }

  def at(t: Time): Frame = expandAt(Millimeters(0), t)

  def at(t: Time, s: Solid): Solid = expandAt(Millimeters(0), t, s)
  
  def expand(t: Length): Frame = expandAt(t, Seconds(0))

  def expand(t: Length, s: Solid): Solid = expandAt(t, Seconds(0), s)

}

object Joint {
  
  def fixed(direction: Vector): Joint = new Joint(direction, MetersPerSecond(0), RadiansPerSecond(0))
  def fixed(x:Double, y: Double, z: Double, unit: LengthUnit): Joint = fixed(Vector(x,y,z, unit))
  
  def revolute(direction: Vector): Joint = new Joint(direction, MetersPerSecond(0), RadiansPerSecond(1))
  def revolute(direction: Vector, angularSpeed: AngularVelocity): Joint = new Joint(direction, MetersPerSecond(0), angularSpeed)
  def revolute(x:Double, y: Double, z: Double, unit: LengthUnit, angularSpeed: AngularVelocity = RadiansPerSecond(1)): Joint = revolute(Vector(x,y,z,unit), angularSpeed)

  def prismatic(direction: Vector): Joint = new Joint(direction, MetersPerSecond(1), RadiansPerSecond(0))
  def prismatic(direction: Vector, linearSpeed: Velocity): Joint = new Joint(direction, linearSpeed, RadiansPerSecond(0))
  def prismatic(x:Double, y: Double, z: Double, unit: LengthUnit, linearSpeed: Velocity = MetersPerSecond(0.001)): Joint = prismatic(Vector(x,y,z,unit), linearSpeed)

  def screw(direction: Vector, linearSpeed: Velocity, angularSpeed: AngularVelocity): Joint = new Joint(direction, linearSpeed, angularSpeed)
  def screw(x:Double, y: Double, z: Double, unit: LengthUnit, linearSpeed: Velocity = MetersPerSecond(0.001), angularSpeed: AngularVelocity = RadiansPerSecond(1)): Joint = screw(Vector(x,y,z,unit), linearSpeed, angularSpeed)

}
