package scadla.backends

import scadla.*
import org.scalatest.funsuite.AnyFunSuite

class ObjParserTest extends AnyFunSuite {
  
  import ParserTest.*

  test("unit cube") {
    val cube = obj.Parser(path + "unit_cube.obj")
    assert(cube.faces.size == 12)
    checkCube(cube)
  }

}
