package dzufferey.scadla.examples.cnc

import dzufferey.scadla._
import utils._
import InlineOps._
import dzufferey.scadla.examples.fastener._

object Common {
  
  val tolerance = 0.15
  
  val nut = new NutPlaceHolder(tolerance)
  val bearing = Cylinder(11+tolerance, 11+tolerance, 7+tolerance).moveZ(-tolerance/2)
  val threading = new MetricThread(tolerance)

}
