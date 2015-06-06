package dzufferey.scadla.examples.cnc

import math._
import dzufferey.scadla._
import utils._
import InlineOps._
import Common._

class Beam(wall: Double, innerThreadRadius: Double) {
  
  def basic(length: Double) = {
    val xy2 = wall + innerThreadRadius
    val xy = xy2 * 2
    val o = Cube(xy, xy, length)
    val i = Hexagon(innerThreadRadius, length)
    o - i.move(xy2, xy2, 0)
  }

  def carveMale(s: Solid) = {
    ???
  }

  def carveFemale(s: Solid) = {
    ???
  }

  //TODO
  //connector: straight (dovetail?), L, T, diagonals
  //ends: space for the nuts, washers, and nut blockers


}
