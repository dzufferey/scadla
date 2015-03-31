package dzufferey.scadla

object InlineOps {

  implicit class Ops(lhs: Solid) {

    def translate(x: Double, y: Double, z: Double) = Translate(x, y, z, lhs)
    def move(x: Double, y: Double, z: Double) = Translate(x, y, z, lhs)
    def moveX(x: Double) = Translate(x, 0, 0, lhs)
    def moveY(y: Double) = Translate(0, y, 0, lhs)
    def moveZ(z: Double) = Translate(0, 0, z, lhs)

    def rotate(x: Double, y: Double, z: Double) = Rotate(x, y, z, lhs)
    def rotateX(x: Double) = Rotate(x, 0, 0, lhs)
    def rotateY(y: Double) = Rotate(0, y, 0, lhs)
    def rotateZ(z: Double) = Rotate(0, 0, z, lhs)

    def scale(x: Double, y: Double, z: Double) = Scale(x, y, z, lhs)
    def scaleX(x: Double) = Scale(x, 1, 1, lhs)
    def scaleY(y: Double) = Scale(1, y, 1, lhs)
    def scaleZ(z: Double) = Scale(1, 1, z, lhs)

    def mirror(x: Double, y: Double, z: Double) = Mirror(x, y, z, lhs)
    def multiply(m: Matrix) = Multiply(m, lhs)

    def +(rhs: Solid) = Union(lhs, rhs)
    def ++(rhs: Iterable[Solid]) = Union((lhs :: rhs.toList): _*)
    def union(rhs: Solid) = Union(lhs, rhs)

    def *(rhs: Solid) = Intersection(lhs, rhs)
    def **(rhs: Iterable[Solid]) = Intersection((lhs :: rhs.toList): _*)
    def intersection(rhs: Solid) = Intersection(lhs, rhs)

    def -(rhs: Solid) = Difference(lhs, rhs)
    def --(rhs: Iterable[Solid]) = Difference(lhs, rhs.toList: _*)
    def difference(rhs: Solid) = Difference(lhs, rhs)

    def hull(rhs: Solid) = Hull(lhs, rhs)
    def minkowski(rhs: Solid) = Minkowski(lhs, rhs)

    def toPolyhedron = backends.OpenSCAD.getResult(lhs)

  }

}
