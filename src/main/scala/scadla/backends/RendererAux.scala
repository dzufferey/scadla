package scadla.backends

import scadla._
import squants.space.Length
import squants.space.Angle

trait RendererAux[A] extends Renderer {

  def empty: A

  def union(objs: Seq[A]): A
  def intersection(objs: Seq[A]): A
  def difference(pos: A, negs: Seq[A]): A
  def minkowski(objs: Seq[A]): A
  def hull(objs: Seq[A]): A

  def polyhedron(p: Polyhedron): A
  def cube(width: Length, depth: Length, height: Length): A
  def sphere(radius: Length): A
  def cylinder(radiusBot: Length, radiusTop: Length, height: Length): A
  def fromFile(path: String, format: String): A

  def multiply(m: Matrix, obj: A): A

  def scale(x: Double, y: Double, z: Double, obj: A): A = {
    val m = Matrix.scale(x,y,z)
    multiply(m, obj)
  }
  def rotate(x: Angle, y: Angle, z: Angle, obj: A): A = {
    val m = Matrix.rotation(x,y,z)
    multiply(m, obj)
  }
  def translate(x: Length, y: Length, z: Length, obj: A): A = {
    val m = Matrix.translation(x,y,z)
    multiply(m, obj)
  }
  def mirror(x: Double, y: Double, z: Double, obj: A): A = {
    val m = Matrix.mirror(x,y,z)
    multiply(m, obj)
  }

  def render(s: Solid): A = s match {
    case Empty => empty
    case Cube(width, depth, height) => cube(width, depth, height)
    case Sphere(radius) => sphere(radius)
    case Cylinder(radiusBot, radiusTop, height) => cylinder(radiusBot, radiusTop, height)
    case p @ Polyhedron(_) => polyhedron(p)
    case FromFile(path, format) => fromFile(path, format)
    case Union(objs @ _*) => union(objs.map(render))
    case Intersection(objs @ _*) => intersection(objs.map(render))
    case Difference(pos, negs @ _*) => difference(render(pos), negs.map(render))
    case Minkowski(objs @ _*) => minkowski(objs.map(render))
    case Hull(objs @ _*) => hull(objs.map(render))
    case Scale(x, y, z, obj) => scale(x, y, z, render(obj))
    case Rotate(x, y, z, obj) => rotate(x, y, z, render(obj))
    case Translate(x, y, z, obj) => translate(x, y, z, render(obj))
    case Mirror(x, y, z, obj) => mirror(x, y, z, render(obj))
    case Multiply(m, obj) => multiply(m, render(obj))
  }

  def toMesh(aux: A): Polyhedron

  def apply(s: Solid): Polyhedron = {
    val a = render(s)
    toMesh(a)
  }

}

class RendererAuxAdapter(r: Renderer) extends RendererAux[Polyhedron] {
  
  def empty: Polyhedron = r(Empty)

  def union(objs: Seq[Polyhedron]): Polyhedron = r(Union(objs:_*))
  def intersection(objs: Seq[Polyhedron]): Polyhedron = r(Intersection(objs:_*))
  def difference(pos: Polyhedron, negs: Seq[Polyhedron]): Polyhedron = r(Difference(pos, negs:_*))
  def minkowski(objs: Seq[Polyhedron]): Polyhedron = r(Minkowski(objs:_*))
  def hull(objs: Seq[Polyhedron]): Polyhedron = r(Hull(objs:_*))

  def polyhedron(p: Polyhedron): Polyhedron = p
  def cube(width: Length, depth: Length, height: Length): Polyhedron = r(Cube(width, depth, height))
  def sphere(radius: Length): Polyhedron = r(Sphere(radius))
  def cylinder(radiusBot: Length, radiusTop: Length, height: Length): Polyhedron = r(Cylinder(radiusBot, radiusTop, height))
  def fromFile(path: String, format: String): Polyhedron = r(FromFile(path,format))

  def multiply(m: Matrix, obj: Polyhedron): Polyhedron = r(Multiply(m, obj))
  
  def toMesh(aux: Polyhedron): Polyhedron = aux
}
