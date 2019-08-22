package scadla.examples

import scadla._
import utils._
import Trig._
import scala.language.postfixOps

object WhiteboardMarkerHolder {
  import backends.renderers.InlineOps._
  import backends.renderers.OpenScad._
  import squants.space.LengthConversions._
  import backends.renderers.Renderable._
  import backends.renderers.BackwardCompatHelper._
  import backends.renderers.Solids._

  val penRadius = 10 mm

  val numberPen = 4

  val thickness = 3 mm

  val magnetRadius = 12 mm
  val magnetHeight = 5 mm

  val tolerance = 0.2 mm

  protected def hook = {
    PieSlice(penRadius + thickness, penRadius, 2*Pi/3, 20 mm)
  }

  def top = {
    val mrt = magnetRadius + thickness
    val beam1 = Cube( 90 mm, 10 mm, thickness).move(-45 mm, -mrt, thickness)
    val beam2 = Cube( 50 mm, (10 mm) + thickness, 2*thickness).move(-25 mm, -mrt, 0 mm)
    val center = Hull(
      Cylinder(mrt, 2*thickness),
      Cube(2*mrt, 1 mm, 2*thickness).move(-mrt, -mrt, 0 mm)
    )
    val magnet = Cylinder(magnetRadius + tolerance, magnetHeight + tolerance)
    beam1 + beam2 + center - magnet
  }

  def side = {
    val penSpace = 2.5 * penRadius
    val base = Cube(penSpace * numberPen + (10 mm), thickness, 20 mm)
    val hooks = (1 to numberPen).map( i => hook.rotateZ(-Pi/3).move(i * penSpace, 11.5 mm, 0 mm) )
    val lip = Cube(thickness, 2 * thickness, 20 mm)
    base ++ hooks + lip + lip.moveX((10 mm) + thickness + tolerance)
  }

  def main(args: Array[String]) {
    val r = backends.Renderer.default
    r.toSTL(top.rotateX(Pi).toSolid, "wbh_top.stl") // 1x
    r.toSTL(side.toSolid, "wbh_side.stl")           // 2x
  }

}
