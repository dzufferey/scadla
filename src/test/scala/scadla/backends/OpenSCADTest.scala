package scadla.backends

import scadla._
import org.scalatest._
import scadla.EverythingIsIn.{millimeters, radians}


class OpenSCADTest extends FunSuite {

  test("rendering a cube") {
    if (OpenSCAD.isPresent) {
      val obj = OpenSCAD(Cube(1, 1, 1))
      assert(obj.faces.size == 12)
    } else {
      // skip
    }
  }

//test("rendering 1a") {
//  val obj = OpenSCAD(Sphere(1.0))
//  JfxViewer(obj)
//}

//test("rendering 1b") {
//  OpenSCAD.view(Sphere(1.0))
//}

//test("rendering 2a") {
//  val obj = Intersection(
//              Union(
//                Cube(1,1,1),
//                Translate( -0.5, -0.5, 0, Cube(1,1,1))
//              ),
//              Sphere(1.5)
//            )
//  OpenSCAD.view(obj)
//}

//test("rendering 2b") {
//  val c = Cube(1,1,1)
//  val s = Sphere(1.5)
//  val u = Union(c, Translate( -0.5, -0.5, 0, c))
//  val obj = Intersection(u, s)
//  OpenSCAD.view(obj)
//}

//test("rendering 2c") {
//  import InlineOps._
//  val c = Cube(1,1,1)
//  val s = Sphere(1.5)
//  val obj = (c + c.move( -0.5, -0.5, 0)) * s
//  OpenSCAD.view(obj)
//}

}
