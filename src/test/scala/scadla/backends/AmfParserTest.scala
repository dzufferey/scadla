package scadla.backends

import scadla.*
import org.scalatest.funsuite.AnyFunSuite

class AmfParserTest extends AnyFunSuite {

  import ParserTest.*

  test("unit cube") {
    val cube = amf.Parser(path + "unit_cube.amf")
    assert(cube.faces.size == 12)
    checkCube(cube)
    //amf.Printer.store(cube, "test.amf")
  }

}
