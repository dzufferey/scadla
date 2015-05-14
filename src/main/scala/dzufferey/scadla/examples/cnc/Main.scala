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

  //val j = new Joint2DOF()
  //j.parts.zipWithIndex.par.foreach{ case (p, i) =>
  //  r.toSTL(p, "j2dof_" + i + ".stl")
  //}

    sys.error("work in progress")
  }

}
