package scadla.backends

import scadla._
import org.scalatest._

class AmfParserTest extends FunSuite {

  import ParserTest._

  test("unit cube") {
    val cube = amf.Parser(path + "unit_cube.amf")
    assert(cube.faces.size == 12)
    checkCube(cube)
    //amf.Printer.store(cube, "test.amf")
  }

}
