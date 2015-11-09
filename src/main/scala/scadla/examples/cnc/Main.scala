package scadla.examples.cnc

import scadla._
import scadla.InlineOps._
import scala.math._

object Main{
    
  val r = backends.Renderer.default
  //val r = backends.OpenSCAD
  //val r = backends.JCSG
  //val r = new backends.ParallelRenderer(backends.OpenSCAD)
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

    for ( (n,s) <- LinearActuator.parts) { //takes a bit too much memory to be done in parallel
      r.toSTL(s, "la-" + n + ".stl")
    }

  //r.toSTL(LinearActuator.motorGear, "la-motor.stl")
  //r.toSTL(LinearActuator.transmissionGear, "la-transmission.stl")
  //r.toSTL(LinearActuator.sunGearPart2, "la-sun2.stl")
  //r.toSTL(LinearActuator.sunGearPart1, "la-sun1.stl")
  //r.toSTL(LinearActuator.planetGear, "la-planet.stl")
  //r.toSTL(LinearActuator.basePlate(), "la-base.stl")

  //r.toSTL(Gimbal.version2inner(
  //  LinearActuator.length + 10,                                   //length
  //  LinearActuator.gimbalWidth,                                   //width
  //  30,                                                           //height
  //  LinearActuator.length/2 - LinearActuator.bearingCenter - 2,   //lengthOffset
  //  0,                                                            //widthOffset
  //  8,                                                            //maxThickness
  //  5,                                                            //minThickness
  //  2,                                                            //knobLength
  //  1                                                             //retainerThickness
  //), "gimbal.stl")

  //r.toSTL(Extrusion(50), "extrusion.stl")
  //r.toSTL(Extrusion.pad(10,1.5), "pad.stl")
  //val af = new ActuatorFasterner(120 * sin(Pi/4), 120 * cos(Pi/4))
  //r.toSTL(af.connector(true), "connector1.stl")
  //r.toSTL(af.connector(false), "connector2.stl")

    println("work in progress")
  }

}
