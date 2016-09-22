package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scala.math._

object Main{
    
  val r = backends.Renderer.default
  //val r = backends.OpenSCAD
  //val r = backends.JCSG
  //val r = new backends.ParallelRenderer(backends.JCSG)
  
  def main(args: Array[String]) {

  //Spindle.objects.par.foreach{ case (name, obj) => 
  //  r.toSTL(obj, "spdl-" + name + ".stl")
  //}

  //new Joint2DOF().parts.zipWithIndex.par.foreach{ case (p, i) =>
  //  r.toSTL(p, "j2dof-" + i + ".stl")
  //}

  //r.toSTL(Platform(4, 10, 10, 1.6), "platform.stl")

  //r.toSTL(Frame(250, 18, Pi/4, 120), "frame.stl")

  //r.toSTL(LinearActuatorBlock(true), "la-gimbal.stl")

  //r.toSTL(scadla.examples.extrusion._2020(50), "extrusion.stl")
  //r.toSTL(scadla.examples.extrusion._2020.pad(10,1.5,Common.tolerance), "pad.stl")
  //val af = new ActuatorFasterner(120 * sin(Pi/4), 120 * cos(Pi/4))
  //r.toSTL(af.connector(true), "connector1.stl")
  //r.toSTL(af.connector(false), "connector2.stl")

    for ( (n,s) <- LinearActuator.parts.par) {
      r.toSTL(s, "la-" + n + ".stl")
    }

    println("work in progress")
  }

}
