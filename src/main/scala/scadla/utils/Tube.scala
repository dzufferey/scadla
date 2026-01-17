package scadla.utils

import scadla.*
import squants.space.Length
import scala.language.postfixOps
import squants.space.LengthConversions.*

object Tube {

  def apply(outerRadius: Length, innerRadius: Length, height: Length) = {
    Difference(
      Cylinder(outerRadius, height),
      Translate(0 mm, 0 mm, -1 mm, Cylinder(innerRadius, height + (2 mm)))
    )
  }

}
