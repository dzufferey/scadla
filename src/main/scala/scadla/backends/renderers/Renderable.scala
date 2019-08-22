package scadla.backends.renderers

import scadla.backends.renderers.BackwardCompatHelper.Solidable

trait Renderable[A] {
  def render(s: A, indent: Int): String
}

object Renderable {

  trait RenderableForOps[A] {
    val a: A
    val fa: Renderable[A]

    //Only for backward comp
    @Deprecated
    val fb: Solidable[A]
    @Deprecated
    def toSolid: scadla.Solid = fb.toSolid(a)

    override def toString: String = a.toString
    def render(indent: Int): String = fa.render(a, indent)
    def renderWithHeader: String = {
      """$fa=4;
        |$fs=0.5;
        |""".stripMargin + render(0)
    }
  }

  object RenderableForOps {
    def apply[A](s: A)(implicit ev: Renderable[A], ev2: Solidable[A]): RenderableForOps[A] =
      new RenderableForOps[A] {
        val a = s
        val fa = ev
        val fb = ev2
      }
  }

  implicit def autoRenderableForOps[A](s: A)(implicit ev: Renderable[A], ev2: Solidable[A]): RenderableForOps[A] = RenderableForOps(s)
  implicit def autoRenderableSeqForOps[A](seq: Seq[A])(implicit ev: Renderable[A], ev2: Solidable[A]): Seq[RenderableForOps[A]] = seq.map(s => RenderableForOps(s))

  implicit final class RenderableForOpsHelper[A](private val lhs: A)(implicit renderer: Renderable[A], ev2: Solidable[A]) {
    def toRenderableForOps: RenderableForOps[A] = autoRenderableForOps(lhs)
  }
}
