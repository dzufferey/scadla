package scadla.utils

import scadla._
import squants.space.Length
import scala.language.postfixOps
import squants.space.LengthConversions._

object RoundedCube {

  def apply(x: Length, y: Length, z: Length, r: Length) = {
    if (r.value > 0) {
      val d = 2*r
      assert(d < x && d < y && d < z, "RoundedCube, radius should be less than x/2, y/2, z/2.")
      val c = Translate(r, r, r, Cube(x - d, y - d, z - d))
      Minkowski(c, Sphere(r))
    } else {
      Cube(x,y,z)
    }
  }

}


object RoundedCubeH {

  def apply(x: Length, y: Length, z: Length, r: Length) = {
    if (r.value > 0) {
      val h = z/2
      val d = 2*r
      assert(d < x && d < y, "roundedCube, radius should be less than x/2, y/2.")
      val c = Translate(r, r, 0 mm, Cube(x - d, y - d, h))
      Minkowski(c, Cylinder(r, h))
    } else {
      Cube(x,y,z)
    }
  }

}
