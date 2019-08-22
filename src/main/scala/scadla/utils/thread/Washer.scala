package scadla.utils.thread

import scadla._
import scadla.backends.renderers.Renderable
import scadla.backends.renderers.Solids.{Difference, Translate}
import scadla.utils._
import squants.space.Length

import scala.language.postfixOps
import squants.space.LengthConversions._

object Washer {

  def apply(innerDiameter: Length, outerDiameter: Length, thickness: Length)(implicit ev1: Renderable[Cylinder], ev2: Renderable[Translate], ev3: Renderable[Difference]) = {
    Tube(outerDiameter/2, innerDiameter/2, thickness)
  }
  
  def metric(innerDiameter: Double, outerDiameter: Double, thickness: Double)(implicit ev1: Renderable[Cylinder], ev2: Renderable[Translate], ev3: Renderable[Difference]) = {
    apply(innerDiameter mm, outerDiameter mm, thickness mm)
  }
  
  def imperial(innerDiameter: Double, outerDiameter: Double, thickness: Double)(implicit ev1: Renderable[Cylinder], ev2: Renderable[Translate], ev3: Renderable[Difference]) = {
    apply(innerDiameter inches, outerDiameter inches, thickness inches)
  }

  trait ISO {
    implicit val ev1: Renderable[Cylinder]
    implicit val ev2: Renderable[Translate]
    implicit val ev3: Renderable[Difference]
    //metric versions (ISO)
    //val M1   = did not find
    //val M1_2 = did not find
    val M1_6 = metric(1.7, 4, 0.3)
    val M2 = metric(2.2, 5, 0.3)
    val M2_5 = metric(2.7, 6, 0.5)
    val M3 = metric(3.2, 7, 0.5)
    val M4 = metric(4.3, 9, 0.8)
    val M5 = metric(5.3, 10, 1)
    val M6 = metric(6.4, 12, 1.6)
    val M8 = metric(8.4, 16, 1.6)
    val M10 = metric(10.5, 20, 2)
    val M12 = metric(13, 24, 2.5)
    val M16 = metric(17, 30, 3)
    val M20 = metric(21, 37, 3)
    val M24 = metric(25, 44, 4)
    val M30 = metric(31, 56, 4)
    val M36 = metric(37, 66, 5)
    val M42 = metric(43, 78, 7)
    val M48 = metric(50, 92, 8)
    val M56 = metric(58, 105, 9)
    //val M64  = did not find

    //imperial versions (ANSI type B, regular)
    val _1_8 = imperial(0.1410, 0.4060, 0.0400)
    val _5_32 = imperial(0.1880, 0.5000, 0.0400)
    val _1_4 = imperial(0.2810, 0.7340, 0.0630)
    val _5_16 = imperial(0.3440, 0.8750, 0.0630)
    val _3_8 = imperial(0.4060, 1.0000, 0.0630)
    val _7_16 = imperial(0.4690, 1.1250, 0.0630)
    val _1_2 = imperial(0.5310, 1.2500, 0.1000)
    val _9_16 = imperial(0.5940, 1.4690, 0.1000)
    val _5_8 = imperial(0.6560, 1.7500, 0.1000)
    //val _11_16 = did not find
    val _3_4 = imperial(0.8120, 2.0000, 0.1000)
    val _7_8 = imperial(0.9380, 2.2500, 0.1600)
    //val _15_16 = did not find
    val _1 = imperial(1.0620, 2.5000, 0.1600)
  }
}
