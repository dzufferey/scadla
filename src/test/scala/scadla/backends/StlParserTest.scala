package scadla.backends

import scadla._
import org.scalatest.funsuite.AnyFunSuite
import squants.space.Millimeters

object ParserTest {
  def checkCube(p: Polyhedron) = {
    p.faces.forall( t =>
      List(t.p1, t.p2, t.p3).forall(p =>
        List(p.x, p.y, p.z).forall( v => v == Millimeters(0) || v == Millimeters(1))))
  }

  val path = "src/test/resources/"
}

class StlParserTest extends AnyFunSuite {

  import ParserTest._

  test("ascii stl") {
    checkCube(stl.Parser(path + "unit_cube_ascii.stl"))
  }

  test("binary stl") {
    checkCube(stl.Parser(path + "unit_cube_binary.stl"))
  }

}
