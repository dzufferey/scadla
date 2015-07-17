package dzufferey.scadla.assembly

import dzufferey.scadla._

abstract class Joint(direction: Vector) {

  protected val dir = direction.unit

  //method to be defined by the subclasses
  protected def getLinearSpeed: Double
  protected def getAngularSpeed: Double

  //TODO need to know the bounding box of the two objects connected by this joint to scale appropriately

  def expandAt(expansion: Double, time: Double): Frame = {
    val effectiveT = timeModifiers.foldRight(time)( (fct, acc) => fct(acc) )
    val r = Quaternion.mkRotation(getAngularSpeed * effectiveT, dir)
    val l = dir * (getLinearSpeed * effectiveT + expansion)
    Frame(l, r)
  }
  
  def expandAt(expansion: Double, time: Double, s: Solid): Solid = {
    val effectiveT = timeModifiers.foldRight(time)( (fct, acc) => fct(acc) )
    val r = Quaternion.mkRotation(getAngularSpeed * effectiveT, dir)
    val l = dir * (getLinearSpeed * effectiveT + expansion)
    Translate(l, Rotate(r, s))
  }

  def at(t: Double): Frame = expandAt(0, t)

  def at(t: Double, s: Solid): Solid = expandAt(0, t, s)
  
  protected var timeModifiers = List[Double => Double]()

  def addTimeModifier(fct: Double => Double) {
    timeModifiers ::= fct
  }

  def clearTimeModifier {
    timeModifiers = Nil
  }

  def expand(t: Double): Frame = expandAt(t, 0)

  def expand(t: Double, s: Solid): Solid = expandAt(t, 0, s)

}

class FixedJoint(direction: Vector) extends Joint(direction) {
  protected def getLinearSpeed: Double = 0
  protected def getAngularSpeed: Double = 0
}

class RevoluteJoint(direction: Vector, var angularSpeed: Double = 1.0) extends Joint(direction) {
  protected def getLinearSpeed: Double = 0
  protected def getAngularSpeed: Double = angularSpeed
}

class PrismaticJoint(direction: Vector, var linearSpeed: Double = 1.0) extends Joint(direction) {
  protected def getLinearSpeed: Double = linearSpeed
  protected def getAngularSpeed: Double = 0
}

class ScrewJoint(direction: Vector, var linearSpeed: Double = 1.0, var angularSpeed: Double = 1.0) extends Joint(direction) {
  protected def getLinearSpeed: Double = linearSpeed
  protected def getAngularSpeed: Double = angularSpeed
}
