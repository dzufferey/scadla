package scadla.utils

import squants.space.{Angle, Radians, Length, Millimeters}

object Trig {

  def π = Radians(math.Pi)
  def Pi = Radians(math.Pi)

  def cos(a: Angle) = math.cos(a.toRadians)

  def sin(a: Angle) = math.sin(a.toRadians)

  def tan(a: Angle) = math.tan(a.toRadians)

  def acos(d: Double) = Radians(math.acos(d))

  def asin(d: Double) = Radians(math.asin(d))

  def atan(d: Double) = Radians(math.atan(d))

  def atan2(y: Length, x: Length) = Radians(math.atan2(y.toMillimeters, x.toMillimeters))
  
  def hypot(x: Length, y: Length) = Millimeters(math.hypot(y.toMillimeters, x.toMillimeters))

}
