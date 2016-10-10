package scadla.examples.cnc

import scadla._
import utils._
import InlineOps._
import scadla.examples.fastener._

object Common {
  
  val tightTolerance = 0.10
  val tolerance      = 0.15
  val looseTolerance = 0.22

  val supportGap = 0.2
  
  val nut = new NutPlaceHolder(looseTolerance)
  val bearing = {
    val t = looseTolerance
    Cylinder(11+t, 7+t).moveZ(-t/2)
  }
  val threading = new MetricThread(tolerance)

}
