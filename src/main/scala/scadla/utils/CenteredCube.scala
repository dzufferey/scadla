package scadla.utils
  
import scadla._

object CenteredCube {

  def apply(x: Double, y: Double, z:Double) = Translate(-x/2, -y/2, -z/2, Cube(x,y,z))

  def xy(x: Double, y: Double, z:Double) = Translate(-x/2, -y/2, 0, Cube(x,y,z))

  def xz(x: Double, y: Double, z:Double) = Translate(-x/2, 0, -z/2, Cube(x,y,z))

  def yz(x: Double, y: Double, z:Double) = Translate(0, -y/2, -z/2, Cube(x,y,z))

  def x(x: Double, y: Double, z:Double) = Translate(-x/2, 0, 0, Cube(x,y,z))

  def y(x: Double, y: Double, z:Double) = Translate(0, -y/2, 0, Cube(x,y,z))

  def z(x: Double, y: Double, z:Double) = Translate(0, 0, -z/2, Cube(x,y,z))

}
