package scadla.utils
  
import scadla._
import scadla.InlineOps._
import squants.space.Length
import squants.space.Angle
import scala.language.postfixOps
import squants.space.LengthConversions._
import squants.space.Radians

object PieSlice {

  def apply(outerRadius: Length, innerRadius: Length, angle: Angle, height: Length) = {
    val o1 = outerRadius + (1 mm)
    val t = Tube(outerRadius, innerRadius, height)
    val blocking_half = Translate(-o1, -o1, -0.5 mm, Cube(2* o1, o1, height + (1 mm)))
    val blocking_quarter = Translate(0 mm, 0 mm, -0.5 mm, Cube(o1, o1, height + (1 mm)))
    if (angle.value <= 0) {
      Empty
    } else {
      val block =
        if (angle <= Radians(math.Pi/2)) {
          Union(
            blocking_half,
            Translate(-o1, -0.5 mm, 0 mm, blocking_quarter),
            Rotate(0°, 0°, angle, blocking_quarter))
        } else if (angle <= Radians(math.Pi)) {
          Union(
            blocking_half,
            Rotate(0°, 0°, angle, blocking_quarter))
        } else if (angle <= Radians(3*math.Pi/2)) {
          Union(
            Translate(0 mm, -o1, 0 mm, blocking_quarter),
            Rotate(0°, 0°, angle, blocking_quarter))
        } else if (angle <= Radians(2*math.Pi)) {
          Intersection(
            Translate(0 mm, -o1, 0 mm, blocking_quarter),
            Rotate(0°, 0°, angle, blocking_quarter))
        } else {
          Empty
        }
      Difference(t, block)
    }
  }

}
