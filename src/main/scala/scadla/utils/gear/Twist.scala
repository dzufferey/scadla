package scadla.utils.gear

import squants.space.Angle
import squants.space.Length
import squants.space.Degrees
import squants.space.Radians
import squants.space.Millimeters

/**
 * Represents an amount of twist [helix] per certain [increment] along a path.
 */
case class Twist(angle: Angle, increment: Length) {
  def * (d: Double) = Twist(angle * d, increment)
  def / (d: Double) = Twist(angle / d, increment)
  def unary_-() = Twist(-angle, increment)
}

object Twist {
  def apply(pitch: Length): Twist = Twist(Degrees(360), pitch)
  
  def radiansPerMm(value: Double): Twist = Twist(Radians(value), Millimeters(1))
}