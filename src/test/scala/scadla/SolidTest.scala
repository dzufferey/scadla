package scadla

import org.scalatest.funsuite.AnyFunSuite
import EverythingIsIn.millimeters

class SolidTest extends AnyFunSuite {

  test("trace test") {
    val c = Cube(1,1,1)
    //for (t <- c.trace) {
    //  Console.println(t)
    //}
    assert(c.trace.head.getFileName == "SolidTest.scala")
  }

}
