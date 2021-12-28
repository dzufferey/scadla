package scadla.backends

import scadla._
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinTask
import squants.space.{Length, Millimeters, LengthUnit}

/** not quite stable yet
 *  A backend that decompose renders independent parts of a Solid in parallel.
 * (problem in parsing and feeding complex objects to openscad)
 * @param renderer the (serial) renderer to use for the simpler tasks
 */
class ParallelRendererAux[A >: Null](renderer: RendererAux[A], unit: LengthUnit = Millimeters) extends RendererAux[ForkJoinTask[A]](unit) {

  //TODO optional preprocessing to make reduction tree or flatten

  protected val taskMap = new ConcurrentHashMap[Solid, ForkJoinTask[A]]

  override def isSupported(s: Solid) = renderer.isSupported(s)

  def clear() = taskMap.clear()

  def shape(s: Shape) = new ForkJoinTask[A]{
      protected var res: A = null
      protected def exec = {
        res = renderer.shape(s)
        true
      }
      protected def setRawResult(p: A): Unit = { res = p }
      def getRawResult = res
    }

  def operation(o: Operation, args: Seq[ForkJoinTask[A]]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.operation(o, args.map(_.join))
        true
      }
      protected def setRawResult(p: A): Unit = { res = p }
      def getRawResult = res
    }

  def transform(t: Transform, arg: ForkJoinTask[A]) = new ForkJoinTask[A] {
      protected var res: A = null
      protected def exec = {
        res = renderer.transform(t, arg.join)
        true
      }
      protected def setRawResult(p: A): Unit = { res = p }
      def getRawResult = res
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
