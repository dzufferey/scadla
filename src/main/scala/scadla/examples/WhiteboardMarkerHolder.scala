package scadla.examples

import math._
import scadla._
import utils._
import InlineOps._

object WhiteboardMarkerHolder {

  val penRadius = 10

  val numberPen = 4

  val thickness = 3

  val magnetRadius = 12
  val magnetHeight = 5

  val tolerance = 0.2

  protected def hook = {
    PieSlice(penRadius + thickness, penRadius, 2*Pi/3, 20)
  }

  def top = {
    val mrt = magnetRadius + thickness
    val beam1 = Cube( 90, 10, thickness).move(-45, -mrt, thickness)
    val beam2 = Cube( 50, 10 + thickness, 2*thickness).move(-25, -mrt, 0)
    val center = Hull(
      Cylinder(mrt, 2*thickness),
      Cube(2*mrt, 1, 2*thickness).move(-mrt, -mrt, 0)
    )
    val magnet = Cylinder(magnetRadius + tolerance, magnetHeight + tolerance)
    beam1 + beam2 + center - magnet
  }

  def rail = {
    val penSpace = 2.5 * penRadius
    val base = Cube(penSpace * numberPen + 10, thickness, 20)
    val hooks = (1 to numberPen).map( i => hook.rotateZ(-Pi/3).move(i * penSpace, 11.5, 0) )
    val lip = Cube(thickness, 2 * thickness, 20)
    base ++ hooks + lip + lip.moveX(10 + thickness + tolerance)
  }

  def main(args: Array[String]) {
    backends.Renderer.default.view(top)
    //backends.Renderer.default.view(rail)
  }

}
