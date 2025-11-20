package scadla

import scala.language.implicitConversions
import squants.space.{Angle,Length,Millimeters,Inches,Radians,Degrees}

object EverythingIsIn {
  implicit def millimeters[A](n: A)(implicit num: Numeric[A]): Length = Millimeters(n)
  implicit def inches[A](n: A)(implicit num: Numeric[A]): Length = Inches(n)
  implicit def radians[A](n: A)(implicit num: Numeric[A]): Angle = Radians(n)
  implicit def degrees[A](n: A)(implicit num: Numeric[A]): Angle = Degrees(n)
}
