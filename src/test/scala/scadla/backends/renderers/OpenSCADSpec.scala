package scadla.backends.renderers

import java.io.{BufferedWriter, StringWriter}

import org.scalatest.{Matchers, WordSpecLike}
import scadla.examples.cnc.Spindle
import scadla.examples.{BeltMold, ExtrusionDemo, WhiteboardMarkerHolder}
import scadla.{Solid, backends}
import scadla.utils.Trig.Pi

class OpenSCADSpec extends WordSpecLike with Matchers {

  "OpenScad" should {

    def compare(s: Solid, expected: String) = {
      val renderer = backends.OpenSCAD
      val strW = new StringWriter()
      val out = new BufferedWriter(strW)
      renderer.print(s, out)
      out.flush()

      val res = strW.toString

      res shouldBe expected
    }

    "work with WhiteboardMaker examples" in {
      import scadla.InlineOps._

      val s = WhiteboardMarkerHolder.top.rotateX(Pi)

      val original =
        """$fa=4;
          |$fs=0.5;
          |rotate([180.0,0.0,0.0])
          |  difference(){
          |    union(){
          |      union(){
          |        translate([-45.0,-15.0,3.0])
          |          cube([ 90.0, 10.0, 3.0]);
          |        translate([-25.0,-15.0,0.0])
          |          cube([ 50.0, 13.0, 6.0]);
          |      }
          |      hull(){
          |        cylinder( r1 = 15.0, r2 = 15.0, h = 6.0);
          |        translate([-15.0,-15.0,0.0])
          |          cube([ 30.0, 1.0, 6.0]);
          |      }
          |    }
          |    cylinder( r1 = 12.2, r2 = 12.2, h = 5.2);
          |  }
          |""".stripMargin

      compare(s, original)
    }

    "work with BeltMold examples" in {

      val s1 = BeltMold.sampleBelt.inner
      val s2 = BeltMold.sampleBelt.spreader

      val original1 =
        """$fa=4;
          |$fs=0.5;
          |difference(){
          |  difference(){
          |    union(){
          |      union(){
          |        cylinder( r1 = 34.83098861837907, r2 = 34.83098861837907, h = 1.0);
          |        translate([0.0,0.0,1.0])
          |          cylinder( r1 = 33.83098861837907, r2 = 33.83098861837907, h = 1.0);
          |        translate([0.0,0.0,2.0])
          |          cylinder( r1 = 31.830988618379067, r2 = 31.830988618379067, h = 2.0);
          |      }
          |      m_0();
          |      m_1();
          |      m_2();
          |    }
          |    rotate([0.0,0.0,59.99999999999999])
          |      minkowski(){
          |        union(){
          |          m_0();
          |          m_1();
          |          m_2();
          |        }
          |        translate([-0.1,-0.1,-0.1])
          |          cube([ 0.2, 0.2, 0.2]);
          |      }
          |  }
          |  rotate([0.0,0.0,29.999999999999996])
          |    m_3();
          |  rotate([0.0,0.0,90.0])
          |    m_3();
          |  rotate([0.0,0.0,149.99999999999997])
          |    m_3();
          |  rotate([0.0,0.0,210.0])
          |    m_3();
          |  rotate([0.0,0.0,270.0])
          |    m_3();
          |  rotate([0.0,0.0,330.0])
          |    m_3();
          |}
          |
          |
          |module m_0() {
          |  rotate([0.0,0.0,0.0])
          |    m_4();
          |}
          |
          |
          |module m_1() {
          |  rotate([0.0,0.0,119.99999999999999])
          |    m_4();
          |}
          |
          |
          |module m_4() {
          |  translate([26.830988618379067,0.0,0.0])
          |    cylinder( r1 = 2.0, r2 = 2.0, h = 8.0);
          |}
          |
          |
          |module m_3() {
          |  translate([26.830988618379067,0.0,0.0])
          |    cylinder( r1 = 1.5, r2 = 1.5, h = 6.0);
          |}
          |
          |
          |module m_2() {
          |  rotate([0.0,0.0,239.99999999999997])
          |    m_4();
          |}
          |""".stripMargin

      val original2 =
        """$fa=4;
          |$fs=0.5;
          |difference(){
          |  intersection(){
          |    union(){
          |      union(){
          |        union(){
          |          difference(){
          |            difference(){
          |              m_0();
          |              translate([0.0,0.0,-1.0])
          |                cylinder( r1 = 33.83098861837907, r2 = 33.83098861837907, h = 8.0);
          |            }
          |            union(){
          |              m_1();
          |              rotate([0.0,0.0,180.0])
          |                m_2();
          |            }
          |          }
          |          m_3();
          |        }
          |        mirror([1.0,0.0,0.0])
          |          m_3();
          |      }
          |      translate([0.0,0.0,1.0])
          |        difference(){
          |          difference(){
          |            cylinder( r1 = 34.03098861837907, r2 = 34.03098861837907, h = 4.0);
          |            translate([0.0,0.0,-1.0])
          |              cylinder( r1 = 33.03098861837907, r2 = 33.03098861837907, h = 6.0);
          |          }
          |          union(){
          |            translate([-35.03098861837907,-35.03098861837907,-0.5])
          |              cube([ 70.06197723675814, 35.03098861837907, 5.0]);
          |            rotate([0.0,0.0,180.0])
          |              translate([0.0,0.0,-0.5])
          |                cube([ 35.03098861837907, 35.03098861837907, 5.0]);
          |          }
          |        }
          |      translate([0.0,0.0,1.5])
          |        difference(){
          |          difference(){
          |            cylinder( r1 = 34.03098861837907, r2 = 34.03098861837907, h = 3.0);
          |            translate([0.0,0.0,-1.0])
          |              cylinder( r1 = 32.53098861837907, r2 = 32.53098861837907, h = 5.0);
          |          }
          |          union(){
          |            translate([-35.03098861837907,-35.03098861837907,-0.5])
          |              cube([ 70.06197723675814, 35.03098861837907, 4.0]);
          |            rotate([0.0,0.0,180.0])
          |              translate([0.0,0.0,-0.5])
          |                cube([ 35.03098861837907, 35.03098861837907, 4.0]);
          |          }
          |        }
          |    }
          |    rotate([0.0,0.0,90.0])
          |      difference(){
          |        difference(){
          |          m_0();
          |          translate([0.0,0.0,-1.0])
          |            cylinder( r1 = 0.0, r2 = 0.0, h = 8.0);
          |        }
          |        union(){
          |          m_1();
          |          translate([-36.83098861837907,-0.5,0.0])
          |            m_2();
          |          rotate([0.0,0.0,29.999999999999996])
          |            m_2();
          |        }
          |      }
          |  }
          |  translate([-31.830988618379067,0.0,0.0])
          |    cube([ 31.830988618379067, 31.830988618379067, 6.0]);
          |}
          |
          |
          |module m_0() {
          |  cylinder( r1 = 35.83098861837907, r2 = 35.83098861837907, h = 6.0);
          |}
          |
          |
          |module m_1() {
          |  translate([-36.83098861837907,-36.83098861837907,-0.5])
          |    cube([ 73.66197723675813, 36.83098861837907, 7.0]);
          |}
          |
          |
          |module m_2() {
          |  translate([0.0,0.0,-0.5])
          |    cube([ 36.83098861837907, 36.83098861837907, 7.0]);
          |}
          |
          |
          |module m_3() {
          |  translate([33.83098861837907,0.0,0.0])
          |    difference(){
          |      cube([ 10.0, 5.0, 6.0]);
          |      translate([7.0,0.0,3.0])
          |        rotate([-90.0,0.0,0.0])
          |          cylinder( r1 = 1.5, r2 = 1.5, h = 5.0);
          |    }
          |}
          |""".stripMargin

      compare(s1, original1)
      compare(s2, original2)
    }

    "work with ExtrusionDemo examples" in {

      val s1 = ExtrusionDemo.s

      val original1 =
        """$fa=4;
          |$fs=0.5;
          |union(){
          |  translate([0.0,0.0,0.0])
          |    difference(){
          |      union(){
          |        difference(){
          |          translate([-10.0,-10.0,0.0])
          |            minkowski(){
          |              translate([1.5,1.5,0.0])
          |                cube([ 17.0, 17.0, 50.0]);
          |              cylinder( r1 = 1.5, r2 = 1.5, h = 50.0);
          |            }
          |          translate([0.0,0.0,-1.0])
          |            translate([-8.0,-8.0,0.0])
          |              cube([ 16.0, 16.0, 101.0]);
          |          translate([0.0,0.0,-1.0])
          |            translate([-11.0,-3.25,0.0])
          |              cube([ 22.0, 6.5, 101.0]);
          |          translate([0.0,0.0,-1.0])
          |            translate([-3.25,-11.0,0.0])
          |              cube([ 6.5, 22.0, 101.0]);
          |        }
          |        translate([-4.0,-4.0,0.0])
          |          cube([ 8.0, 8.0, 100.0]);
          |        translate([-7.5,-7.5,0.0])
          |          m_0();
          |        translate([-7.5,7.5,0.0])
          |          m_0();
          |        translate([7.5,-7.5,0.0])
          |          m_0();
          |        translate([7.5,7.5,0.0])
          |          m_0();
          |        rotate([0.0,0.0,45.0])
          |          m_1();
          |        rotate([0.0,0.0,-45.0])
          |          m_1();
          |      }
          |      translate([0.0,0.0,-1.0])
          |        cylinder( r1 = 2.1, r2 = 2.1, h = 102.0);
          |    }
          |  translate([0.0,25.0,0.0])
          |    union(){
          |      union(){
          |        m_2();
          |        translate([0.0,16.0,0.0])
          |          m_3();
          |      }
          |      translate([15.0,16.0,0.0])
          |        m_3();
          |    }
          |  translate([0.0,50.0,0.0])
          |    union(){
          |      union(){
          |        translate([0.0,16.0,0.0])
          |          m_4();
          |        m_4();
          |      }
          |      m_5();
          |    }
          |  translate([0.0,75.0,0.0])
          |    union(){
          |      m_6();
          |      m_4();
          |    }
          |  translate([0.0,100.0,0.0])
          |    union(){
          |      m_5();
          |      m_4();
          |    }
          |  translate([0.0,125.0,0.0])
          |    m_2();
          |  translate([0.0,150.0,0.0])
          |    union(){
          |      union(){
          |        translate([0.0,16.0,0.0])
          |          m_7();
          |        m_5();
          |      }
          |      translate([8.0,0.0,0.0])
          |        m_7();
          |    }
          |}
          |
          |
          |module m_5() {
          |  translate([8.0,0.0,0.0])
          |    m_6();
          |}
          |
          |
          |module m_6() {
          |  cube([ 4.0, 20.0, 100.0]);
          |}
          |
          |
          |module m_4() {
          |  cube([ 20.0, 4.0, 100.0]);
          |}
          |
          |
          |module m_1() {
          |  translate([-0.875,-12.5,0.0])
          |    cube([ 1.75, 25.0, 100.0]);
          |}
          |
          |
          |module m_7() {
          |  cube([ 12.0, 4.0, 100.0]);
          |}
          |
          |
          |module m_3() {
          |  cube([ 5.0, 4.0, 100.0]);
          |}
          |
          |
          |module m_0() {
          |  translate([-1.5,-1.5,0.0])
          |    cube([ 3.0, 3.0, 100.0]);
          |}
          |
          |
          |module m_2() {
          |  union(){
          |    union(){
          |      m_4();
          |      m_6();
          |    }
          |    translate([16.0,0.0,0.0])
          |      m_6();
          |  }
          |}
          |""".stripMargin

      compare(s1, original1)
    }

    "work with Spindle examples" in {

      val s1 = Spindle.motorBase

      val original1 =
        """$fa=4;
          |$fs=0.5;
          |difference(){
          |  difference(){
          |    difference(){
          |      union(){
          |        hull(){
          |          translate([0.0,0.0,5.0])
          |            rotate([-90.0,0.0,0.0])
          |              polyhedron( points=[ [30.0,0.0,0.0], [-8.0,5.0,21.0], [-8.0,0.0,21.0], [38.0,0.0,21.0], [0.0,5.0,0.0], [38.0,5.0,21.0], [30.0,5.0,0.0], [0.0,0.0,0.0] ], faces=[ [7,4,0], [0,4,6], [2,3,1], [3,5,1], [7,0,2], [0,3,2], [0,6,3], [6,5,3], [6,4,5], [4,1,5], [4,7,1], [7,2,1] ]);
          |          translate([0.0,0.0,30.700000000000003])
          |            cube([ 30.0, 23.5, 3.0]);
          |        }
          |        translate([0.0,0.0,33.7])
          |          cube([ 30.0, 30.0, 3.0]);
          |      }
          |      hull(){
          |        m_0();
          |        translate([0.0,15.0,0.0])
          |          m_0();
          |      }
          |    }
          |    translate([0.0,0.0,26.700000000000003])
          |      translate([3.25,3.25,0.0])
          |        m_1();
          |    translate([0.0,0.0,26.700000000000003])
          |      translate([26.75,3.25,0.0])
          |        m_1();
          |    translate([0.0,0.0,26.700000000000003])
          |      translate([3.25,26.75,0.0])
          |        m_1();
          |    translate([0.0,0.0,26.700000000000003])
          |      translate([26.75,26.75,0.0])
          |        m_1();
          |    translate([0.0,0.0,26.700000000000003])
          |      translate([26.75,3.25,4.0])
          |        m_2();
          |    translate([0.0,0.0,26.700000000000003])
          |      translate([26.75,26.75,4.0])
          |        m_2();
          |    translate([0.0,0.0,26.700000000000003])
          |      translate([3.25,3.25,4.0])
          |        m_3();
          |    translate([0.0,0.0,26.700000000000003])
          |      translate([3.25,26.75,4.0])
          |        m_3();
          |  }
          |  translate([15.0,15.0,36.7])
          |    rotate([90.0,0.0,0.0])
          |      scale([0.3,1.0,1.0])
          |        translate([0.0,0.0,-25.0])
          |          cylinder( r1 = 20.0, r2 = 20.0, h = 50.0);
          |}
          |
          |
          |module m_0() {
          |  translate([15.0,15.0,0.0])
          |    cylinder( r1 = 8.0, r2 = 8.0, h = 36.7);
          |}
          |
          |
          |module m_1() {
          |  cylinder( r1 = 1.5, r2 = 1.5, h = 10.0);
          |}
          |
          |
          |module m_2() {
          |  minkowski(){
          |    hull(){
          |      polyhedron( points=[ [3.02531541055364,0.0,0.0], [1.5126577052768204,2.6200000000000006,2.6200000000000006], [-1.5126577052768193,2.620000000000001,2.6200000000000006], [-3.02531541055364,3.704942833945676E-16,0.0], [3.02531541055364,0.0,2.6200000000000006], [-1.5126577052768213,-2.6200000000000006,2.6200000000000006], [1.5126577052768204,-2.6200000000000006,2.6200000000000006], [-1.5126577052768193,2.620000000000001,0.0], [1.5126577052768204,-2.6200000000000006,0.0], [-1.5126577052768213,-2.6200000000000006,0.0], [1.5126577052768204,2.6200000000000006,0.0], [-3.02531541055364,3.704942833945676E-16,2.6200000000000006] ], faces=[ [0,10,1], [10,7,2], [7,3,11], [3,9,5], [9,8,6], [8,0,4], [4,0,1], [1,10,2], [2,7,11], [11,3,5], [5,9,6], [6,8,4], [0,7,10], [7,9,3], [9,0,8], [0,9,7], [4,1,2], [2,11,5], [5,6,4], [4,2,5] ]);
          |      translate([5.0,0.0,0.0])
          |        polyhedron( points=[ [3.02531541055364,0.0,0.0], [1.5126577052768204,2.6200000000000006,2.6200000000000006], [-1.5126577052768193,2.620000000000001,2.6200000000000006], [-3.02531541055364,3.704942833945676E-16,0.0], [3.02531541055364,0.0,2.6200000000000006], [-1.5126577052768213,-2.6200000000000006,2.6200000000000006], [1.5126577052768204,-2.6200000000000006,2.6200000000000006], [-1.5126577052768193,2.620000000000001,0.0], [1.5126577052768204,-2.6200000000000006,0.0], [-1.5126577052768213,-2.6200000000000006,0.0], [1.5126577052768204,2.6200000000000006,0.0], [-3.02531541055364,3.704942833945676E-16,2.6200000000000006] ], faces=[ [0,10,1], [10,7,2], [7,3,11], [3,9,5], [9,8,6], [8,0,4], [4,0,1], [1,10,2], [2,7,11], [11,3,5], [5,9,6], [6,8,4], [0,7,10], [7,9,3], [9,0,8], [0,9,7], [4,1,2], [2,11,5], [5,6,4], [4,2,5] ]);
          |    }
          |    translate([-0.2,-0.2,-0.2])
          |      cube([ 0.4, 0.4, 0.4]);
          |  }
          |}
          |
          |
          |module m_3() {
          |  rotate([0.0,0.0,180.0])
          |    m_2();
          |}
          |""".stripMargin

      compare(s1, original1)
    }


  }
}
