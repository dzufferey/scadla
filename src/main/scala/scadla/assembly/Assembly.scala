package scadla.assembly

import scadla._
import scadla.backends.Renderer
import scala.language.implicitConversions
import squants.space.Length
import squants.time.Time
import squants.time.Seconds
import squants.space.Millimeters

sealed abstract class Assembly(name: String, children: List[(Frame,Joint,Assembly,Frame)]) {

  protected def checkNotInChildren(as: Set[Assembly]): Boolean = {
    val tas = as + this
    !as(this) && children.forall(_._3.checkNotInChildren(tas))
  }

  protected def addChild(where: Frame, joint: Joint, child: Assembly, whereChild: Frame): Assembly
  

  def +(where: Frame, joint: Joint, child: Assembly, whereChild: Frame): Assembly = {
    assert(child.checkNotInChildren(Set(this)))
    addChild(where, joint, child, whereChild.inverse)
  }
  
  def +(where: Vector, joint: Joint, child: Assembly, whereChild: Frame): Assembly =
    this + (Frame(where), joint, child, whereChild)

  def +(joint: Joint, child: Assembly, whereChild: Frame): Assembly =
    this + (Frame(), joint, child, whereChild)
  
  def +(where: Frame, joint: Joint, child: Assembly, whereChild: Vector): Assembly =
    this + (where, joint, child, Frame(whereChild))
  
  def +(where: Vector, joint: Joint, child: Assembly, whereChild: Vector): Assembly =
    this + (Frame(where), joint, child, Frame(whereChild))

  def +(joint: Joint, child: Assembly, whereChild: Vector): Assembly =
    this + (Frame(), joint, child, Frame(whereChild))

  def +(where: Frame, joint: Joint, child: Assembly): Assembly =
    this + (where, joint, child, Frame())

  def +(where: Vector, joint: Joint, child: Assembly): Assembly =
    this + (Frame(where), joint, child, Frame())

  def +(joint: Joint, child: Assembly): Assembly =
    this + (Frame(), joint, child, Frame())
  
  def expandAt(expansion: Length, time: Time): Seq[(Frame,Polyhedron)] = {
    children.flatMap{ case (f1, j, c, f3) =>
      val f2 = j.expandAt(expansion, time)
      val f = f1.compose(f2).compose(f3)
      val children = c.expandAt(expansion, time)
      children.map{ case (fc,p) => (f.compose(fc),p) }
    }
  }

  def at(t: Time): Seq[(Frame,Polyhedron)] = expandAt(Millimeters(0), t)

  def expand(t: Length): Seq[(Frame,Polyhedron)] = expandAt(t, Seconds(0))
  
  //TODO immutable
  def preRender(r: Renderer) {
    children.foreach( _._3.preRender(r) )
  }
    
  def parts: Set[Part] = {
    children.foldLeft(Set[Part]())( _ ++ _._3.parts )
  }

  def bom: Map[Part,Int] = {
    children.foldLeft(Map[Part,Int]())( (acc,c) => {
      c._3.bom.foldLeft(acc)( (acc, kv) => {
        val (k,v) = kv
        acc + (k -> (acc.getOrElse(k,0) + v))
      })
    })
  }

  def plate(x: Double, y: Double, gap: Double = 5): Seq[(Frame,Polyhedron)] = {
    //filter out vitamines
    val b = bom.filter(!_._1.vitamin)
    //gets all the parts to print
    val polys = b.flatMap{ case (p,n) => Seq.fill(n)(p.printable) }
    //TODO compute bounding box and place within the x-y space
    ???
  }

}

object Assembly {

  def apply(name: String) = EmptyAssembly(name, Nil)
  def apply(part: Part) = SingletonAssembly(part, Nil)

  implicit def part2Assembly(part: Part): Assembly = new SingletonAssembly(part, Nil)

  def quickRender(assembly: Seq[(Frame,Polyhedron)]) = {
    val p2 = assembly.map{ case (f,p) => f.directTo(p) }
    Polyhedron(p2.flatMap(_.faces))
  }

}

case class EmptyAssembly(name: String, children: List[(Frame,Joint,Assembly,Frame)]) extends Assembly(name, children) {
  protected def addChild(where: Frame, joint: Joint, child: Assembly, whereChild: Frame): Assembly = {
    EmptyAssembly(name, (where, joint, child, whereChild) :: children)
  }
}

case class SingletonAssembly(part: Part, children: List[(Frame,Joint,Assembly,Frame)]) extends Assembly(part.name, children) {
  
  protected def addChild(where: Frame, joint: Joint, child: Assembly, whereChild: Frame): Assembly = {
    SingletonAssembly(part, (where, joint, child, whereChild) :: children)
  }

  override def expandAt(e: Length, t: Time): Seq[(Frame,Polyhedron)] = super.expandAt(e, t) :+ (Frame() -> part.mesh)

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

