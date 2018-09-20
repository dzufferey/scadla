package scadla

import org.scalatest._
import EverythingIsIn.millimeters

class SolidTest extends FunSuite {

  test("trace test") {
    val c = Cube(1,1,1)
    //for (t <- c.trace) {
    //  Console.println(t)
    //}
    assert(c.trace.head.getFileName == "SolidTest.scala")
  }

}
