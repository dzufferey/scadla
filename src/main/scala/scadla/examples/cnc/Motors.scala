package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import squants.space.Length
import scala.language.postfixOps
import squants.space.LengthConversions._

//TODO move that to the lib
//place holder for NEMA stepper motors

object Nema14 {

  val size = 35.2 mm
  
  def apply( length: Length,
             screwLength: Length,
             axisFlat: Length,
             axisLengthFront: Length,
             axisLengthBack: Length ): Solid = {
    NemaStepper(size, length,
                26 mm, thread.ISO.M3, screwLength,
                11 mm, 2.0 mm,
                2.5 mm, axisFlat, axisLengthFront, axisLengthBack)
  }
  
  def apply( length: Length,
             screwLength: Length,
             axisFlat: Length ): Solid = {
    apply(length, screwLength, axisFlat, 22 mm, 0 mm)
  }
  
  def apply( length: Length,
             screwLength: Length): Solid = {
    apply(length, screwLength, 0.45 mm, 22 mm, 0 mm)
  }

  def apply( length: Length): Solid = {
    apply(length, 3 mm, 0.45 mm, 22 mm, 0 mm)
  }
  
  def putOnScrew(s: Solid) = {
    NemaStepper.putOnScrew(26 mm, s)
  }

}

object Nema17 {

  val size = 42.3 mm

  def apply( length: Length,
             screwLength: Length,
             axisFlat: Length,
             axisLengthFront: Length,
             axisLengthBack: Length ): Solid = {
    NemaStepper(size, length,
                31 mm, thread.ISO.M3, screwLength,
                11 mm, 2.0 mm,
                2.5 mm, axisFlat, axisLengthFront, axisLengthBack)
  }
  
  def apply( length: Length,
             screwLength: Length,
             axisFlat: Length ): Solid = {
    apply(length, screwLength, axisFlat, 25 mm, 0 mm)
  }

  def apply( length: Length,
             screwLength: Length): Solid = {
    apply(length, screwLength, 0.45 mm, 25 mm, 0 mm)
  }

  def apply( length: Length): Solid = {
    apply(length, 5 mm, 0.45 mm, 22 mm, 0 mm)
  }
  
  def putOnScrew(s: Solid) = {
    NemaStepper.putOnScrew(31 mm, s)
  }

  def axis(length: Length) = NemaStepper.axis(2.5 mm, length, 0.45 mm)

}

object NemaStepper {

  def axis(axisRadius: Length, length: Length, axisFlat: Length) = {
    val a = Cylinder(axisRadius, length)
    if (axisFlat > (0 mm)) {
      a - Cube(2*axisRadius, 2*axisRadius, length).move(axisRadius - axisFlat, -axisRadius, 0 mm)
    } else {
      a
    }
  }

  def apply( side: Length,
             length: Length,
             screwSeparation: Length,
             screwSize: Length,
             screwLength: Length,
             flangeRadius: Length,
             flangeDepth: Length,
             axisRadius: Length,
             axisFlat: Length,
             axisLengthFront: Length,
             axisLengthBack: Length ) = {
    val base = CenteredCube.xy(side, side, length).moveZ(-length)
    val withFlange = if (flangeDepth > (0 mm)) base + Cylinder(flangeRadius, flangeDepth) else base
    val screw = Cylinder(screwSize, screwLength.abs)
    val screws = putOnScrew(screwSeparation, screw)
    val withScrews =
      if (screwLength > (0 mm)) {
        withFlange - screws.moveZ(-screwLength)
      } else if (screwLength < (0 mm)) {
        withFlange + screws
      } else {
        withFlange
      }
    val withFrontAxis = withScrews + axis(axisRadius, axisLengthFront, axisFlat)
    val withBackAxis = withFrontAxis + axis(axisRadius, axisLengthBack, axisFlat).moveZ(-length -axisLengthBack)
    withBackAxis
  }

  def putOnScrew(screwSeparation: Length, s: Solid) = {
    Union(
      s.move( -screwSeparation/2, -screwSeparation/2, 0 mm),
      s.move( -screwSeparation/2,  screwSeparation/2, 0 mm),
      s.move(  screwSeparation/2, -screwSeparation/2, 0 mm),
      s.move(  screwSeparation/2,  screwSeparation/2, 0 mm)
    )
  }

}
