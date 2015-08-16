package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._

//place holder for 20x20mm aluminium extrusions
object Extrusion {

  val width = 20

  protected def centerHole(length: Double) = Cylinder(2.1, length+2).moveZ(-1)

  def apply(length: Double) = {
    val base = roundedCubeH(20,20, length, 1.5).move(-10, -10, 0)
    val shell = Difference(
      base,
      centeredCubeXY(16, 16, length + 1).moveZ(-1),
      centeredCubeXY(22, 6.5, length + 1).moveZ(-1),
      centeredCubeXY(6.5, 22, length + 1).moveZ(-1)
    )
    val withInner = Union(
      shell,
      centeredCubeXY(8, 8, length), //center
      centeredCubeXY(3, 3, length).move(-7.5, -7.5, 0), //corner
      centeredCubeXY(3, 3, length).move(-7.5,  7.5, 0), //corner
      centeredCubeXY(3, 3, length).move( 7.5, -7.5, 0), //corner
      centeredCubeXY(3, 3, length).move( 7.5,  7.5, 0), //corner
      centeredCubeXY(1.75, 25, length).rotateZ( Pi/4), //cross
      centeredCubeXY(1.75, 25, length).rotateZ(-Pi/4)  //cross
    )
    withInner - centerHole(length)
  }
  
  def placeHolder(length: Double) = centeredCubeXY(20, 20, length)

  def connector(plateThicknesss: Double,
                knobHeight: Double,
                tolerance: Double = Common.tightTolerance) = {
    val base = centeredCubeXY(20, 20, plateThicknesss + knobHeight).moveZ(-plateThicknesss)
    val negative = bigger(apply(knobHeight), tolerance).moveZ(tolerance/2)
    val hole = Cylinder(Thread.ISO.M5, knobHeight + plateThicknesss + 1).moveZ(-plateThicknesss-1)
    Difference(
      base,
      negative,
      hole,
      centeredCubeXY(4,4,knobHeight+1).move( 10, 10, 0),
      centeredCubeXY(4,4,knobHeight+1).move( 10,-10, 0),
      centeredCubeXY(4,4,knobHeight+1).move(-10, 10, 0),
      centeredCubeXY(4,4,knobHeight+1).move(-10,-10, 0)
    )
  }

}
