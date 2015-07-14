package dzufferey.scadla.assembly

import dzufferey.scadla._
import dzufferey.scadla.backends.Renderer
import scala.language.implicitConversions


class Assembly(name: String) {

  protected var children: List[(Connection,Joint,Connection)] = Nil

  protected def checkNotInChildren(as: Set[Assembly]): Boolean = {
    val tas = as + this
    !as(this) && children.forall(_._3.parent.checkNotInChildren(tas))
  }

  def attach(where: Connection, joint: Joint, child: Connection) {
    assert(where.parent == this)
    assert(child.parent.checkNotInChildren(Set(this)))
    children ::= (where, joint, child)
  }

  def at(t: Double): Seq[(Frame,Polyhedron)] = {
    children.flatMap{ case (c1, j, c2) =>
      val f1 = c1.frame
      val f2 = j.at(t)
      val f3 = c2.frame.inverse
      val f = f1.compose(f2).compose(f3)
      val children = c2.parent.at(t)
      children.map{ case (fc,p) => (f.compose(fc),p) }
    }
  }

  def expand(t: Double): Seq[(Frame,Polyhedron)] = {
    children.flatMap{ case (c1, j, c2) =>
      val f1 = c1.frame
      val f2 = j.expand(t)
      val f3 = c2.frame.inverse
      val f = f1.compose(f2).compose(f3)
      val children = c2.parent.expand(t)
      children.map{ case (fc,p) => (f.compose(fc),p) }
    }
  }
  
  def preRender(r: Renderer) {
    children.foreach( _._3.parent.preRender(r) )
  }
    
  def parts: Set[Part] = {
    children.foldLeft(Set[Part]())( _ ++ _._3.parent.parts )
  }

  def bom: Map[Part,Int] = {
    children.foldLeft(Map[Part,Int]())( (acc,c) => {
      c._3.parent.bom.foldLeft(acc)( (acc, kv) => {
        val (k,v) = kv
        acc + (k -> (acc.getOrElse(k,0) + v))
      })
    })
  }

  def plate(x: Double, y: Double, gap: Double = 5): Seq[(Frame,Polyhedron)] = {
    val b = bom
    //TODO filter out vitamines
    //TODO compute bounding box and place within the x-y space
    ???
  }

}

object Assembly {

  implicit def part2Assembly(part: Part): Assembly = new SingletonAssembly(part)

  def quickRender(assembly: Seq[(Frame,Polyhedron)]) = {
    val p2 = assembly.map{ case (f,p) => f.directTo(p) }
    Polyhedron(p2.flatMap(_.faces))
  }

}

class SingletonAssembly(val part: Part) extends Assembly(part.name) {

  override def at(t: Double) = super.at(t) :+ (Frame() -> part.mesh)

  override def expand(t: Double) = super.expand(t) :+ (Frame() -> part.mesh)

  override def preRender(r: Renderer) {
    super.preRender(r)
    part.preRender(r)
  }

  override def parts = super.parts + part

  override def bom = {
    val b = super.bom
    b + (part -> (b.getOrElse(part, 0) + 1))
  }

}
