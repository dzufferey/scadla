package scadla.backends

import scadla.*
import squants.space.{Length, Millimeters, LengthUnit}

abstract class RendererAux[A](unit: LengthUnit = Millimeters) extends Renderer(unit) {

  def shape(s: Shape): A
  def operation(o: Operation, args: Seq[A]): A
  def transform(t: Transform, arg: A): A

  def render(s: Solid): A = s match {
    case s: Shape => shape(s)
    case o: Operation => operation(o, o.children.map(render))
    case t: Transform => transform(t, render(t.child))
  }

  def toMesh(aux: A): Polyhedron

  def apply(s: Solid): Polyhedron = {
    val a = render(s)
    toMesh(a)
  }

}

class RendererAuxAdapter(r: Renderer) extends RendererAux[Polyhedron] {

  def shape(s: Shape): Polyhedron = r(s)
  def operation(o: Operation, args: Seq[Polyhedron]) = r(o.setChildren(args))
  def transform(t: Transform, arg: Polyhedron) = r(t.setChild(arg))

  def toMesh(aux: Polyhedron): Polyhedron = aux
}
