package scadla.backends.obj

import scadla.*
import dzufferey.utils.*
import dzufferey.utils.LogLevel.*
import java.io.*
import squants.space.{Length, Millimeters, LengthUnit}

//TODO make parametric in terms of unit

object Printer extends Printer(Millimeters) {
}

class Printer(unit: LengthUnit = Millimeters) {
  
  def store(obj: Polyhedron, fileName: String) = {
    val writer = new BufferedWriter(new FileWriter(fileName))
    try {
      val (points, faces) = obj.indexed
      writer.write("g ScadlaObject")
      writer.newLine
      points.foreach{ p =>
        writer.write("v " + p.x.to(unit) + " " + p.y.to(unit) + " " + p.z.to(unit))
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
