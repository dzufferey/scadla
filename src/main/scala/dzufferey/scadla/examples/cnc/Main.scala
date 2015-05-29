package dzufferey.scadla.examples.cnc

import dzufferey.scadla._

object Main{
    
  val r = backends.OpenSCAD
  //val r = backends.JCSG
  //val r = new backends.ParallelRenderer(backends.OpenSCAD)
  //val r = new backends.ParallelRenderer(backends.JCSG)
  
  def main(args: Array[String]) {
  //r.toSTL(BitsHolder(4, 2, 3.5, 16, 12), "bits_holder.stl")

  //Spindle.objects.par.foreach{ case (name, obj) => 
  //  r.toSTL(obj, name + ".stl")
  //}
  //r.toSTL(Spindle.objects("chuck"), "chuck.stl")
  //r.toSTL(Spindle.objects("gear_bolt"), "gear_bolt.stl")
  //r.toSTL(Spindle.objects("gear_motor"), "gear_motor.stl")
  //r.toSTL(Rotate(math.Pi, 0, 0, Spindle.objects("bearing_support")), "bearing_support.stl")
  //r.toSTL(Spindle.objects("spindle_body"), "spindle_body.stl")

  //val j = new Joint2DOF()
  //j.parts.zipWithIndex.par.foreach{ case (p, i) =>
  //  r.toSTL(p, "j2dof_" + i + ".stl")
  //}

  //r.toSTL(Platform(4, 10, 10, 1.8), "platform.stl")

    println("work in progress")
  }

}
