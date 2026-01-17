package scadla.backends.stl

import scadla.*
import dzufferey.utils.*
import dzufferey.utils.LogLevel.*
import java.io.*
import java.nio.{ByteBuffer,ByteOrder}
import java.nio.channels.FileChannel
import squants.space.{LengthUnit, Millimeters}

object Printer extends Printer(Millimeters) {
}

class Printer(unit: LengthUnit = Millimeters) {

  def writeASCII(obj: Polyhedron, writer: BufferedWriter, name: String = ""): Unit = {
    writer.write("solid " + name); writer.newLine
    obj.faces.foreach{ case f @ Face(p1, p2, p3) =>
      val n = f.normal
      writer.write("facet normal ")
      writer.write(s"${n.x} ${n.y} ${n.z}")
      writer.newLine
      writer.write("\touter loop"); writer.newLine
      writer.write("\t\tvertex ")
      writer.write(s"${p1.x} ${p1.y} ${p1.z}")
      writer.newLine
      writer.write("\t\tvertex ")
      writer.write(s"${p2.x} ${p2.y} ${p2.z}")
      writer.newLine
      writer.write("\t\tvertex ")
      writer.write(s"${p3.x} ${p3.y} ${p3.z}")
      writer.newLine
      writer.write("\tendloop"); writer.newLine
      writer.write("endfacet"); writer.newLine
    }
    writer.write("endsolid " + name); writer.newLine
  }

  def writeBinary(obj: Polyhedron, out: ByteBuffer): Unit = {
    if (out.order() != ByteOrder.LITTLE_ENDIAN) {
      out.order(ByteOrder.LITTLE_ENDIAN)
    }
    def outputPoint(p: Point): Unit = {
      out.putFloat(p.x.to(unit).toFloat)
      out.putFloat(p.y.to(unit).toFloat)
      out.putFloat(p.z.to(unit).toFloat)
    }
    val header = Array.fill[Byte](80)(' '.toByte)
    "Generated with Scadla".getBytes.copyToArray(header)
    out.put(header)
    out.putInt(obj.faces.size)
    obj.faces.foreach{ case f @ Face(p1, p2, p3) =>
      val n = f.normal
      out.putFloat(n.x.to(unit).toFloat)
      out.putFloat(n.y.to(unit).toFloat)
      out.putFloat(n.z.to(unit).toFloat)
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
    assert(size.toLong == 84L + 50L * obj.faces.size, "checking for overflow")
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
    assert(size.toLong == 84L + 50L * obj.faces.size, "checking for overflow")
    chan.truncate(size)
    val buffer = chan.map(FileChannel.MapMode.READ_WRITE, 0, size)
    chan.close
    writeBinary(obj, buffer)
    buffer.force
  }

}

