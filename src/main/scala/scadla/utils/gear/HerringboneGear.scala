package scadla.utils.gear

import scadla._
import scadla.InlineOps._
import scala.math._

object HerringboneGear {

  /** Create an herringbone gear (from two helical gears of opposite rotations)
   * @param pitch the effective radius of the gear
   * @param nbrTeeth the number of tooth in the gear
   * @param pressureAngle the angle between meshing gears at the pitch radius (0 mean "square" tooths, π/2 no tooths)
   * @param addenum how much to add to the pitch to get the outer radius of the gear
   * @param dedenum how much to remove to the pitch to get the root radius of the gear
   * @param height the height of the gear
   * @param helixAngle how much twisting (in rad / mm)
   * @param backlash add some space (manufacturing tolerance)
   * @param skew generate a gear with an asymmetric profile by skewing the tooths
   */
  def apply( pitch: Double,
             nbrTeeth: Int,
             pressureAngle: Double,
             addenum: Double,
             dedenum: Double,
             height: Double,
             helixAngle: Double,
             backlash: Double,
             skew: Double = 0.0) = {
    val stepped = InvoluteGear.stepped(pitch, nbrTeeth, pressureAngle, addenum, dedenum, height, backlash, skew)
    def turnPoint(p: Point): Point = {
      val z = p.z
      val a = if (z < height/2) helixAngle * z
              else helixAngle * (height - z)
      val x = p.x * cos(a) - p.y * sin(a)
      val y = p.x * sin(a) + p.y * cos(a)
      Point(x, y, z)
    }
    Polyhedron(stepped.faces.map( f => Face(turnPoint(f.p1), turnPoint(f.p2), turnPoint(f.p3)) ))
  }
  
}
