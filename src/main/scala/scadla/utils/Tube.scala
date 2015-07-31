package scadla.utils
  
import scadla._

object Tube {

  def apply(outerRadius: Double, innerRadius: Double, height: Double) = {
    Difference(
      Cylinder(outerRadius, height),
      Translate( 0, 0, -1, Cylinder(innerRadius, height + 2))
    )
  }

}
