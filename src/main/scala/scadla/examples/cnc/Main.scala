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
  //r.toSTL(Frame(250, 20, Pi/4, 100), "frame.stl")

  //for ( (n,s) <- LinearActuator.parts) { //takes a bit too much memory to be done in parallel
  //  r.toSTL(s, "la-" + n + ".stl")
  //}

  //r.toSTL(LinearActuator.motorGear, "la-motor.stl")
  //r.toSTL(LinearActuator.transmissionGear, "la-transmission.stl")
  //r.toSTL(LinearActuator.sunGearPart1, "la-sun1.stl")
  //r.toSTL(LinearActuator.sunGearPart2, "la-sun2.stl")
  //r.toSTL(LinearActuator.planetGear, "la-planet.stl")
  //r.toSTL(LinearActuator.basePlate, "la-base.stl")

  //r.toSTL(Gimbal.roundedSquare(
  //  LinearActuator.gimbalWidth + 16,                              //length
  //  LinearActuator.length + 16 + 20,                              //width
  //  30,                                                           //height
  //  2,                                                            //rounding
  //  8,                                                            //thickness
  //  0,                                                            //lengthOffset
  //  LinearActuator.length/2 - LinearActuator.bearingCenter - 2,   //widthOffset
  //  0,                                                            //heightOffset
  //  2,                                                            //knobLength
  //  1                                                             //retainerThickness
  //), "gimbal.stl")

  //r.toSTL(Extrusion(50), "extrusion.stl")
  //r.toSTL(Extrusion.pad(10,1.5), "pad.stl")
  //val af = new ActuatorFasterner(50, 100)
  //r.view(af.connector(true))

    println("work in progress")
  }

}
