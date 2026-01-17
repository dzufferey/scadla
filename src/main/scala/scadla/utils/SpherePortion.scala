package scadla.utils

import scadla.*
import scadla.InlineOps.*
import math.*
import squants.space.Length
import squants.space.Millimeters
import squants.space.Angle
import squants.space.Radians

/** same idea as PieSlice with with a sphere */
object SpherePortion {

  //TODO check https://en.wikipedia.org/wiki/Spherical_coordinate_system for the conventions

  def apply(outerRadius: Length, innerRadius: Length,
            inclinationStart: Angle, inclinationEnd: Angle,
            azimut: Angle) = {
    val i = if (innerRadius.value > 0) Sphere(innerRadius) else Empty
    val o1 = outerRadius + Millimeters(1)
    val carved = Difference(
      Sphere(outerRadius),
      pointyThing(o1, inclinationStart),
      Mirror(0,0,1, pointyThing(o1, Radians(Pi) - inclinationEnd)),
      i
    )
    val sliced = Intersection(
      carved,
      PieSlice(o1, Millimeters(0), azimut, 2*o1).moveZ(-o1)
    )
    sliced
  }

  def elevation(outerRadius: Length, innerRadius: Length,
                elevationStart: Angle, elevationEnd: Angle,
                azimut: Angle) = {
    apply(outerRadius, innerRadius, Radians(Pi/2) - elevationStart, Radians(Pi/2) - elevationEnd, azimut)
  }

  private def pointyThing(radius: Length, inclination: Angle) = {
    val c = Cylinder(radius, 2*radius)
    val t = inclination.tan
    val h = radius / t
    if (inclination <= Radians(0))           Empty
    else if (inclination <= Radians(Pi/4))   Cylinder(Millimeters(0), radius * t, radius)
    else if (inclination <  Radians(Pi/2))   Cylinder(Millimeters(0), radius, h) + c.moveZ(h)
    else if (inclination == Radians(Pi/2))   c
    else if (inclination <= Radians(3*Pi/4)) c.moveZ(h) - Cylinder(radius, Millimeters(0), -h).moveZ(h)
    else if (inclination <  Radians(Pi))     c.moveZ(-radius) - Cylinder(-h, Millimeters(0), radius).moveZ(-radius)
    else                                     c.moveZ(-radius)
  }

}
