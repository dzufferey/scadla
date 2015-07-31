package scadla.assembly

import scadla._
import scadla.backends.Renderer

//final for pickling

//TODO immutable
final class Part(val name: String, val model: Solid, val printableModel: Option[Solid] = None) {

  var description: String = ""

  var vitamin = false

  //TODO should be protected but serialization ...
  var poly: Polyhedron = null
  var polyPrint: Polyhedron = null

  def preRender(r: Renderer) {
    if (poly == null) {
      poly = r(model)
      polyPrint = printableModel.map(r(_)).getOrElse(poly)
    }
  }

  def mesh = {
    if (poly != null) {
      poly
    } else {
      sys.error("Part not yet rendered.")
    }
  }

  def printable = {
    if (polyPrint != null) {
      polyPrint
    } else {
      sys.error("Part not yet rendered.")
    }
  }

  override def equals(any: Any) = {
    if (any.isInstanceOf[Part]) {
      val p = any.asInstanceOf[Part]
      name == p.name && model == p.model
    } else false
  }

}
