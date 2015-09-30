package scadla.backends

import scadla._
import java.io._

trait Renderer {

  def apply(s: Solid): Polyhedron

  def toSTL(s: Solid, fileName: String) {
    val p = apply(s)
    stl.Printer.storeBinary(p, fileName)
    //stl.Printer.storeText(p, fileName)
  }

  def view(s: Solid) = Viewer.default(apply(s))

}

object Renderer {

  def default: Renderer = {
    if (OpenSCADnightly.isPresent) OpenSCADnightly
    else if (OpenSCAD.isPresent) OpenSCAD
    else JCSG
  }

}
