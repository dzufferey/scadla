package scadla.backends.x3d

import scadla._
import java.io._
import java.util.Date

object Printer {

  def write(obj: Polyhedron, writer: BufferedWriter, withHeader: Boolean = true, name: String = "scadla object") {
    def writeLine(s: String) {
      writer.write(s)
      writer.newLine
    }
    val (points, faces) = obj.indexed
    if (withHeader) {
      writeLine("<?xml version='1.0' encoding='UTF-8'?>")
      writeLine("<!DOCTYPE X3D PUBLIC 'ISO//Web3D//DTD X3D 3.3//EN' 'http://www.web3d.org/specifications/x3d-3.3.dtd'>")
      writeLine("<X3D profile='Interchange' version='3.3' xmlns:xsd='http://www.w3.org/2001/XMLSchema-instance' xsd:noNamespaceSchemaLocation='http://www.web3d.org/specifications/x3d-3.3.xsd'>")
      writeLine("  <head>")
      writeLine("    <meta content='"+name+"' name='title'/>")
      writeLine("    <meta content='"+ (new Date()).toString +"' name='created'/>")
      writeLine("  </head>")
    } else {
      writeLine("<X3D>")
    }
      writeLine("  <Scene>")
      writeLine("    <Shape>")
      writeLine("      <Appearance>")
      writeLine("        <Material diffuseColor='0.7 0.7 0.7'/>")
      writeLine("      </Appearance>")
      writer.write("      <IndexedFaceSet coordIndex='")
      faces.foreach{ case (a,b,c) =>
        writer.write(a.toString)
        writer.write(" ")
        writer.write(b.toString)
        writer.write(" ")
        writer.write(c.toString)
        writer.write(" -1 ")
      }
      writeLine("'>")
      writer.write("        <Coordinate point='")
      points.foreach{ p =>
        writer.write(p.x.toMillimeters.toString)
        writer.write(" ")
        writer.write(p.y.toMillimeters.toString)
        writer.write(" ")
        writer.write(p.z.toMillimeters.toString)
        writer.write(" ")
      }
      writeLine("'/>")
      writeLine("      </IndexedFaceSet>")
      writeLine("    </Shape>")
      writeLine("  </Scene>")
      writeLine("</X3D>")

  }
  
  def store(obj: Polyhedron, fileName: String) = {
    val writer = new BufferedWriter(new FileWriter(fileName))
    try write(obj, writer)
    finally writer.close
  }

}
