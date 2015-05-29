package dzufferey.scadla.examples.cnc

import dzufferey.scadla._
import utils._
import InlineOps._
import dzufferey.scadla.examples.fastener._

object Common {
  
  val tightTolerance = 0.15
  val looseTolerance = 0.22
  val tolerance = tightTolerance
  
  val nut = new NutPlaceHolder(looseTolerance)
  val bearing = {
    val t = looseTolerance
    Cylinder(11+t, 7+t).moveZ(-t/2)
  }
  val threading = new MetricThread(tolerance)

}
