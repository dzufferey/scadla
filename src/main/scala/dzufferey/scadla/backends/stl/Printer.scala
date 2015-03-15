package dzufferey.scadla.backends.stl

import dzufferey.scadla._
import dzufferey.utils._
import dzufferey.utils.LogLevel._
import java.io._
import java.nio.{ByteBuffer,ByteOrder}
import java.nio.channels.FileChannel

object Printer {

  def writeASCII(obj: Polyhedron, writer: BufferedWriter, name: String = "") {
    writer.write("solid " + name); writer.newLine
    obj.faces.foreach{ case f @ Face(p1, p2, p3) =>
      val n = f.normal
      writer.write("facet normal ")
      writer.write(n.x + " " + n.y + " " + n.z)
      writer.newLine
      writer.write("\touter loop"); writer.newLine
      writer.write("\t\tvertex ")
      writer.write(p1.x + " " + p1.y + " " + p1.z)
      writer.newLine
      writer.write("\t\tvertex ")
      writer.write(p2.x + " " + p2.y + " " + p2.z)
      writer.newLine
      writer.write("\t\tvertex ")
      writer.write(p3.x + " " + p3.y + " " + p3.z)
      writer.newLine
      writer.write("\tendloop"); writer.newLine
      writer.write("endfacet"); writer.newLine
    }
    writer.write("endsolid " + name); writer.newLine
  }

  def writeBinary(obj: Polyhedron, out: ByteBuffer) {
    if (out.order() != ByteOrder.LITTLE_ENDIAN) {
      out.order(ByteOrder.LITTLE_ENDIAN)
    }
    for (_ <- 0 until 10) out.putLong(0l)
    out.putInt(obj.faces.size)
    obj.faces.foreach{ case f @ Face(p1, p2, p3) =>
      val n = f.normal
      out.putFloat(n.x.toFloat)
      out.putFloat(n.y.toFloat)
      out.putFloat(n.z.toFloat)
      out.putFloat(p1.x.toFloat)
      out.putFloat(p1.y.toFloat)
      out.putFloat(p1.z.toFloat)
      out.putFloat(p2.x.toFloat)
      out.putFloat(p2.y.toFloat)
      out.putFloat(p2.z.toFloat)
      out.putFloat(p3.x.toFloat)
      out.putFloat(p3.y.toFloat)
      out.putFloat(p3.z.toFloat)
      out.putShort(0)
    }
  }

  def storeText(obj: Polyhedron, fileName: String) = {
    val writer = new BufferedWriter(new FileWriter(fileName))
    try writeASCII(obj, writer)
    finally writer.close
  }
  
  def storeBinary(obj: Polyhedron, fileName: String) = {
    val stream = new FileOutputStream(fileName)
    val chan = stream.getChannel
    val size = 80 + 50 * obj.faces.size
    val buffer = ByteBuffer.allocate(size)
    writeBinary(obj, buffer)
    chan.write(buffer)
    chan.close
  }

}

