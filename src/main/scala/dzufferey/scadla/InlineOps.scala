package dzufferey.scadla

object InlineOps {

  implicit class Ops(lhs: Solid) {

    def translate(x: Double, y: Double, z: Double) = Translate(x, y, z, lhs)
    def moveX(x: Double) = Translate(x, 0, 0, lhs)
    def moveY(y: Double) = Translate(0, y, 0, lhs)
    def moveZ(z: Double) = Translate(0, 0, z, lhs)

    def rotate(x: Double, y: Double, z: Double) = Rotate(x, y, z, lhs)
    def scale(x: Double, y: Double, z: Double) = Scale(x, y, z, lhs)
    def mirror(x: Double, y: Double, z: Double) = Mirror(x, y, z, lhs)
    def multiply(m: Matrix) = Multiply(m, lhs)

    def +(rhs: Solid) = Union(lhs, rhs)
    def union(rhs: Solid) = Union(lhs, rhs)

    def *(rhs: Solid) = Intersection(lhs, rhs)
    def intersection(rhs: Solid) = Intersection(lhs, rhs)

    def -(rhs: Solid) = Difference(lhs, rhs)
    def difference(rhs: Solid) = Difference(lhs, rhs)

    def hull(rhs: Solid) = Hull(lhs, rhs)
    def minkowski(rhs: Solid) = Minkowski(lhs, rhs)

  }

}
