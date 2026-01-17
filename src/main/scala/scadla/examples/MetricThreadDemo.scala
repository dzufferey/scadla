package scadla.example

import scadla.*
import scadla.utils.*
import InlineOps.*
import scadla.utils.thread.*
import scala.language.postfixOps
import squants.space.LengthConversions.*

object MetricThreadDemo {

  //TODO some screws by name

  def renderingOption = List("$fn=30;")
  def renderer = new backends.OpenSCAD(renderingOption)

  def demo = {

    val m = new MetricThread()

    val set = Map(
      1-> (1 mm),
      2-> (2 mm),
      3-> (3 mm),
      4-> (4 mm),
      5-> (1 mm),
      6-> (8 mm),
      7-> (6 mm),
      8-> (5 mm)
    )

    def boltAndNut(x: Int, y: Int) = {
      val i = 4*x + y
      val di = set(i)
      val bolt = m.hexScrewIso( di, di*2, 2, di*0.6, 0 mm, di/1.7)
      val nut = m.hexNutIso(di, di/1.2)
      val pair = bolt + nut.moveZ( di/2 + di*0.6 + 6*m.getIsoPitch(di))
      pair.translate(x*12 mm, (y-1)*12 + x*(y-2)*8 mm, 0 mm)
    }

    val all = for (x <- 0 to 1; y <- 1 to 4) yield boltAndNut(x, y)
    Union(all:_*)
  }

  def test = {
    val m = new MetricThread()
    //m.hexHead(4, 8)
    m.hexScrewIso( 8 mm, 12.5 mm, 2, 7.5 mm, 0 mm, 3 mm)
    //m.hexNutIso( 8, 4)
    //m.screwThreadIsoOuter( 8, 10, 2)
    //m.screwThreadIsoInner( 8, 6)
  }
  
  def main(args: Array[String]): Unit = {
    //val obj = test
    val obj = demo
    //render and display the wheel
    //backends.OpenSCAD.saveFile("test.scad", obj, renderingOption)
    Console.println("rendering set of metric ISO bolts and nuts. This may take a while ...")
    //new backends.ParallelRenderer(renderer).toSTL(obj, "metric_threads.stl")
    renderer.view(obj)
  }

}
