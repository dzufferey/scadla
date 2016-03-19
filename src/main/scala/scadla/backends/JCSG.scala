package scadla.backends

import eu.mihosoft.vrl.v3d.{Cube => JCube, Sphere => JSphere, Cylinder => JCylinder, Polyhedron => JPolyhedron, _}
import scadla._
import InlineOps._
import scala.math._
import java.util.ArrayList

//backend using: https://github.com/miho/JCSG
object JCSG extends JCSG(16)

class JCSG(numSlices: Int) extends Renderer {

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
    val parts2 = bPoints.toSeq.map{ case Point(x, y, z) => a.move(x,y,z) }
    // then union everything and render
    val res = Union( (parts1 ++ parts2) :_*)
    apply(res)
  }

  protected def stupidMinkowski(objs: Seq[Solid]): CSG = {
    val objs2 = objs.map( apply )
    val res = objs2.reduce(stupidMinkowski2)
    to(res)
  }

  protected def to(s: Solid): CSG = s match {
    case Empty =>                                   empty
    case Cube(width, depth, height) =>              new JCube(new Vector3d(width/2, depth/2, height/2), new Vector3d(width, depth, height)).toCSG()
    case Sphere(radius) =>                          new JSphere(radius, numSlices, numSlices/2).toCSG()
    case Cylinder(radiusBot, radiusTop, height) =>  new JCylinder(radiusBot, radiusTop, height, numSlices).toCSG()
    case Polyhedron(triangles) =>
      val points = triangles.foldLeft(Set[Point]())( (acc, face) => acc + face.p1 + face.p2 + face.p3 )
      val indexed = points.toSeq.zipWithIndex
      val idx: Map[Point, Int] = indexed.toMap
      val vs = Array.ofDim[Vector3d](indexed.size)
      indexed.foreach{ case (Point(x,y,z), i) => vs(i) = new Vector3d(x,y,z) }
      val is = Array.ofDim[Array[Integer]](triangles.size)
      triangles.zipWithIndex.foreach { case (Face(a,b,c), i) => is(i) = Array(idx(a), idx(b), idx(c)) }
      new JPolyhedron(vs, is).toCSG()
    case FromFile(path, format) =>
      format match {
        case "stl" => STL.file(java.nio.file.Paths.get(path))
        case "obj" => to(obj.Parser(path))
        case other => sys.error("unsupported format: " + other)
      }
    case Union(objs @ _*) =>            if (objs.isEmpty) empty else objs.map(to).reduce( _.union(_) )
    case Intersection(objs @ _*) =>     if (objs.isEmpty) empty else objs.map(to).reduce( _.intersect(_) )
    case Difference(pos, negs @ _*) =>  negs.map(to).foldLeft(to(pos))( _.difference(_) )
    case Minkowski(objs @ _*) =>        stupidMinkowski(objs)
    case Hull(objs @ _*) =>             to(Union(objs:_*)).hull
    case Scale(x, y, z, obj) =>         to(obj).transformed(new Transform().scale(x, y, z))
    case Rotate(x, y, z, obj) =>        to(obj).transformed(new Transform().rot(toDegrees(x), toDegrees(y), toDegrees(z)))
    case Translate(x, y, z, obj) =>     to(obj).transformed(new Transform().translate(x, y, z))
    case Mirror(x, y, z, obj) =>        to(obj).transformed(new Transform().mirror(new Plane(new Vector3d(x,y,z), 0)))
    case Multiply(m, obj) =>            sys.error("JCSG does not support arbitrary matrix transform")
  }
  
  protected def polyToFaces(p: Polygon): List[Face] = {
    val vs = p.vertices.toArray(Array.ofDim[Vertex](p.vertices.size))
    val pts = vs.map( v => Point(v.pos.x, v.pos.y, v.pos.z))
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
