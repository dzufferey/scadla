package scadla.assembly

import scadla._

case class Joint(direction: Vector,
                 linearSpeed: Double,
                 angularSpeed: Double) {

  //TODO need to know the bounding box of the two objects connected by this joint to scale appropriately

  //TODO override that for putting bounds on the rotation, e.g., hinges
  def effectiveTime(time: Double) = time

  def expandAt(expansion: Double, time: Double): Frame = {
    val effectiveT = effectiveTime(time)
    val r = Quaternion.mkRotation(angularSpeed * effectiveT, direction)
    val l = direction * (linearSpeed * effectiveT + expansion)
    Frame(l, r)
  }
  
  def expandAt(expansion: Double, time: Double, s: Solid): Solid = {
    val effectiveT = effectiveTime(time)
    val r = Quaternion.mkRotation(angularSpeed * effectiveT, direction)
    val l = direction * (linearSpeed * effectiveT + expansion)
    Translate(l, Rotate(r, s))
  }

  def at(t: Double): Frame = expandAt(0, t)

  def at(t: Double, s: Solid): Solid = expandAt(0, t, s)
  
  def expand(t: Double): Frame = expandAt(t, 0)

  def expand(t: Double, s: Solid): Solid = expandAt(t, 0, s)

}

object Joint {
  
  def fixed(direction: Vector): Joint = new Joint(direction, 0, 0)
  def fixed(x:Double, y: Double, z: Double): Joint = fixed(Vector(x,y,z))
  
  def revolute(direction: Vector): Joint = new Joint(direction, 0, 1)
  def revolute(direction: Vector, angularSpeed: Double): Joint = new Joint(direction, 0, angularSpeed)
  def revolute(x:Double, y: Double, z: Double, angularSpeed: Double = 1.0): Joint = revolute(Vector(x,y,z), angularSpeed)

  def prismatic(direction: Vector): Joint = new Joint(direction, 1, 0)
  def prismatic(direction: Vector, linearSpeed: Double): Joint = new Joint(direction, linearSpeed, 0)
  def prismatic(x:Double, y: Double, z: Double, linearSpeed: Double = 1.0): Joint = prismatic(Vector(x,y,z), linearSpeed)

  def screw(direction: Vector, linearSpeed: Double, angularSpeed: Double): Joint = new Joint(direction, linearSpeed, angularSpeed)
  def screw(x:Double, y: Double, z: Double, linearSpeed: Double = 1.0, angularSpeed: Double = 1.0): Joint = screw(Vector(x,y,z), linearSpeed, angularSpeed)

}
