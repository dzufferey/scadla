package scadla.examples.cnc

import scadla._
import utils._
import InlineOps._
import scadla.examples.fastener._
import Common._
import scadla.EverythingIsIn.{millimeters, radians}  
import squants.space.Length
import scala.language.postfixOps

object Chuck {

  protected def doubleHex(radius: Length, height: Length) = {
    val h = Hexagon(radius, height)
    Union(h, h.rotateZ(30°))
  }
  
  //TODO some more parameters
  //assumes M8 middle
  def innerThread( outerRadius: Length,
                   innerHole: Length,
                   chuckHeight: Length,
                   colletLength: Length,
                   mNumber: Length ) = {
  val shaft = Thread.ISO.M8 + tightTolerance //TODO add more/less tolerance ???
  val splitWasher = 2
  val nutHeight = nut.height(shaft)
    val body = Union(
      Cylinder(outerRadius, chuckHeight - 5).moveZ(5),
      doubleHex(Hexagon.minRadius(outerRadius).toMillimeters, 5)
    )
    val toRemove = List(
      threading.screwThreadIsoInner(mNumber, colletLength),
      Cylinder(shaft, chuckHeight),
      nut(shaft).moveZ(colletLength + splitWasher + nutHeight),
      Cylinder( innerHole, colletLength + splitWasher + nutHeight),
      Cylinder( innerHole+1, innerHole, colletLength)
    )
    body -- toRemove
  }
  
  //TODO some more parameters
  def wrench(outerRadius: Length) = {
    val wall = 5
    val hex = doubleHex(Hexagon.minRadius(outerRadius).toMillimeters + looseTolerance, 5)
    val head = Cylinder(outerRadius + wall, 5)
    val handle = RoundedCubeH(outerRadius*6, outerRadius*2, 5, 3)
    head + handle.moveY(-outerRadius) - hex
  }

}
