package scadla.utils
  
import scadla._
import scadla.backends.renderers.Renderable
import squants.space.Length

import scala.language.postfixOps

object Tube {
  import squants.space.LengthConversions._
  import backends.renderers.Renderable._
  import backends.renderers.Solids._

  def apply(outerRadius: Length, innerRadius: Length, height: Length)(implicit ev1: Renderable[Cylinder], ev2: Renderable[Translate], ev3: Renderable[Difference]) = {
    Difference(
      Cylinder(outerRadius, height),
      Translate(0 mm, 0 mm, -1 mm, Cylinder(innerRadius, height + (2 mm)))
    )
  }

}
