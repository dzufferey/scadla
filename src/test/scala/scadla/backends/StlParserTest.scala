package scadla.backends

import scadla._
import org.scalatest._

class StlParserTest extends FunSuite {

  def checkCube(p: Polyhedron) = {
    p.faces.forall( t =>
      List(t.p1, t.p2, t.p3).forall(p =>
        List(p.x, p.y, p.z).forall( v => v == 0.0 || v == 1.0)))
  }

  val path = "src/test/resources/"
  

  test("ascii stl") {
    checkCube(stl.Parser(path + "unit_cube_ascii.stl"))
  }

  test("binary stl") {
    checkCube(stl.Parser(path + "unit_cube_binary.stl"))
  }

}
