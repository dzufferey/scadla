package scadla.examples

import scadla._
import scadla.InlineOps._
import scadla.utils.extrusion._
import scadla.EverythingIsIn.millimeters

object ExtrusionDemo {

  def s = {
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
    Union(moved: _*)
  }

  def main(args: Array[String]) {
    val r = scadla.backends.Renderer.default
    r.view(s)
  }

}
