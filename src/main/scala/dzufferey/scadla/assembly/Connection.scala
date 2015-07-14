package dzufferey.scadla.assembly

import dzufferey.scadla._

class Connection(val parent: Assembly,
                 val frame: Frame) {

  def toRefence(s: Solid): Solid = frame.toRefence(s)

  def fromReference(s: Solid): Solid = frame.fromReference(s)
  
  def attach(joint: Joint, child: Connection) {
    parent.attach(this, joint, child)
  }

}

object Connection {
  def apply(p: Assembly, f: Frame): Connection = new Connection(p, f)
  def apply(p: Assembly, t: Vector, q: Quaternion): Connection = apply(p, Frame(t, q))
  def apply(p: Assembly, t: Vector): Connection = apply(p, t, Quaternion(1,0,0,0))
  def apply(p: Assembly, q: Quaternion): Connection = apply(p, Vector(0,0,0), q)
  def apply(p: Assembly): Connection = apply(p, Vector(0,0,0), Quaternion(1,0,0,0))
}
