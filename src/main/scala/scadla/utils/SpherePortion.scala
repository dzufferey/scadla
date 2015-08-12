package scadla.utils
  
import scadla._
import scadla.InlineOps._
import math._

/** same idea as PieSlice with with a sphere */
object SpherePortion {

  //TODO check https://en.wikipedia.org/wiki/Spherical_coordinate_system for the conventions

  def apply(outerRadius: Double, innerRadius: Double,
            inclinationStart: Double, inclinationEnd: Double,
            azimut: Double) = {
    val i = if (innerRadius > 0) Sphere(innerRadius) else Empty
    val o1 = outerRadius + 1
    val carved = Difference(
      Sphere(outerRadius),
      pointyThing(o1, inclinationStart),
      Mirror(0,0,1, pointyThing(o1, Pi - inclinationEnd)),
      i
    )
    val sliced = Intersection(
      carved,
      PieSlice(o1, 0, azimut, 2*o1).moveZ(-o1)
    )
    sliced
  }
  
  def elevation(outerRadius: Double, innerRadius: Double,
                elevationStart: Double, elevationEnd: Double,
                azimut: Double) = {
    apply(outerRadius, innerRadius, Pi/2 - elevationStart, Pi/2 - elevationEnd, azimut)
  }

  private def pointyThing(radius: Double, inclination: Double) = {
    val c = Cylinder(radius, 2*radius)
    val t = tan(inclination)
    val h = radius / t
    if (inclination <= 0)           Empty
    else if (inclination <= Pi/4)   Cylinder(0, radius * t, radius)
    else if (inclination <  Pi/2)   Cylinder(0, radius, h) + c.moveZ(h)
    else if (inclination == Pi/2)   c
    else if (inclination <= 3*Pi/4) c.moveZ(h) - Cylinder(radius, 0, -h).moveZ(h)
    else if (inclination <  Pi)     c.moveZ(-radius) - Cylinder(-h, 0, radius).moveZ(-radius)
    else                            c.moveZ(-radius)
  }

}
