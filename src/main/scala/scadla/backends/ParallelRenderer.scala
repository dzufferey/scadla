package scadla.backends

import scadla._
import squants.space.Length
import squants.space.Angle
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinTask

/** not quite stable yet
 *  A backend that decompose renders independent parts of a Solid in parallel.
 * (problem in parsing and feeding complex objects to openscad)
 * @param renderer the (serial) renderer to use for the simpler tasks
 */
class ParallelRendererAux[A >: Null](renderer: RendererAux[A]) extends RendererAux[ForkJoinTask[A]] {
  
  //TODO optional preprocessing to make reduction tree or flatten
    
  protected val taskMap = new ConcurrentHashMap[Solid, ForkJoinTask[A]]

  def clear() = taskMap.clear()
    
  def empty = new ForkJoinTask[A]{
      protected var res: A = null
      protected def exec = {
        res = renderer.empty
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }

  def union(objs: Seq[ForkJoinTask[A]]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.union(objs.map(_.join))
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  def intersection(objs: Seq[ForkJoinTask[A]]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.intersection(objs.map(_.join))
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  def difference(pos: ForkJoinTask[A], negs: Seq[ForkJoinTask[A]]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.difference(pos.join, negs.map(_.join))
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  def minkowski(objs: Seq[ForkJoinTask[A]]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.minkowski(objs.map(_.join))
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  def hull(objs: Seq[ForkJoinTask[A]]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.hull(objs.map(_.join))
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }

  def polyhedron(p: Polyhedron) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.polyhedron(p)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  def cube(width: Length, depth: Length, height: Length) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.cube(width, depth, height)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  def sphere(radius: Length) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.sphere(radius)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  def cylinder(radiusBot: Length, radiusTop: Length, height: Length) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.cylinder(radiusBot, radiusTop, height)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  def fromFile(path: String, format: String) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.fromFile(path, format)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }

  def multiply(m: Matrix, obj: ForkJoinTask[A]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.multiply(m, obj.join)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }

  override def scale(x: Double, y: Double, z: Double, obj: ForkJoinTask[A]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.scale(x, y, z, obj.join)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  override def rotate(x: Angle, y: Angle, z: Angle, obj: ForkJoinTask[A]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.rotate(x, y, z, obj.join)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  override def translate(x: Length, y: Length, z: Length, obj: ForkJoinTask[A]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.translate(x, y, z, obj.join)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }
  override def mirror(x: Double, y: Double, z: Double, obj: ForkJoinTask[A]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.mirror(x, y, z, obj.join)
        true
      }
      protected def setRawResult(p: A) { res = p }
      protected def getRawResult = res
    }

  def toMesh(t: ForkJoinTask[A]) = renderer.toMesh(t.join)

  override def render(s: Solid): ForkJoinTask[A] = {
    var task = taskMap.get(s) 
    if (task == null) {
      task = super.render(s)
      val t2 = taskMap.putIfAbsent(s, task)
      if (t2 != null) {
        task = t2
      } else {
        task.fork
      }
    }
    task
  }

}

class ParallelRenderer(renderer: Renderer) extends ParallelRendererAux[Polyhedron](new RendererAuxAdapter(renderer))
