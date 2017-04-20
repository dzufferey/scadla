package scadla.examples.fastener

import scadla._
import scadla.utils._
import math._
import InlineOps._
import squants.space.Length
import scala.language.postfixOps
import squants.space.LengthConversions._

object Nut {

  private lazy val thread = new MetricThread()

  def apply(radius: Length) = {
    thread.hexNutIso(radius*2, 1.6 * radius)
  }

  def minOuterRadius(innerRadius: Length) = 1.6 * innerRadius
  def maxOuterRadius(innerRadius: Length) = Hexagon.maxRadius(minOuterRadius(innerRadius))
  def height(innerRadius: Length) = 1.6 * innerRadius

  //metric versions (ISO)
  def M1   = apply( Thread.ISO.M1 )
  def M1_2 = apply( Thread.ISO.M1_2 )
  def M1_6 = apply( Thread.ISO.M1_6 )
  def M2   = apply( Thread.ISO.M2 )
  def M2_5 = apply( Thread.ISO.M2_5 )
  def M3   = apply( Thread.ISO.M3 )
  def M4   = apply( Thread.ISO.M4 )
  def M5   = apply( Thread.ISO.M5 )
  def M6   = apply( Thread.ISO.M6 )
  def M8   = apply( Thread.ISO.M8 )
  def M10  = apply( Thread.ISO.M10 )
  def M12  = apply( Thread.ISO.M12 )
  def M16  = apply( Thread.ISO.M16 )
  def M20  = apply( Thread.ISO.M20 )
  def M24  = apply( Thread.ISO.M24 )
  def M30  = apply( Thread.ISO.M30 )
  def M36  = apply( Thread.ISO.M36 )
  def M42  = apply( Thread.ISO.M42 )
  def M48  = apply( Thread.ISO.M48 )
  def M56  = apply( Thread.ISO.M56 )
  def M64  = apply( Thread.ISO.M64 )

}

/** simple Hexagon as placeholder for nuts (rendering is much faster) */
class NutPlaceHolder(tolerance: Length = 0.1 mm) {

  protected val factor = 1.6

  def apply(radius: Length) = {
    Hexagon(radius * factor + tolerance, factor * radius + tolerance)
  }
  
  def minOuterRadius(innerRadius: Length) = factor * innerRadius + tolerance
  def maxOuterRadius(innerRadius: Length) = Hexagon.maxRadius(minOuterRadius(innerRadius)).toMillimeters
  def height(innerRadius: Length) = factor * innerRadius + tolerance

  def M1   = apply( Thread.ISO.M1 )
  def M1_2 = apply( Thread.ISO.M1_2 )
  def M1_6 = apply( Thread.ISO.M1_6 )
  def M2   = apply( Thread.ISO.M2 )
  def M2_5 = apply( Thread.ISO.M2_5 )
  def M3   = apply( Thread.ISO.M3 )
  def M4   = apply( Thread.ISO.M4 )
  def M5   = apply( Thread.ISO.M5 )
  def M6   = apply( Thread.ISO.M6 )
  def M8   = apply( Thread.ISO.M8 )
  def M10  = apply( Thread.ISO.M10 )
  def M12  = apply( Thread.ISO.M12 )
  def M16  = apply( Thread.ISO.M16 )
  def M20  = apply( Thread.ISO.M20 )
  def M24  = apply( Thread.ISO.M24 )
  def M30  = apply( Thread.ISO.M30 )
  def M36  = apply( Thread.ISO.M36 )
  def M42  = apply( Thread.ISO.M42 )
  def M48  = apply( Thread.ISO.M48 )
  def M56  = apply( Thread.ISO.M56 )
  def M64  = apply( Thread.ISO.M64 )

}

class StructuralNutPlaceHolder(tolerance: Length = 0.1 mm) extends NutPlaceHolder(tolerance) {

  override protected val factor = 1.8

}
