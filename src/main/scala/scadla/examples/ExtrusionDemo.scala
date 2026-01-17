package scadla.examples

import scadla.*
import scadla.InlineOps.*
import scadla.utils.extrusion.*
import scadla.EverythingIsIn.millimeters

object ExtrusionDemo {

  def main(args: Array[String]): Unit = {
    val r = scadla.backends.Renderer.default
    val objects = List(
      _2020(100),
      C(20,20,10,4)(100),
      H(20,20,4)(100),
      L(20,20,4)(100),
      T(20,20,4)(100),
      U(20,20,4)(100),
      Z(20,20,4)(100)
    )
    val moved = objects.zipWithIndex.map{ case(o, i) => o.moveY(25 * i) }
    r.view(Union(moved: _*))
  }

}
