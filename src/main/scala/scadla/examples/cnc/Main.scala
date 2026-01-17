package scadla.examples.cnc

import scadla.*
import scadla.InlineOps.*
import scala.math.*

object Main{
    
  val r = backends.Renderer.default
  //val r = backends.OpenSCAD
  //val r = backends.JCSG
  //val r = new backends.ParallelRenderer(backends.JCSG)
  
  def main(args: Array[String]): Unit = {

  //Spindle.objects.par.foreach{ case (name, obj) => 
  //  r.toSTL(obj, "spdl-" + name + ".stl")
  //}

  //new Joint2DOF().parts.zipWithIndex.par.foreach{ case (p, i) =>
  //  r.toSTL(p, "j2dof-" + i + ".stl")
  //}
  //r.view(new Joint2DOF().cross)
  //r.view(new Joint2DOF().bottom(20))
  //r.view(new Joint2DOF().top(30, 16, 1.35))

  //r.view(Platform.with608Bearings())
  //r.toSTL(Platform.with608Bearings(), "platform.stl")
  //r.toSTL(Platform.verticalBushing2Rod(), "p2r.stl")
  //r.toSTL(Platform.verticalBushing2Platform(), "p2p.stl")

  //Frame.cableTensioner.zipWithIndex.foreach{ case (s,i) => r.toSTL(s, "cableTensioner_" + i + ".stl") }
  //r.view(Frame.cableAttachPoint(0))
  //Frame.cableAttachPoint.zipWithIndex.foreach{ case (s,i) => r.toSTL(s, "cableAttach_" + i + ".stl") }

  //r.toSTL(LinearActuatorBlock(true), "la-gimbal.stl")

  //r.toSTL(scadla.examples.extrusion._2020(50), "extrusion.stl")
  //r.toSTL(scadla.examples.extrusion._2020.pad(10,1.5,Common.tolerance), "pad.stl")
  //val af = new ActuatorFasterner(120 * sin(Pi/4), 120 * cos(Pi/4))
  //r.toSTL(af.connector(true), "connector1.stl")
  //r.toSTL(af.connector(false), "connector2.stl")

  //for ( (n,s) <- LinearActuator.parts(true).par) {
  //  r.toSTL(s(), "la-" + n + ".stl")
  //}

  //r.view(LinearActuator.assembled())
  //LinearActuator.gimbal.parts(true).zipWithIndex.par.foreach{ case (s, i) => r.toSTL(s, "gimbal_" + i + ".stl") }

  //r.view(Frame.assembled)
  //r.toSTL(Frame.connector1WithSupport, "connectorS1.stl")
  //r.toSTL(Frame.connector2WithSupport, "connectorS2.stl")
  //r.toSTL(Frame.connector3, "connector3.stl")
  //r.toSTL(Frame.actuatorConnector1, "actuatorConnector1.stl")
  //r.toSTL(Frame.actuatorConnector2, "actuatorConnector2.stl")
  //r.toSTL(Frame.hBeam, "hBeam.stl")
  //r.toSTL(Frame.vBeam, "vBeam.stl")
  //r.toSTL(Frame.tBeam, "tBeam.stl")
  //r.toSTL(Frame.foot(true), "foot.stl")
  //r.toSTL(Frame.hBeamJig1, "jig1.stl")
  //r.toSTL(Frame.hBeamJig2, "jig2.stl")

  //r.view(Pulley(6, 4, 2, 1, 1, 2))

    println("work in progress")
  }

}
