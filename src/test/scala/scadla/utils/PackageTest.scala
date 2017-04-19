package scadla.utils

import scadla._
import org.scalatest._
import scadla.EverythingIsIn.{millimeters, radians}  

class PackageTest extends FunSuite {

  def incr(map: Map[Solid, Int], s: Solid) = {
    val mult = map.getOrElse(s, 0) + 1
    map + (s -> mult)
  }

  test("fold 1") {
    val c = Cube(1,1,1)
    val tc = Translate( -0.5, -0.5, 0, Cube(1,1,1))
    val u = Union(Cube(1,1,1),
                  Translate( -0.5, -0.5, 0, Cube(1,1,1)))
    val s = Sphere(1.5)
    val i = Intersection(
              Union(
                Cube(1,1,1),
                Translate( -0.5, -0.5, 0, Cube(1,1,1))
              ),
              Sphere(1.5)
            )
    val map = fold(incr, Map[Solid, Int](), i)
    assert(map(i) == 1)
    assert(map(s) == 1)
    assert(map(u) == 1)
    assert(map(tc) == 1)
    assert(map(c) == 2)
  }
  
  test("fold 2") {
    val top=Difference(
              Difference(
                Difference(
                  Minkowski(Translate(3.25,3.25,0.0,Cube(23.5,23.5,13.0)), Cylinder(3.25,3.25,13.0)),
                  Translate(15.0,15.0,0.0,Cylinder(10.0,10.0,35.0))
                ),
                Translate(0.0,0.0,16.0,Translate(3.25,3.25,0.0,Cylinder(1.38,1.5,10.0))),
                Translate(0.0,0.0,16.0,Translate(26.75,3.25,0.0,Cylinder(1.38,1.5,10.0))),
                Translate(0.0,0.0,16.0,Translate(3.25,26.75,0.0,Cylinder(1.38,1.5,10.0))),
                Translate(0.0,0.0,16.0,Translate(26.75,26.75,0.0,Cylinder(1.38,1.5,10.0)))
              ),
              Translate(15.0,15.0,26.0,Rotate(1.5707963267948966,0.0,0.0,Scale(0.425,1.0,1.0,Translate(0.0,0.0,-25.0,Cylinder(20.0,20.0,50.0))))),
              Translate(15.0,15.0,26.0,Rotate(0.0,1.5707963267948966,0.0,Scale(1.0,0.425,1.0,Translate(0.0,0.0,-25.0,Cylinder(20.0,20.0,50.0)))))
            )
    val map = fold(incr, Map[Solid, Int](), top)
    assert(map(Cube(23.5,23.5,13.0)) == 1)
  }

}
