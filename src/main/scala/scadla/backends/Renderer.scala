package scadla.backends

import scadla._
import java.io._
import squants.space.{Millimeters, Length, LengthUnit}

abstract class Renderer(unit: LengthUnit = Millimeters) {

  protected def length2Double(l: Length): Double = l to unit

  def apply(s: Solid): Polyhedron

  /** Check if all the Operations are supported.
   *  Not all backends support all the operations.
   *  By default all the transforms and the CSG operations should be supported
   */
  def isSupported(s: Solid): Boolean = s match {
    case Cube(_,_,_) | Sphere(_) | Cylinder(_, _, _) | FromFile(_, _) | Empty | Polyhedron(_) => true
    case t: Transform => isSupported(t.child)
    case u @ Union(_) => u.children.forall(isSupported)
    case i @ Intersection(_) => i.children.forall(isSupported)
    case d @ Difference(_,_) => d.children.forall(isSupported)
    case _ => false
  }

  def toSTL(s: Solid, fileName: String) {
    val p = apply(s)
    stl.Printer.storeBinary(p, fileName)
    //stl.Printer.storeText(p, fileName)
  }

  def view(s: Solid) = Viewer.default(apply(s))

}

object Renderer {

  def default: Renderer = {
    if (OpenSCADnightly.isPresent) OpenSCADnightly
    else if (OpenSCAD.isPresent) OpenSCAD
    else JCSG
  }

}
