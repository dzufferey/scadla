package scadla.backends.ply

import scadla._
import dzufferey.utils._
import dzufferey.utils.LogLevel._
import java.io._
import java.nio.{ByteBuffer,ByteOrder}
import java.nio.channels.FileChannel

object Printer {

  def printHeader(writer: BufferedWriter, nbrVertex: Int, nbrFace: Int) {
    writer.write("ply"); writer.newLine
    writer.write("format ascii 1.0"); writer.newLine
    writer.write("element vertex " + nbrVertex); writer.newLine
    writer.write("property float x"); writer.newLine
    writer.write("property float y"); writer.newLine
    writer.write("property float z"); writer.newLine
    writer.write("element face " + nbrFace); writer.newLine
    writer.write("property list uchar uint vertex_indices"); writer.newLine
    writer.write("end_header"); writer.newLine
  }

  def store(obj: Polyhedron, fileName: String) = {
    val (points, faces) = obj.indexed
    val writer = new BufferedWriter(new FileWriter(fileName))
    try {
      printHeader(writer, points.length, faces.size)
      points.foreach{ case Point(x,y,z) =>
        writer.write(x + " " + y + " " + z)
        writer.newLine
      }
      faces.foreach{ case (a,b,c) =>
        writer.write("3 " + a + " " + b + " " + c)
        writer.newLine
      }
    } finally writer.close
  }

}
