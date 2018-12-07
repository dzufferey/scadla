package scadla.utils.gear

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import squants.space.{Length, Radians, Millimeters, Angle}

object Rack {

  //for carving so the backlash makes the tooth larger
  def tooth( toothWidth: Length,
             pressureAngle: Double,
             addenum: Length,
             dedenum: Length,
             height: Length,
             backlash: Length,
             skew: Angle ) = {
    assert(pressureAngle < Pi / 2 && pressureAngle >= 0, "pressureAngle must be between in [0;π/2)")
    val base = toothWidth + 2 * addenum * tan(pressureAngle) + backlash
    val tip  = toothWidth - 2 * dedenum * tan(pressureAngle) + backlash
    assert(tip.value > 0, "tip of the profile is negative ("+tip+"), try decreasing the pressureAngle, or addenum/dedenum.")
    val tHeight = addenum + dedenum + backlash
    Trapezoid(tip, base, height, tHeight, skew).rotateX(Radians(Pi/2)).move(-base/2 + addenum*tan(skew.toRadians), addenum, Millimeters(0)).rotateZ(Radians(-Pi/2))
  }
  
  /** Create an involute spur gear.
   * @param toothWidth the width of a tooth
   * @param nbrTeeth the number of tooth in the gear
   * @param pressureAngle the angle between meshing gears at the pitch radius (0 mean "square" tooths, π/2 no tooths)
   * @param addenum how much to add to the pitch to get the outer radius of the gear
   * @param dedenum how much to remove to the pitch to get the root radius of the gear
   * @param height the height of the gear
   * @param backlash add some space (manufacturing tolerance)
   * @param skew generate a gear with an asymmetric profile by skewing the tooths
   */
  def apply( toothWidth: Length,
             nbrTeeth: Int,
             pressureAngle: Double,
             addenum: Length,
             dedenum: Length,
             height: Length,
             backlash: Length,
             skew: Angle = Radians(0.0)) = {

    assert(addenum.value > 0, "addenum must be greater than 0")
    assert(dedenum.value > 0, "dedenum must be greater than 0")
    assert(nbrTeeth > 0, "number of tooths must be greater than 0")
    assert(toothWidth.value > 0, "toothWidth must be greater than 0")

    val rackTooth = tooth(toothWidth, pressureAngle, addenum, dedenum, height, -backlash, skew)

    val space = 2*toothWidth
    val teeth = for (i <- 0 until nbrTeeth) yield rackTooth.moveY(i * space)
    val base = Cube(Gear.baseThickness, nbrTeeth * space, height).move(dedenum, -space/2, Millimeters(0))
    base ++ teeth
  }

}
