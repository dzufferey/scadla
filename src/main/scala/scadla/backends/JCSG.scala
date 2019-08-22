package scadla.backends

import eu.mihosoft.jcsg.{Cube => JCube, Sphere => JSphere, Cylinder => JCylinder, Polyhedron => JPolyhedron, _}
import eu.mihosoft.vvecmath.{Vector3d, Transform, Plane}
import scadla._
import InlineOps._
import java.util.ArrayList
import squants.space.{Length, Millimeters, LengthUnit}

//backend using: https://github.com/miho/JCSG
object JCSG extends JCSG(16, Millimeters)

class JCSG(numSlices: Int, unit: LengthUnit = Millimeters) extends Renderer(unit) {

  override def isSupported(s: Solid): Boolean = s match {
    case s: Shape => super.isSupported(s)
    case _: Multiply => false
    case t: scadla.Transform => isSupported(t.child)
    case u @ Union(_) => u.children.forall(isSupported)
    case i @ Intersection(_) => i.children.forall(isSupported)
    case d @ Difference(_,_) => d.children.forall(isSupported)
    case c @ Hull(_) => c.children.forall(isSupported)
    case m @ Minkowski(_) => m.children.forall(isSupported)
    case _ => false
  }

  protected def empty = new JPolyhedron(Array[Vector3d](), Array[Array[Integer]]()).toCSG

  protected def stupidMinkowski2(a: Polyhedron, b: Polyhedron): Polyhedron = {
    // for all face in A: move B to each point and take the hull
    val parts1 = a.faces.toSeq.map{ case Face(p1, p2, p3) =>
      Hull( b.move(p1.x, p1.y, p1.z),
            b.move(p2.x, p2.y, p2.z),
            b.move(p3.x, p3.y, p3.z))
    }
    // and then deal with internal holes (union with A translated by every point in B ?)
    val bPoints = b.faces.foldLeft(Set.empty[Point])( (acc, f) => acc + f.p1 + f.p2 + f.p3 )
    val parts2 = bPoints.toSeq.map(a.move(_))
    // then union everything and render
    val res = Union( (parts1 ++ parts2) :_*)
    apply(res)
  }

  protected def stupidMinkowski(objs: Seq[Solid]): CSG = {
    val objs2 = objs.map( apply )
    val res = objs2.reduce(stupidMinkowski2)
    to(res)
  }

  protected def to(s: Solid): CSG = {
    import scala.language.implicitConversions
    implicit def toDouble(l: Length): Double = length2Double(l)
    s match {  
    case Empty() =>                                 empty
    case Cube(width, depth, height) =>              new JCube(Vector3d.xyz(width/2, depth/2, height/2), Vector3d.xyz(width, depth, height)).toCSG()
    case Sphere(radius) =>                          new JSphere(radius, numSlices, numSlices/2).toCSG()
    case Cylinder(radiusBot, radiusTop, height) =>  new JCylinder(radiusBot, radiusTop, height, numSlices).toCSG()
    case Polyhedron(triangles) =>
      val points = triangles.foldLeft(Set[Point]())( (acc, face) => acc + face.p1 + face.p2 + face.p3 )
      val indexed = points.toSeq.zipWithIndex
      val idx: Map[Point, Int] = indexed.toMap
      val vs = Array.ofDim[Vector3d](indexed.size)
      indexed.foreach{ case (p, i) => vs(i) = Vector3d.xyz(p.x,p.y,p.z) }
      val is = Array.ofDim[Array[Integer]](triangles.size)
      triangles.zipWithIndex.foreach { case (Face(a,b,c), i) => is(i) = Array(idx(a), idx(b), idx(c)) }
      new JPolyhedron(vs, is).toCSG()
    case f @ FromFile(path, format) =>
      format match {
        case "stl" => STL.file(java.nio.file.Paths.get(path))
        case _ => to(f.load)
      }
    case Union(objs @ _*) =>            if (objs.isEmpty) empty else objs.map(to).reduce( _.union(_) )
    case Intersection(objs @ _*) =>     if (objs.isEmpty) empty else objs.map(to).reduce( _.intersect(_) )
    case Difference(pos, negs @ _*) =>  negs.map(to).foldLeft(to(pos))( _.difference(_) )
    case Minkowski(objs @ _*) =>        stupidMinkowski(objs)
    case Hull(objs @ _*) =>             to(Union(objs:_*)).hull
    case Scale(x, y, z, obj) =>         to(obj).transformed(Transform.unity().scale(x, y, z))
    case Rotate(x, y, z, obj) =>        to(obj).transformed(Transform.unity().rot(x.toDegrees, y.toDegrees, z.toDegrees))
    case Translate(x, y, z, obj) =>     to(obj).transformed(Transform.unity().translate(x, y, z))
    case Mirror(x, y, z, obj) =>        to(obj).transformed(Transform.unity().mirror(Plane.fromPointAndNormal(Vector3d.ZERO, Vector3d.xyz(x,y,z))))
    case Multiply(m, obj) =>            sys.error("JCSG does not support arbitrary matrix transform")
    }
  }
  
  protected def polyToFaces(p: Polygon): List[Face] = {
    val vs = p.vertices.toArray(Array.ofDim[Vertex](p.vertices.size))
    val pts = vs.map( v => Point(unit(v.pos.x), unit(v.pos.y), unit(v.pos.z)))
    //if more than 3 vertices if needs to be triangulated
    //assume that the vertices form a convex loop
    (0 until vs.size - 2).toList.map(i => Face(pts(0), pts(i+1), pts(i+2)) )
  }

  protected def from(c: CSG): Polyhedron = {
    val polygons = c.getPolygons.iterator
    var faces = List[Face]()
    while (polygons.hasNext) {
      faces = polyToFaces(polygons.next) ::: faces
    }
    Polyhedron(faces)
  }
  
  def apply(obj: Solid): Polyhedron = obj match {
    case p @ Polyhedron(_) => p
    case other => from(to(other))
  }

}
