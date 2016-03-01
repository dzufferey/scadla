package scadla.backends

import scadla._
import org.scalatest._

class ObjParserTest extends FunSuite {

  def checkCube(p: Polyhedron) = {
    p.faces.forall( t =>
      List(t.p1, t.p2, t.p3).forall(p =>
        List(p.x, p.y, p.z).forall( v => v == 0.0 || v == 1.0)))
  }

  val path = "src/test/resources/"
  

  test("unit cube") {
    val cube = obj.Parser(path + "unit_cube.obj")
    assert(cube.faces.size == 12)
    checkCube(cube)
  }

}
