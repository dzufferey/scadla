package scadla

import scala.language.implicitConversions

object EverythingIsIn {
  implicit def millimeters[A](n: A)(implicit num: Numeric[A]) = squants.space.Millimeters(n)
  implicit def inches[A](n: A)(implicit num: Numeric[A]) = squants.space.Inches(n)
  implicit def radians[A](n: A)(implicit num: Numeric[A]) = squants.space.Radians(n)
  implicit def degrees[A](n: A)(implicit num: Numeric[A]) = squants.space.Degrees(n)
}
