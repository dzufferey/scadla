package scadla.utils.gear

import scadla.*
import scadla.InlineOps.*
import scala.math.*
import squants.space.{Length, Angle, Radians}

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
  def apply( pitch: Length,
             nbrTeeth: Int,
             pressureAngle: Double,
             addenum: Length,
             dedenum: Length,
             height: Length,
             twist: Twist,
             backlash: Length,
             skew: Angle = Radians(0.0)) = {
    val stepped = InvoluteGear.stepped(pitch, nbrTeeth, pressureAngle, addenum, dedenum, height, backlash, skew)
    def turnPoint(p: Point): Point = {
      val z = p.z
      val a = if (z < height/2) twist.angle * (z / twist.increment)
              else twist.angle * ((height - z) / twist.increment)
      val x = p.x * a.cos - p.y * a.sin
      val y = p.x * a.sin + p.y * a.cos
      Point(x, y, z)
    }
    Polyhedron(stepped.faces.map( f => Face(turnPoint(f.p1), turnPoint(f.p2), turnPoint(f.p3)) ))
  }

}
