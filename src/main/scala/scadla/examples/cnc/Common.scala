package scadla.examples.cnc

import scadla._
import utils._
import InlineOps._
import thread._
import scala.language.postfixOps // for mm notation
import squants.space.LengthConversions._ // for mm notation

object Common {
  
  val tightTolerance = 0.10 mm
  val tolerance      = 0.15 mm
  val looseTolerance = 0.22 mm

  val supportGap = 0.2 mm
  
  val nut = new NutPlaceHolder(looseTolerance)
  val bearing = {
    val t = looseTolerance
    Cylinder((11 mm) + t, (7 mm) + t).moveZ(-t/2)
  }
  val threading = new MetricThread(tolerance)

  // wood screws that can be used to hold multiples palstic parts together
  val woodScrewRadius = ISO.M3 - (0.5 mm) // for 3mm wood screws
  val woodScrewHeadHeight = 2 mm
  val woodScrewHeadRadius = 3 mm
  val woodScrewLength = 18 mm

}
