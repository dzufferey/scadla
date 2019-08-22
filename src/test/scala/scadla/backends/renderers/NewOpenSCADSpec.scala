package scadla.backends.renderers

import org.scalatest.{Matchers, WordSpecLike}
import scadla.backends.renderers.Solids.Hull
import scadla.{Cube, Cylinder, backends}
import scadla.examples.WhiteboardMarkerHolder.{magnetHeight, magnetRadius, thickness, tolerance}
import scadla.utils.Trig.Pi
import scala.language.postfixOps

class NewOpenSCADSpec extends WordSpecLike with Matchers {

  "OpenScad" should {

    "work with WhiteboardMaker examples" in {
      import backends.renderers.InlineOps._
      import backends.renderers.OpenScad._
      import squants.space.LengthConversions._
      import backends.renderers.Renderable._
      import backends.renderers.BackwardCompatHelper._
      object any2stringadd

      def top: Solids.Difference = {
        val mrt = magnetRadius + thickness
        val beam1 = Ops(Cube( 90 mm, 10 mm, thickness)).move(-45 mm, -mrt, thickness)
        val beam2 = Cube( 50 mm, (10 mm) + thickness, 2*thickness).move(-25 mm, -mrt, 0 mm)
        val center = Hull(
          Cylinder(mrt, 2*thickness),
          Cube(2*mrt, 1 mm, 2*thickness).move(-mrt, -mrt, 0 mm)
        )
        val magnet = Cylinder(magnetRadius + tolerance, magnetHeight + tolerance)
        beam1 + beam2 + center - magnet
      }
      val s = top.rotateX(Pi)

      val original =
        """$fa=4;
          |$fs=0.5;
          |rotate([180.0,0.0,0.0])
          |  difference(){
          |    union(){
          |      union(){
          |        translate([-45.0,-15.0,3.0])
          |          cube([ 90.0, 10.0, 3.0]);
          |        translate([-25.0,-15.0,0.0])
          |          cube([ 50.0, 13.0, 6.0]);
          |      }
          |      hull(){
          |        cylinder( r1 = 15.0, r2 = 15.0, h = 6.0);
          |        translate([-15.0,-15.0,0.0])
          |          cube([ 30.0, 1.0, 6.0]);
          |      }
          |    }
          |    cylinder( r1 = 12.2, r2 = 12.2, h = 5.2);
          |  }
          |""".stripMargin

      s.renderWithHeader shouldBe original
    }
  }

}
