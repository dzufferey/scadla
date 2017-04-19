package scadla.examples.fastener

import scadla._
import scadla.utils._
import math._
import InlineOps._
import scadla.EverythingIsIn.{millimeters, radians}  

object Nut {

  private lazy val thread = new MetricThread()

  def apply(radius: Double) = {
    thread.hexNutIso(radius*2, 1.6 * radius)
  }

  def minOuterRadius(innerRadius: Double) = 1.6 * innerRadius
  def maxOuterRadius(innerRadius: Double) = Hexagon.maxRadius(minOuterRadius(innerRadius))
  def height(innerRadius: Double) = 1.6 * innerRadius

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
class NutPlaceHolder(tolerance: Double = 0.1) {

  protected val factor = 1.6

  def apply(radius: Double) = {
    Hexagon(radius * factor + tolerance, factor * radius + tolerance)
  }
  
  def minOuterRadius(innerRadius: Double) = factor * innerRadius + tolerance
  def maxOuterRadius(innerRadius: Double) = Hexagon.maxRadius(minOuterRadius(innerRadius)).toMillimeters
  def height(innerRadius: Double) = factor * innerRadius + tolerance

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

class StructuralNutPlaceHolder(tolerance: Double = 0.1) extends NutPlaceHolder(tolerance) {

  override protected val factor = 1.8

}
