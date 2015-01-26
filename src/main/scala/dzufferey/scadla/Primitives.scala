package dzufferey.scadla

case class Point(x: Double, y: Double, z: Double)
case class Face(p1: Point, p2: Point, p3: Point)
case class Matrix(m00: Double, m01: Double, m02: Double, m03:Double,
                  m10: Double, m11: Double, m12: Double, m13:Double,
                  m20: Double, m21: Double, m22: Double, m23:Double,
                  m30: Double, m31: Double, m32: Double, m33:Double)
