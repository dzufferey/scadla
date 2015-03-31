package dzufferey.scadla.utils
  
import dzufferey.scadla._
import scala.math._

object Involute {
  
  // https://en.wikipedia.org/wiki/Involute
  // in cartesian form:
  //  x = r (cos(a) + a * sin(a))
  //  y = r (sin(a) - a * cos(a))
  // in polar form:
  //  ρ = r * sqrt(1 + a^2)
  //  φ = a - arctan(a)

  def x(radius: Double, phase: Double, theta: Double): Double = {
    radius * (cos(theta) + (theta - phase) * sin(theta))
  }

  def y(radius: Double, phase: Double, theta: Double): Double = {
    radius * (sin(theta) - (theta - phase) * cos(theta))
  }
  
  def x(radius: Double, theta: Double): Double = x(radius, 0, theta)
  def y(radius: Double, theta: Double): Double = x(radius, 0, theta)

  def x(theta: Double): Double = x(1, theta)
  def y(theta: Double): Double = x(1, theta)



  def apply(radius: Double, phase: Double, start: Double, end: Double, height: Double, stepSize: Double): Polyhedron = {
    assert(radius > 0)
    assert(end > start)
    assert(height > 0)
    assert(stepSize > 0)

    val steps = ceil((end - start) / stepSize).toInt
    val actualStepSize = (end - start) / steps

    val points = Array.ofDim[Point](2 * (2 + steps))
    points(0) = Point(0,0,0)
    points(1) = Point(0,0,height)

    for (i <- (0 to steps)) {
      val theta = start + i * actualStepSize
      val x1 = x(radius, phase, theta)
      val y1 = y(radius, phase, theta)
      points(2*(i+1)    ) = Point(x1, y1, 0)
      points(2*(i+1) + 1) = Point(x1, y1, height)
    }

    def pt(i: Int) = points(i % points.size)
    def face(a: Int, b: Int, c: Int) = Face(pt(a), pt(b), pt(c))

    val topBot = (1 to steps).flatMap( i => {
      val j = 2*i
      val top = face(1  , j+1, j+3)
      val bot = face(0  , j+2, j  )
      Seq(top, bot)
    })
    val ext = (0 to steps + 1).flatMap( i => {
      val j = 2*i
      val ex1 = face(j  , j+2, j+1)
      val ex2 = face(j+2, j+3, j+1)
      Seq(ex1, ex2)
    })
    val faces = topBot ++ ext
    Polyhedron(faces)
  }
  
  def apply(radius: Double, start: Double, end: Double, height: Double): Polyhedron = {
    apply(radius, 0, start, end, height, 0.1)
  }
    
}
