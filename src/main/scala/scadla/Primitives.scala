package scadla

import squants.space.Length
import squants.space.Area
import squants.space.Millimeters
import squants.space.LengthUnit
import squants.space.AngleUnit
import squants.space.Radians
import squants.space.Angle

case class Point(x: Length, y: Length, z: Length) {
  private def unit = x.unit
  def to(p: Point) = Vector(
      (p.x - x).in(unit).value, 
      (p.y - y).in(unit).value, 
      (p.z - z).in(unit).value, unit)
  def toVector = Vector(x.in(unit).value, y.in(unit).value, z.in(unit).value, unit)
  def toQuaternion = Quaternion(0, x.in(unit).value, y.in(unit).value, z.in(unit).value, unit)
}

case class Face(p1: Point, p2: Point, p3: Point) {
  def normal = {
    val v1 = p1 to p2
    val v2 = p1 to p3
    val n1 = v1 cross v2
    n1.toUnitVector
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
    val extended = Seq(p.x.toMillimeters, p.y.toMillimeters, p.z.toMillimeters, 1)
    val x = prod(row(0), extended)
    val y = prod(row(1), extended)
    val z = prod(row(2), extended)
    val w = prod(row(3), extended)
    Point(Millimeters(x/w), Millimeters(y/w), Millimeters(z/w))
  }

  def *(q: Quaternion): Matrix = this * q.toMatrix
}

object Matrix {
  def unit = Matrix(1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1)

  def translation(x: Length, y: Length, z: Length) = {
    Matrix(1, 0, 0, x.toMillimeters,
           0, 1, 0, y.toMillimeters,
           0, 0, 1, z.toMillimeters,
           0, 0, 0, 1)
  }

  def rotation(x: Angle, y: Angle, z: Angle) = {
    val qx = Quaternion.mkRotation(x, Vector.x)
    val qy = Quaternion.mkRotation(y, Vector.y)
    val qz = Quaternion.mkRotation(z, Vector.z)
    val q = qx * (qy * qz)
    q.toMatrix
  }

  def mirror(_x: Double, _y: Double, _z: Double) = {
    val v = Vector(_x,_y,_z,Millimeters).toUnitVector
    val x = v.x.toMillimeters
    val y = v.y.toMillimeters
    val z = v.z.toMillimeters
    Matrix(1-2*x*x,  -2*y*x,  -2*z*x, 0,
            -2*x*y, 1-2*y*y,  -2*z*y, 0,
            -2*x*z,  -2*y*z, 1-2*z*z, 0,
                 0,       0,       0, 1)
  }

  def scale(x: Double, y: Double, z: Double) = {
    Matrix(x, 0, 0, 0,
           0, y, 0, 0,
           0, 0, z, 0,
           0, 0, 0, 1)
  }

}

case class Vector(private val _x: Double, private val _y: Double, private val _z: Double, unit: LengthUnit) {
  def x = unit(_x)
  def y = unit(_y)
  def z = unit(_z)
  def +(v: Vector): Vector = Vector(_x+v._x, _y+v._y, _z+v._z, unit)
  def -(v: Vector): Vector = Vector(_x-v._x, _y-v._y, _z-v._z, unit)
  def *(c: Double): Vector = Vector(_x*c, _y*c, _z*c, unit)
  def /(c: Double): Vector = Vector(_x/c, _y/c, _z/c, unit)
  def dot(v: Vector): Area = x*v.x + y*v.y + z*v.z
  def cross(v: Vector) = Vector(_y*v._z - _z*v._y, _z*v._x - _x*v._z, _x*v._y - _y*v._x, unit)
  private def _norm: Double = Math.sqrt(_x*_x + _y*_y + _z*_z)
  def norm: Length = unit(_norm)
  def toUnitVector: Vector = this / _norm
  def toQuaternion = Quaternion(0, _x, _y, _z, unit)
  def toQuaternion(real: Double) = Quaternion(real, _x, _y, _z, unit)
  def toPoint = Point(x, y, z)
  def rotateBy(q: Quaternion) = q.rotate(this)
}

object Vector {
  /** A 1mm vector pointing in the positive X direction */
  def x = new Vector(1, 0, 0, Millimeters)
  /** A 1mm vector pointing in the positive Y direction */
  def y = new Vector(0, 1, 0, Millimeters)
  /** A 1mm vector pointing in the positive Z direction */
  def z = new Vector(0, 0, 1, Millimeters)
}

case class Quaternion(a: Double, i: Double, j: Double, k: Double, unit: LengthUnit) {
  /** Hammilton product */
  def *(q: Quaternion) = Quaternion(a*q.a - i*q.i - j*q.j - k*q.k,
                                    a*q.i + i*q.a + j*q.k - k*q.j,
                                    a*q.j - i*q.k + j*q.a + k*q.i,
                                    a*q.k + i*q.j - j*q.i + k*q.a, unit)
  def inverse = Quaternion(a, -i, -j, -k, unit)
  private def _norm: Double = math.sqrt(a*a + i*i + j*j + k*k) 
  def norm: Length = unit(_norm) 
  def toUnitQuaternion = {
    val n = _norm
    Quaternion(a/n, i/n, j/n, k/n, unit)
  }
  def toVector = Vector(i, j, k, unit)
  def toPoint = Point(unit(i), unit(j), unit(k))
  def toMatrix = Matrix(1 - 2*(j*j + k*k),     2*(i*j - k*a),     2*(i*k + j*a), 0,
                            2*(i*j + k*a), 1 - 2*(i*i + k*k),     2*(j*k - i*a), 0,
                            2*(i*k - j*a),     2*(j*k + i*a), 1 - 2*(i*i + j*j), 0,
                                        0,                 0,                 0, 1)
  /** Get the axis of rotation for an unit quaternion */
  def getDirection: Vector = {
    if (i == 0.0 && j == 0.0 && k == 0.0) Vector.x
    else toVector.toUnitVector
  }
  /** Get the angle of rotation for an unit quaternion */
  def getAngle = 2 * math.acos(a)
  
  // https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
  def toRollPitchYaw = RollPitchYaw(
    math.atan2(2 * (i*a + j*k), 1 - 2 * (i*i + j*j)),
    math.asin(2 * (a*k - i*k)),
    math.atan2(2 * (a*k + i*j), 1 - 2 * (j*j + k*k)),
    Radians
  )

  def rotate(v: Vector): Vector = (this * v.toQuaternion * inverse).toVector
  def rotate(p: Point): Point = (this * p.toQuaternion * inverse).toPoint
}

case class RollPitchYaw(_roll: Double, _pitch: Double, _yaw: Double, unit: AngleUnit) {
  def roll = unit(_roll)
  def x = roll
  def pitch = unit(_pitch)
  def y = pitch
  def yaw = unit(_yaw)
  def z = yaw
}

object Quaternion {
  def mkRotation(alpha: Angle, direction: Vector) = {
    val a = (alpha / 2).cos
    val d = direction.toUnitVector * (alpha / 2).sin
    d.toQuaternion(a)
  }
}
