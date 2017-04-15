package scadla.utils
  
import scadla._
import squants.space.Length
import scala.language.postfixOps
import squants.space.LengthConversions._

object CenteredCube {

  def apply(x: Length, y: Length, z:Length) = Translate(-x/2, -y/2, -z/2, Cube(x,y,z))

  def xy(x: Length, y: Length, z:Length) = Translate(-x/2, -y/2, 0 mm, Cube(x,y,z))

  def xz(x: Length, y: Length, z:Length) = Translate(-x/2, 0 mm, -z/2, Cube(x,y,z))

  def yz(x: Length, y: Length, z:Length) = Translate(0 mm, -y/2, -z/2, Cube(x,y,z))

  def x(x: Length, y: Length, z:Length) = Translate(-x/2, 0 mm, 0 mm, Cube(x,y,z))

  def y(x: Length, y: Length, z:Length) = Translate(0 mm, -y/2, 0 mm, Cube(x,y,z))

  def z(x: Length, y: Length, z:Length) = Translate(0 mm, 0 mm, -z/2, Cube(x,y,z))

}
