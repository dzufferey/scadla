package scadla.utils
  
import scadla._

//try to emulate a Minkowski difference
//works only for objects that are not too concave

object Smaller {

  def apply(obj: Solid, s: Double) = {
    val epsilon = 1e-6
    val larger = Minkowski(obj, CenteredCube(epsilon, epsilon, epsilon))
    val shell = Difference(larger, obj)
    val biggerShell = Minkowski(shell, CenteredCube(s, s, s))
    Difference(obj, biggerShell)
  }

}

object SmallerS {

  def apply(obj: Solid, s: Double) = {
    val epsilon = 1e-6
    val larger = Minkowski(obj, Sphere(epsilon))
    val shell = Difference(larger, obj)
    val biggerShell = Minkowski(shell, Sphere(s))
    Difference(obj, biggerShell)
  }

}
