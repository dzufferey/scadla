package scadla.backends.renderers
import scadla.Solid

object BackwardCompatHelper {

  trait Solidable[A] {
    def toSolid(a: A): scadla.Solid
  }

  implicit def solid2Solid[A <: Solid]: Solidable[A] = new Solidable[A] {
    override def toSolid(a: A): Solid = a
  }

  implicit val union2Solid: Solidable[Solids.Union] = new Solidable[Solids.Union] {
    override def toSolid(a: Solids.Union): Solid = {
      scadla.Union(a.objs.map(_.toSolid) :_*)
    }
  }
  implicit val intersection2Solid: Solidable[Solids.Intersection] = new Solidable[Solids.Intersection] {
    override def toSolid(a: Solids.Intersection): Solid = {
      scadla.Intersection(a.objs.map(_.toSolid):_*)
    }
  }
  implicit val difference2Solid: Solidable[Solids.Difference] = new Solidable[Solids.Difference] {
    override def toSolid(a: Solids.Difference): Solid = {
      scadla.Difference(a.pos.toSolid, a.negs.map(_.toSolid):_*)
    }
  }

  implicit val minkowski2Solid: Solidable[Solids.Minkowski] = new Solidable[Solids.Minkowski]{
    override def toSolid(a: Solids.Minkowski): Solid = {
      scadla.Minkowski(a.objs.map(_.toSolid):_*)
    }
  }
  implicit val hull2Solid: Solidable[Solids.Hull] = new Solidable[Solids.Hull]{
    override def toSolid(a: Solids.Hull): Solid = {
      scadla.Hull(a.objs.map(_.toSolid):_*)
    }
  }
  implicit val scale2Solid: Solidable[Solids.Scale] = new Solidable[Solids.Scale]{
    override def toSolid(a: Solids.Scale): Solid = {
      scadla.Scale(a.x, a.y, a.z, a.obj.toSolid)
    }
  }
  implicit val rotate2Solid: Solidable[Solids.Rotate] = new Solidable[Solids.Rotate]{
    override def toSolid(a: Solids.Rotate): Solid = {
      scadla.Rotate(a.x, a.y, a.z, a.obj.toSolid)
    }
  }
  implicit val translate2Solid: Solidable[Solids.Translate] = new Solidable[Solids.Translate]{
    override def toSolid(a: Solids.Translate): Solid = {
      scadla.Translate(a.x, a.y, a.z, a.obj.toSolid)
    }
  }
  implicit val mirror2Solid: Solidable[Solids.Mirror] = new Solidable[Solids.Mirror]{
    override def toSolid(a: Solids.Mirror): Solid = {
      scadla.Mirror(a.x, a.y, a.z, a.obj.toSolid)
    }
  }
  implicit val multiply2Solid: Solidable[Solids.Multiply] = new Solidable[Solids.Multiply]{
    override def toSolid(a: Solids.Multiply): Solid = {
      scadla.Multiply(a.m, a.obj.toSolid)
    }
  }

}
