package scadla.utils
  
import scadla._

object Bigger {

  def apply(obj: Solid, s: Double) = Minkowski(obj, CenteredCube(s,s,s))

}

object BiggerS {

  def apply(obj: Solid, s: Double) = Minkowski(obj, Sphere(s))


}
