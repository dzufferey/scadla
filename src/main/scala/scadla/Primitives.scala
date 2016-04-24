package scadla

case class Point(x: Double, y: Double, z: Double) {
  def to(p: Point) = Vector(p.x-x, p.y-y, p.z-z)
  def toVector = Vector(x, y, z)
  def toQuaternion = Quaternion(0, x, y, z)
}

case class Face(p1: Point, p2: Point, p3: Point) {
  def normal = {
    val v1 = p1 to p2
    val v2 = p1 to p3
    val n1 = v1 cross v2
    n1 / n1.norm
  }
  def flipOrientation = Face(p1, p3, p2)
}

case class Matrix(m00: Double, m01: Double, m02: Double, m03:Double,
                  m10: Double, m11: Double, m12: Double, m13:Double,
                  m20: Double, m21: Double, m22: Double, m23:Double,
                  m30: Double, m31: Double, m32: Double, m33:Double) {

  private def prod(r: Seq[Double], c: Seq[Double]): Double = {
    r(0)*c(0) + r(1)*c(1) + r(2)*c(2) + r(3)*c(3)
  }

  def row(i: Int) = i match {
    case 0 => Seq(m00, m01, m02, m03)
    case 1 => Seq(m10, m11, m12, m13)
    case 2 => Seq(m20, m21, m22, m23)
    case 3 => Seq(m30, m31, m32, m33)
    case _ => sys.error("0 ≤ " + i + " ≤ 3")
  }

  def col(i: Int) = i match {
    case 0 => Seq(m00, m10, m20, m30)
    case 1 => Seq(m01, m11, m21, m31)
    case 2 => Seq(m02, m12, m22, m32)
    case 3 => Seq(m03, m13, m23, m33)
    case _ => sys.error("0 ≤ " + i + " ≤ 3")
  }

  def *(m: Matrix): Matrix = {
    def p(r: Int, c: Int) = prod(row(r), m.col(c))
    Matrix(
      p(0, 0), p(0, 1), p(0, 2), p(0, 3),
      p(1, 0), p(1, 1), p(1, 2), p(1, 3),
      p(2, 0), p(2, 1), p(2, 2), p(2, 3),
      p(3, 0), p(3, 1), p(3, 2), p(3, 3)
    )
  }

  def *(p: Point): Point = {
    val extended = Seq(p.x, p.y, p.z, 1)
    val x = prod(row(0), extended)
    val y = prod(row(1), extended)
    val z = prod(row(2), extended)
    val w = prod(row(3), extended)
    Point(x/w, y/w, z/w)
  }

  def *(q: Quaternion): Matrix = this * q.toMatrix
}

object Matrix {
  def unit = Matrix(1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1)
  def rotation(x: Double, y: Double, z: Double) = {
    val qx = Quaternion.mkRotation(x, Vector(1,0,0))
    val qy = Quaternion.mkRotation(y, Vector(0,1,0))
    val qz = Quaternion.mkRotation(z, Vector(0,0,1))
    val q = qx * (qy * qz)
    q.toMatrix
  }

  def mirror(_x: Double, _y: Double, _z: Double) = {
    val v = Vector(_x,_y,_z).unit
    val x = v.x
    val y = v.y
    val z = v.z
    Matrix(1-2*x*x,  -2*y*x,  -2*z*x, 0,
            -2*x*y, 1-2*y*y,  -2*z*y, 0,
            -2*x*z,  -2*y*z, 1-2*z*z, 0,
                 0,       0,       0, 1)
  }

}

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
  def toPoint = Point(x, y, z)
  def rotateBy(q: Quaternion) = q.rotate(this)
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
  def toPoint = Point(i, j, k)
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

  def rotate(v: Vector): Vector = (this * v.toQuaternion * inverse).toVector
  def rotate(p: Point): Point = (this * p.toQuaternion * inverse).toPoint
}

object Quaternion {
  def mkRotation(alpha: Double, direction: Vector) = {
    val a = math.cos(alpha / 2)
    val d = direction.unit * math.sin(alpha / 2)
    Quaternion(a, d.x, d.y, d.z)
  }
}
