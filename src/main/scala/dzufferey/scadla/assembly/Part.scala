package dzufferey.scadla.assembly

import dzufferey.scadla._
import dzufferey.scadla.backends.Renderer

//final for pickling

//TODO immutable
final class Part(val name: String, val model: Solid) {

  var description: String = ""

  var vitamin = false

  protected var printTransform = List[Solid => Solid]() 

  protected var poly: Polyhedron = null
  protected var polyPrint: Polyhedron = null

  def addPrintTransform(fct: Solid => Solid) = {
    printTransform ::= fct
  }
  
  def preRender(r: Renderer) {
    if (poly == null) {
      poly = r(model)
      polyPrint = r(printTransform.foldRight(model)( (f, acc) => f(acc) ))
    }
  }

  def mesh = {
    if (poly != null) {
      poly
    } else {
      sys.error("Part not yet rendered.")
    }
  }

  def printableModel = {
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
