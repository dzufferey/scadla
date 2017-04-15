package scadla.backends.obj

import scadla._
import dzufferey.utils._
import dzufferey.utils.LogLevel._
import java.io._

object Printer {
  
  def store(obj: Polyhedron, fileName: String) = {
    val writer = new BufferedWriter(new FileWriter(fileName))
    try {
      val (points, faces) = obj.indexed
      writer.write("g ScadlaObject")
      writer.newLine
      points.foreach{ p =>
        writer.write("v " + p.x.toMillimeters + " " + p.y.toMillimeters + " " + p.z.toMillimeters)
        writer.newLine
      }
      writer.newLine
      faces.foreach{ case (a,b,c) =>
        writer.write("f " + a + " " + b + " " + c)
        writer.newLine
      }
      writer.newLine
    } finally writer.close
  }

}
