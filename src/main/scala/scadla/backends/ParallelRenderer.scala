package scadla.backends

import scadla._
import scadla.utils.simplify
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinTask

/** not quite stable yet
 *  A backend that decompose renders independent parts of a Solid in parallel.
 * (problem in parsing and feeding complex objects to openscad)
 * @param renderer the (serial) renderer to use for the simpler tasks
 */
class ParallelRenderer(renderer: Renderer) extends Renderer {
  
  //TODO optional preprocessing to make reduction tree or flatten

  def apply(obj: Solid): Polyhedron = {
    val taskMap = new ConcurrentHashMap[Solid, ForkJoinTask[Polyhedron]]

    def makeTask(ref: Solid, s: Solid) = {
      val t = new ForkJoinTask[Polyhedron]{
        protected var res: Polyhedron = null
        protected def exec = { res = renderer(s); true }
        protected def setRawResult(p: Polyhedron) { res = p }
        protected def getRawResult = res
      }
      t.fork
      val t2 = taskMap.putIfAbsent(ref, t)
      if (t2 != null) {
        t.tryUnfork
        t2
      } else {
        t
      }
    }

    def process(s: Solid): Solid = {
      if (taskMap containsKey s) {
        taskMap.get(s).join
      } else s match {
        case Translate(x, y, z, s2) =>  Translate(x, y, z, process(s2))
        case Rotate(x, y, z, s2) =>     Rotate(x, y, z, process(s2))
        case Scale(x, y, z, s2) =>      Scale(x, y, z, process(s2))
        case Mirror(x, y, z, s2) =>     Mirror(x, y, z, process(s2))
        case Multiply(m, s2) =>         Multiply(m, process(s2))

        case Union(lst @ _*) =>
          val lst2 = lst.par.map(process).seq
          makeTask(s, Union(lst2:_*)).join
        case Intersection(lst @ _*) =>
          val lst2 = lst.par.map(process).seq
          makeTask(s, Intersection(lst2:_*)).join
        case Difference(s2, lst @ _*) =>
          val t1 = makeTask(s2, process(s2))
          val lst2 = lst.par.map(process).seq
          makeTask(s, Difference(t1.join, lst2:_*)).join
        case Minkowski(lst @ _*) =>
          val lst2 = lst.par.map(process).seq
          makeTask(s, Minkowski(lst2:_*)).join
        case Hull(lst @ _*) =>
          val lst2 = lst.par.map(process).seq
          makeTask(s, Hull(lst2:_*)).join

        case other => other
      }
    }

    val s = simplify(obj)
    val p = process(s)
    renderer(p)
  }

}
