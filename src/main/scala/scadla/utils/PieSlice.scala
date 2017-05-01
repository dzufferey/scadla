package scadla.utils
  
import scadla._
import scadla.InlineOps._
import scadla.utils.Trig._
import squants.space.{Length, Angle}
import scala.language.postfixOps
import squants.space.LengthConversions._

object PieSlice {

  def apply(outerRadius: Length, innerRadius: Length, angle: Angle, height: Length) = {
    val o1 = outerRadius + (1 mm)
    val h1 = height + (1 mm)
    val blocking_half = Cube(2* o1, o1, h1).move(-o1, -o1, -0.5 mm)
    val blocking_quarter = Cube(o1, o1, h1).move(0 mm, 0 mm, -0.5 mm)
    if (angle.value <= 0) {
      Empty
    } else {
      val block =
        if (angle <= Pi/2) {
          Union(
            blocking_half,
            blocking_quarter.move(-o1, -0.5 mm, 0 mm),
            blocking_quarter.rotateZ(angle)
          )
        } else if (angle <= Pi) {
          Union(
            blocking_half,
            blocking_quarter.rotateZ(angle)
          )
        } else if (angle <= 3*Pi/2) {
          Union(
            blocking_quarter.moveY(-o1),
            blocking_quarter.rotateZ(angle)
          )
        } else if (angle <= 2*Pi) {
          Intersection(
            blocking_quarter.moveY(-o1),
            blocking_quarter.rotateZ(angle)
          )
        } else {
          Empty
        }
      val t = Tube(outerRadius, innerRadius, height)
      Difference(t, block)
    }
  }

}
