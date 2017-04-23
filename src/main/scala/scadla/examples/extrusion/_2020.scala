package scadla.examples.extrusion

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import scadla.EverythingIsIn.{millimeters, radians}
import squants.space.Length
import scala.language.postfixOps // for mm notation
import squants.space.LengthConversions._ // for mm notation

//place holder for 20x20mm aluminium extrusions
object _2020 {

  val width = 20 mm

  protected def centerHole(length: Length) = Cylinder(2.1, length+2).moveZ(-1)

  def apply(length: Length) = {
    val base = RoundedCubeH(20, 20, length, 1.5).move(-10, -10, 0)
    val shell = Difference(
      base,
      CenteredCube.xy(16, 16, length + 1).moveZ(-1),
      CenteredCube.xy(22, 6.5, length + 1).moveZ(-1),
      CenteredCube.xy(6.5, 22, length + 1).moveZ(-1)
    )
    val withInner = Union(
      shell,
      CenteredCube.xy(8, 8, length), //center
      CenteredCube.xy(3, 3, length).move(-7.5, -7.5, 0), //corner
      CenteredCube.xy(3, 3, length).move(-7.5,  7.5, 0), //corner
      CenteredCube.xy(3, 3, length).move( 7.5, -7.5, 0), //corner
      CenteredCube.xy(3, 3, length).move( 7.5,  7.5, 0), //corner
      CenteredCube.xy(1.75, 25, length).rotateZ( Pi/4), //cross
      CenteredCube.xy(1.75, 25, length).rotateZ(-Pi/4)  //cross
    )
    withInner - centerHole(length)
  }
  
  def placeHolder(length: Length) = CenteredCube.xy(20, 20, length)

  def connector(plateThicknesss: Length,
                knobHeight: Length,
                tolerance: Length) = {
    val base = CenteredCube.xy(20, 20, plateThicknesss + knobHeight).moveZ(-plateThicknesss)
    val negative = Bigger(apply(knobHeight), tolerance).moveZ(tolerance/2)
    val hole = Cylinder(Thread.ISO.M5, knobHeight + plateThicknesss + 1).moveZ(-plateThicknesss-1)
    Difference(
      base,
      negative,
      hole,
      CenteredCube.xy(4,4,knobHeight+1).move( 10, 10, 0),
      CenteredCube.xy(4,4,knobHeight+1).move( 10,-10, 0),
      CenteredCube.xy(4,4,knobHeight+1).move(-10, 10, 0),
      CenteredCube.xy(4,4,knobHeight+1).move(-10,-10, 0)
    )
  }

  //TODO better name
  def pad(length: Length, threadRadius: Length, tolerance: Length) = {
    val bot = 14 mm
    val top = 8 mm
    val height = 3 mm
    val d = 2 mm
    val base = Trapezoid(top, bot, length, height)
    val chamfered = base * Cube(bot - d, length, height).moveX(d/2)
    Difference(
      chamfered,
      Cylinder(threadRadius-tolerance, height).move(bot/2, length/2, 0),
      new examples.fastener.NutPlaceHolder().apply(threadRadius).move(bot/2, length/2, height-1.6*threadRadius+1)
    )
  }

}
