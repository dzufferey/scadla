package scadla.backends.renderers

import scadla.{Matrix, Quaternion, Vector}
import scadla.backends.renderers.Renderable.RenderableForOps
import squants.{Angle, Length}

object Solids {

  case class Union(objs: RenderableForOps[_]*)
  case class Intersection(objs: RenderableForOps[_]*)
  case class Difference(pos: RenderableForOps[_], negs: RenderableForOps[_]*)

  case class Minkowski(objs: RenderableForOps[_]*)
  case class Hull(objs: RenderableForOps[_]*)

  case class Scale(x: Double, y: Double, z: Double, obj: RenderableForOps[_])
  case class Rotate(x: Angle, y: Angle, z: Angle, obj: RenderableForOps[_])
  case class Translate(x: Length, y: Length, z: Length, obj: RenderableForOps[_])

  object Translate {
    def apply(v: Vector, s: RenderableForOps[_]): Translate = Translate(v.x, v.y, v.z, s)
  }

  object Rotate {
    def apply(q: Quaternion, s: RenderableForOps[_]): Rotate = {
      // TODO make sure OpendSCAD use the same sequence of roation
      val v = q.toRollPitchYaw
      Rotate(v.x, v.y, v.z, s)
    }
  }

  case class Mirror(x: Double, y: Double, z: Double, obj: RenderableForOps[_])
  case class Multiply(m: Matrix, obj: RenderableForOps[_])
}
