package scadla.examples

import math.*
import scadla.*
import utils.*
import InlineOps.*

/** An experiment to make belts with silicone and thread / small ropes.
 * @param length
 * @param threadDiameter (typically ~1)
 * @param threadTurns (typically ~3)
 * @param jacket how much silicone around the thread (typically ~0.5)
 * @param tolerance some tolerance between parts to account for the printer
 *
 * How is it supposed to work:
 * ⒈  screw the two half of the inner mold
 * ⒉  add silicone
 * ⒊  add 1st outer mold and let it cure
 * ⒋  remove outer mold
 * ⒌  spread thin layer of silicon in the cured part
 * ⒍  wrap the thread in the silicone groove
 * ⒎  fill with silicone
 * ⒏  add 2nd outer mold and let it cure
 * ⒐  take apart and clean
 */
class BeltMold(length: Double,
               threadDiameter: Double,
               threadTurns: Int,
               jacket: Double,
               tolerance: Double) {

  import scadla.EverythingIsIn.{millimeters, radians}
  
  val innerRadius = length / 2 / Pi
  val outerRadius = innerRadius + threadDiameter + 2*jacket
  val height = threadDiameter * threadTurns + 2*jacket
  val screwRadius = 1.5

  def inner = {
    val base = Union(
      Cylinder(outerRadius + 1, 1),
      Cylinder(outerRadius, 1).moveZ(1),
      Cylinder(innerRadius, height/2).moveZ(2)
    )
    val peg = Cylinder(2, height + 4)
    val pegs = (0 until 3).map( i => peg.moveX(innerRadius - 5).rotateZ(i * 2*Pi/3) )
    val baseWithPegs = base ++ pegs - Bigger(Union(pegs:_*), tolerance).rotateZ(Pi/3)
    val screw = Cylinder(screwRadius, height + 2)
    val screws = (0 until 6).map( i => screw.moveX(innerRadius - 5).rotateZ(i * 2*Pi/6 + Pi/6) )
    baseWithPegs -- screws
  }

  def outerFst = {
    Union(
      outerSnd,
      PieSlice(outerRadius+tolerance, innerRadius + jacket + threadDiameter/2 + tolerance, Pi, height).moveZ(1),
      PieSlice(outerRadius+tolerance, innerRadius + jacket + tolerance, Pi, threadTurns*threadDiameter).moveZ(1+jacket)
    )
  }

  def outerSnd = {
    val h = height + 2
    val ring = PieSlice(outerRadius + 2, outerRadius, Pi, h)
    val block = Cube(10, 5, h) - Cylinder(screwRadius, 5).rotateX(-Pi/2).move( 7, 0, h/2)
    val blockPositionned = block.moveX(outerRadius)
    ring + blockPositionned + blockPositionned.mirror(1,0,0)
  }

  def spreader = {
    val base = outerFst * PieSlice(outerRadius + 2, 0, Pi/6, height+2).rotateZ(Pi/2)
    base - Cube(innerRadius, innerRadius, height+2).moveX(-innerRadius)
  }

}

object BeltMold {

  def sampleBelt = new BeltMold(200, 1, 3, 0.5, 0.2)

  def main(args: Array[String]) = {
    val r = backends.Renderer.default
    r.toSTL(sampleBelt.inner, "belt-inner.stl")
    r.toSTL(sampleBelt.outerFst, "belt-outer1.stl")
    r.toSTL(sampleBelt.outerSnd, "belt-outer2.stl")
    r.toSTL(sampleBelt.spreader, "belt-spreader.stl")
    //r.view(sampleBelt.inner)
    //r.view(sampleBelt.outerFst)
    //r.view(sampleBelt.outerSnd)
    //r.view(sampleBelt.spreader)
  }

}
