package dzufferey.scadla.backends

import dzufferey.scadla._
import java.io._

trait Renderer {

  def apply(s: Solid): Polyhedron

  def toSTL(s: Solid, fileName: String) {
    val p = apply(s)
    stl.Printer.storeBinary(p, fileName)
    //stl.Printer.storeText(p, fileName)
  }

}

