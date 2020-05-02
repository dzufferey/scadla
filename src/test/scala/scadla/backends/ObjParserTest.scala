package scadla.backends

import scadla._
import org.scalatest.funsuite.AnyFunSuite

class ObjParserTest extends AnyFunSuite {
  
  import ParserTest._

  test("unit cube") {
    val cube = obj.Parser(path + "unit_cube.obj")
    assert(cube.faces.size == 12)
    checkCube(cube)
  }

}
