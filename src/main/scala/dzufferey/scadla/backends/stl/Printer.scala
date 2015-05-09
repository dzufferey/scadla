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
    def outputPoint(p: Point) {
      out.putFloat(p.x.toFloat)
      out.putFloat(p.y.toFloat)
      out.putFloat(p.z.toFloat)
    }
    val header = Array.fill[Byte](80)(' '.toByte)
    "Generated with Scadla".getBytes.copyToArray(header)
    out.put(header)
    out.putInt(obj.faces.size)
    obj.faces.foreach{ case f @ Face(p1, p2, p3) =>
      val n = f.normal
      out.putFloat(n.x.toFloat)
      out.putFloat(n.y.toFloat)
      out.putFloat(n.z.toFloat)
      outputPoint(p1)
      outputPoint(p2)
      outputPoint(p3)
      out.putShort(0)
    }
  }

  def storeText(obj: Polyhedron, fileName: String) = {
    val writer = new BufferedWriter(new FileWriter(fileName))
    try writeASCII(obj, writer)
    finally writer.close
  }
  
  /*
  def storeBinary(obj: Polyhedron, fileName: String) = {
    val stream = new FileOutputStream(fileName)
    val chan = stream.getChannel
    val size = 84 + 50 * obj.faces.size
    assert(size.toLong == 84l + 50l * obj.faces.size, "checking for overflow")
    val buffer = ByteBuffer.allocate(size)
    writeBinary(obj, buffer)
    buffer.flip
    chan.write(buffer)
    chan.close
  }
  */

  def storeBinary(obj: Polyhedron, fileName: String) = {
    val stream = new RandomAccessFile(fileName, "rw")
    val chan = stream.getChannel
    val size = 84 + 50 * obj.faces.size
    assert(size.toLong == 84l + 50l * obj.faces.size, "checking for overflow")
    chan.truncate(size)
    val buffer = chan.map(FileChannel.MapMode.READ_WRITE, 0, size)
    chan.close
    writeBinary(obj, buffer)
    buffer.force
  }

  def isPresent = SysCmd(Array("meshlab", "-h"))._1 == 0

}

