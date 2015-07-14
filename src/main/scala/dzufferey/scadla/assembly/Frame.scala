package dzufferey.scadla.assembly

import dzufferey.scadla._



class Frame(val translation: Vector,
            val orientation: Quaternion) {

  def compose(f: Frame): Frame = {
    val t = translation + orientation.rotate(f.translation)
    val o = orientation * f.orientation //TODO is that the right order
    new Frame(t, o)
  }

  def inverse: Frame = {
    val o = orientation.inverse
    val t = o.rotate(translation)
    new Frame(t, o) //TODO does that make sense ?
  }

  def toRefence(s: Solid): Solid = { 
    Rotate(orientation, Translate(translation, s))
  }

  def fromReference(s: Solid): Solid = { 
    Translate(translation * -1, Rotate(orientation.inverse, s))
  }

  def directTo(p: Point): Point = ((p.toVector + translation).rotateBy(orientation)).toPoint

  def directTo(p: Polyhedron): Polyhedron = {
    Polyhedron(p.faces.map{ case Face(p1, p2, p3) =>
      Face(directTo(p1), directTo(p2), directTo(p3))
    })
  }

  def directFrom(p: Polyhedron): Polyhedron = inverse.directTo(p)

}

object Frame {
  def apply(t: Vector, q: Quaternion) = new Frame(t, q)
  def apply(t: Vector) = new Frame(t, Quaternion(1,0,0,0))
  def apply(q: Quaternion) = new Frame(Vector(0,0,0), q)
  def apply() = new Frame(Vector(0,0,0), Quaternion(1,0,0,0))
}
