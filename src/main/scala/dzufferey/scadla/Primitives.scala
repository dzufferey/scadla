package dzufferey.scadla

case class Point(x: Double, y: Double, z: Double) {
  def to(p: Point) = Vector(p.x-x, p.y-y, p.z-z)
}

case class Face(p1: Point, p2: Point, p3: Point) {
  def normal = {
    val v1 = p1 to p2
    val v2 = p1 to p3
    val n1 = v1 cross v2
    n1 / n1.norm
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
  def /(c: Double): Vector = Vector(x/c, y/c, z/c)
  def dot(v: Vector): Double = x*v.x + y*v.y + z*v.z
  def cross(v: Vector): Vector = Vector(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x)
  def norm: Double = math.sqrt(x*x + y*y + z*z)
  def unit = this / norm
  def toQuaternion = Quaternion(0, x, y, z)
  def rotate(q: Quaternion) = (q * toQuaternion * q.inverse).toVector
}

case class Quaternion(a: Double, i: Double, j: Double, k: Double) {
  /** Hammilton product */
  def *(q: Quaternion) = Quaternion(a*q.a - i*q.i - j*q.j - k*q.k,
                                    a*q.i + i*q.a + j*q.k - k*q.j,
                                    a*q.j - i*q.k + j*q.a + k*q.i,
                                    a*q.k + i*q.j - j*q.i + k*q.a)
  def inverse = Quaternion(a, -i, -j, -k)
  def norm: Double = math.sqrt(a*a + i*i + j*j + k*k)
  def unit = {
    val n = norm
    Quaternion(a/n, i/n, j/n, k/n)
  }
  def toVector = Vector(i, j, k)
  def toMatrix = Matrix(1 - 2*(j*j + k*k),     2*(i*j - k*a),     2*(i*k + j*a), 0,
                            2*(i*j + k*a), 1 - 2*(i*i + k*k),     2*(j*k - i*a), 0,
                            2*(i*k - j*a),     2*(j*k + i*a), 1 - 2*(i*i + j*j), 0,
                                        0,                 0,                 0, 1)
  /** Get the axis of rotation for an unit quaternion */
  def getDirection = {
    if (i == 0.0 && j == 0.0 && k == 0.0) Vector(1, 0, 0)
    else toVector.unit
  }
  /** Get the angle of rotation for an unit quaternion */
  def getAngle = 2 * math.acos(a)
  
  // https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
  def toRollPitchYaw = Vector(
    math.atan2(2 * (i*a + j*k), 1 - 2 * (i*i + j*j)),
    math.asin(2 * (a*k - i*k)),
    math.atan2(2 * (a*k + i*j), 1 - 2 * (j*j + k*k))
  )
}

object Quaternion {
  def mkRotation(alpha: Double, direction: Vector) = {
    val a = math.cos(alpha / 2)
    val d = direction.unit * math.sin(alpha / 2)
    Quaternion(a, d.x, d.y, d.z)
  }
}
