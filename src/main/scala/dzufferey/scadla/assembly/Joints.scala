package dzufferey.scadla.assembly

import dzufferey.scadla._

abstract class Joint(direction: Vector) {

  protected val dir = direction.unit

  //method to be defined by the subclasses
  protected def getLinearSpeed: Double
  protected def getAngularSpeed: Double

  def at(t: Double): Frame = {
    val effectiveT = timeModifiers.foldRight(t)( (fct, acc) => fct(acc) )
    val r = Quaternion.mkRotation(getAngularSpeed * t, dir)
    val l = dir * getLinearSpeed * t
    Frame(l, r)
  }

  def at(t: Double, s: Solid): Solid = {
    val effectiveT = timeModifiers.foldRight(t)( (fct, acc) => fct(acc) )
    val r = Quaternion.mkRotation(getAngularSpeed * t, dir)
    val l = dir * getLinearSpeed * t
    Translate(l, Rotate(r, s))
  }
  
  protected var timeModifiers = List[Double => Double]()

  def addTimeModifier(fct: Double => Double) {
    timeModifiers ::= fct
  }

  def clearTimeModifier {
    timeModifiers = Nil
  }

  //TODO need to know the bounding box of the two objects connected by this joint to scale appropriately

  def expand(t: Double): Frame = {
    Frame(dir * t)
  }

  def expand(t: Double, s: Solid): Solid = {
    val l = dir * t
    Translate(l, s)
  }

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
