package scadla.backends

import scadla._
import org.scalatest.funsuite.AnyFunSuite

class AmfParserTest extends AnyFunSuite {

  import ParserTest._

  test("unit cube") {
    val cube = amf.Parser(path + "unit_cube.amf")
    assert(cube.faces.size == 12)
    checkCube(cube)
    //amf.Printer.store(cube, "test.amf")
  }

}
