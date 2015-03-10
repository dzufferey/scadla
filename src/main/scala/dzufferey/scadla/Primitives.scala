package dzufferey.scadla

case class Point(x: Double, y: Double, z: Double) {
  def to(p: Point) = Vector(p.x-x, p.y-y, p.z-z)
}

case class Face(p1: Point, p2: Point, p3: Point) {
  def normal = {
    val v1 = p1 to p2
    val v2 = p1 to p3
    v1 cross v2
  }
}

case class Matrix(m00: Double, m01: Double, m02: Double, m03:Double,
                  m10: Double, m11: Double, m12: Double, m13:Double,
                  m20: Double, m21: Double, m22: Double, m23:Double,
                  m30: Double, m31: Double, m32: Double, m33:Double)

case class Vector(x: Double, y: Double, z: Double) {
  def +(v: Vector): Vector = Vector(x+v.x, y+v.y, z+v.z)
  def -(v: Vector): Vector = Vector(x-v.x, y-v.y, z-v.z)
  def *(c: Double): Vector = Vector(c*x, c*y, c*z)
  def dot(v: Vector): Double = x*v.x + y*v.y + z*v.z
  def cross(v: Vector): Vector = Vector(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x)
}

