package scadla.utils

import scadla.*
import squants.space.Length

object Bigger {

  def apply(obj: Solid, s: Length) = Minkowski(obj, CenteredCube(s,s,s))

}

object BiggerS {

  def apply(obj: Solid, s: Length) = Minkowski(obj, Sphere(s))


}
